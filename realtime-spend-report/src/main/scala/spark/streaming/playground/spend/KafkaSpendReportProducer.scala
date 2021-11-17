package spark.streaming.playground.spend

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

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
