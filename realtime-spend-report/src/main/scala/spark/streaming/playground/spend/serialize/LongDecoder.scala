package spark.streaming.playground.spend.serialize

import kafka.serializer.Decoder
import kafka.utils.VerifiableProperties

import java.nio.ByteBuffer

class LongDecoder(props: VerifiableProperties = null) extends Decoder[Long] {
  override def fromBytes(bytes: Array[Byte]): Long = ByteBuffer.wrap(bytes).getLong
}
