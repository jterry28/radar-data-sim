package com.terrycode.radar.processing

import cats.effect.unsafe.implicits.global
import cats.effect.{ExitCode, IO, IOApp}
import com.terrycode.radar.math.HaversineFormulae
import com.terrycode.radar.topic.{EntityDetectionEvent, EntityDetectionEventTopic, EntitySpeed, EntitySpeedTopic}
import com.typesafe.scalalogging.Logger
import fs2.kafka.{AdminClientSettings, KafkaAdminClient}
import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.common.accumulators.{AverageAccumulator, ListAccumulator}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.configuration.{Configuration, JobManagerOptions, RestOptions}
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.datastream.{DataStream, SingleOutputStreamOperator}
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.TopicExistsException

import java.lang
import java.nio.charset.{Charset, StandardCharsets}
import java.time.{LocalDateTime, ZoneOffset, Duration as JDuration}
import scala.concurrent.duration.*
import scala.jdk.DurationConverters.*

object SubmitTasks {

  @main
  def run(): JobExecutionResult = {
    val kafkaBootstrapServer   = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    val flinkJobManagerAddress = sys.env.getOrElse("FLINK_JOB_MANAGER_ADDRESS", "localhost")
    val flinkJobManagerPort    = sys.env.getOrElse("FLINK_JOB_MANAGER_PORT", "8081")

    KafkaAdminClient.resource[IO](AdminClientSettings(kafkaBootstrapServer))
      .use(c => c.createTopic(EntitySpeedTopic.topicProperties))
      .recover {
        case _: TopicExistsException => IO.unit
      }.unsafeRunSync()

    val conf = Configuration()
    conf.setString(JobManagerOptions.ADDRESS, flinkJobManagerAddress)
    conf.setInteger(RestOptions.PORT, flinkJobManagerPort.toInt)
    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)

    val source = KafkaSource.builder[EntityDetectionEvent]
      .setBootstrapServers(kafkaBootstrapServer)
      .setTopics(EntityDetectionEventTopic.name)
      .setGroupId("calc_knots")
      .setStartingOffsets(OffsetsInitializer.earliest)
      .setValueOnlyDeserializer(EntityDetectionEvent.deserializationSchema)
      .build

    val watermarkStrategy = WatermarkStrategy
      .forBoundedOutOfOrderness[EntityDetectionEvent](10.seconds.toJava)
      .withTimestampAssigner((event, _) => event.capturedTime.toInstant(ZoneOffset.UTC).toEpochMilli)

    val stream: DataStream[EntitySpeed] = env
      .fromSource(source, watermarkStrategy, "kafka")
      .keyBy(_.entityName)
      .window(SlidingEventTimeWindows.of(Time.seconds(20), Time.seconds(10)))
      .aggregate(KnotsAggregator())

    val sink = KafkaSink.builder[EntitySpeed]
      .setBootstrapServers(kafkaBootstrapServer)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder[EntitySpeed]
                             .setTopic(EntitySpeedTopic.name)
                             .setKeySerializationSchema(_.entityName.getBytes(StandardCharsets.UTF_8))
                             .setValueSerializationSchema(EntitySpeed.serializationSchema)
                             .build)
      .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
      .setProperty("transaction.timeout.ms", "900000")
      .build

    stream.sinkTo(sink)

    env.execute()
  }
}
