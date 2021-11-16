package spark.streaming.playground.spend

import _root_.kafka.serializer.{Decoder, StringDecoder}
import _root_.kafka.utils.VerifiableProperties
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

import java.nio.ByteBuffer
import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class LongDecoder(props: VerifiableProperties = null) extends Decoder[Long] {
  override def fromBytes(bytes: Array[Byte]): Long = ByteBuffer.wrap(bytes).getLong
}

case class Transaction(accountId: Long, amount: Int, timestamp: LocalDateTime)

object Transaction {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def from(row: String): Transaction = {
    val fields = row.split(",")
    val accountId = fields(0).toLong
    val amount = fields(1).toInt
    val timestamp = LocalDateTime.parse(fields(2), formatter)

    new Transaction(accountId, amount, timestamp)
  }
}

class MysqlPool extends Serializable {
  private val cpds: ComboPooledDataSource = new ComboPooledDataSource(true)
  try {
    cpds.setJdbcUrl("jdbc:mysql://mysql:3306/sql-demo?useUnicode=true&characterEncoding=UTF-8")
    cpds.setDriverClass("com.mysql.cj.jdbc.Driver")
    cpds.setUser("sql-demo")
    cpds.setPassword("demo-sql")
    cpds.setMaxPoolSize(200)
    cpds.setMinPoolSize(20)
    cpds.setAcquireIncrement(5)
    cpds.setMaxStatements(180)
  } catch {
    case e: Exception => e.printStackTrace()
  }

  def getConnection: Connection = {
    try {
      cpds.getConnection()
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        null
    }
  }
}

object MysqlManager {
  var mysqlManager: MysqlPool = _

  def getMysqlManager: MysqlPool = {
    if (mysqlManager == null) {
      synchronized {
        if (mysqlManager == null) {
          mysqlManager = new MysqlPool
        }
      }
    }
    mysqlManager
  }
}

object SpendReport {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val insertTemplate = "INSERT INTO spend_report(account_id, log_ts, amount) VALUES(?, ?, ?)"

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
            .foreachPartition(partitionRecords => {
              val conn = MysqlManager.getMysqlManager.getConnection
              val ps = conn.prepareStatement(insertTemplate)
              try {
                conn.setAutoCommit(false)
                partitionRecords.foreach(record => {
                  ps.setLong(1, record._1._1)
                  ps.setString(2, record._1._2.format(formatter))
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
            })
        }
      }

    ssc.start()
    ssc.awaitTermination()
  }
}

object KafkaSpendReportProducer {
  def main(args: Array[String]): Unit = {
    val brokers = "kafka:9092"
    val topic = "wordCounts"
    val messagesPerSec = "5" // between 1 to 100
    val wordsPerMessage = "5"

    val props = scala.collection.JavaConverters.mapAsJavaMapConverter(
      Map[String, Object](
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer"
      )
    ).asJava

    val producer = new KafkaProducer[String, String](props)

    while (true) {
      (1 to messagesPerSec.toInt).foreach { _ =>
        val str = (1 to wordsPerMessage.toInt).map(_ => scala.util.Random.nextInt(10).toString).mkString(" ")

        val message = new ProducerRecord[String, String](topic, null, str)

        producer.send(message)
      }

      Thread.sleep(1000)
    }
  }
}