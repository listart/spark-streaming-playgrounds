package spark.streaming.playground.spend

import _root_.kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import spark.streaming.playground.spend.domain.Transaction
import spark.streaming.playground.spend.serialize.LongDecoder
import spark.streaming.playground.spend.util.{DateTimeUtil, MysqlManager}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object SpendReport {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("SpendReport")
    val ssc = new StreamingContext(sparkConf, Seconds(1))
    ssc.checkpoint("out")

    val kafkaParams = Map("metadata.broker.list" -> "kafka:9092")
    val topics = Set("transactions")
    val kafkaSource = KafkaUtils.createDirectStream[Long, String, LongDecoder, StringDecoder](ssc, kafkaParams, topics)
    //    kafkaSource.print()

    kafkaSource
      .map { case (k: Long, v: String) => Transaction.from(v) }
      .window(Seconds(5), Seconds(5))
      .foreachRDD { (rdd, time) =>
        println(s"\n============================\n$time\n============================")
        if (!rdd.isEmpty()) {
          rdd
            .map { t: Transaction => ((t.accountId, t.timestamp.truncatedTo(ChronoUnit.MINUTES)), t.amount) }
            .reduceByKey(_ + _)
            .foreachPartition(batchSaveToMysql)
        }
      }

    ssc.start()
    ssc.awaitTermination()
  }

  def batchSaveToMysql(partitionRecords: Iterator[((Long, LocalDateTime), Int)]): Unit = {
    val conn = MysqlManager.getMysqlManager.getConnection
    val insertTemplate = "INSERT INTO spend_report(account_id, log_ts, amount) VALUES(?, ?, ?)"
    val ps = conn.prepareStatement(insertTemplate)
    try {
      conn.setAutoCommit(false)
      partitionRecords.foreach(record => {
        ps.setLong(1, record._1._1)
        ps.setString(2, DateTimeUtil.format(record._1._2))
        ps.setInt(3, record._2)
        ps.addBatch()
      })
      val results = ps.executeBatch()
      println(results.mkString("results of inserts:", ",", "."))
      conn.commit()
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      ps.close()
      conn.close()
    }
  }
}