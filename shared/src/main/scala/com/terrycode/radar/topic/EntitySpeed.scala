package com.terrycode.radar.topic

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema}
import org.apache.flink.api.common.typeinfo.TypeInformation

import java.time.LocalDateTime

case class EntitySpeed(entityName: String, capturedTime: LocalDateTime, averageKnots: Float, capturedBy: Array[String])

object EntitySpeed {
  given codec: JsonValueCodec[EntitySpeed] = JsonCodecMaker.make[EntitySpeed]

  given deserializationSchema: DeserializationSchema[EntitySpeed] = new {
    override def deserialize(message: Array[Byte]): EntitySpeed = readFromArray(message)

    override def isEndOfStream(nextElement: EntitySpeed): Boolean = false
    
    override def getProducedType: TypeInformation[EntitySpeed] = TypeInformation.of(classOf[EntitySpeed])
  }

  given serializationSchema: SerializationSchema[EntitySpeed] = writeToArray(_)

}

