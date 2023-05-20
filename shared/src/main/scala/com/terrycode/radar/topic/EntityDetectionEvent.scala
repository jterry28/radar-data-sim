package com.terrycode.radar.topic

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema}
import org.apache.flink.api.common.typeinfo.TypeInformation

import java.time.LocalDateTime

final case class EntityDetectionEvent(radarId: String, capturedTime: LocalDateTime, entityName: String, lat: Float, lon: Float)

object EntityDetectionEvent {
  given codec: JsonValueCodec[EntityDetectionEvent] = JsonCodecMaker.make[EntityDetectionEvent]

  given deserializationSchema: DeserializationSchema[EntityDetectionEvent] = new {
    override def deserialize(message: Array[Byte]): EntityDetectionEvent = readFromArray(message)

    override def isEndOfStream(nextElement: EntityDetectionEvent): Boolean = false

    override def getProducedType: TypeInformation[EntityDetectionEvent] = TypeInformation.of(classOf[EntityDetectionEvent])
  }

  given serializationSchema: SerializationSchema[EntityDetectionEvent] = writeToArray(_)

}
