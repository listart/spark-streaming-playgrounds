package spark.streaming.playground.spend.domain

import spark.streaming.playground.spend.util.DateTimeUtil

import java.time.LocalDateTime

case class Transaction(accountId: Long, amount: Int, timestamp: LocalDateTime)

object Transaction {
  def from(row: String): Transaction = {
    val fields = row.split(",")
    val accountId = fields(0).toLong
    val amount = fields(1).toInt
    val timestamp = DateTimeUtil.parse(fields(2))

    new Transaction(accountId, amount, timestamp)
  }
}