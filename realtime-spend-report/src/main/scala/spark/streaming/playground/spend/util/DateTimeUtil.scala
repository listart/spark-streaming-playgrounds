package spark.streaming.playground.spend.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtil {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def  parse(text: CharSequence ): LocalDateTime =  {
    LocalDateTime.parse(text, formatter)
  }

  def format(dateTime: LocalDateTime): String = {
    dateTime.format(formatter)
  }
}
