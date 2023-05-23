package com.terrycode.radar.processing

import com.terrycode.radar.math.HaversineFormulae
import com.terrycode.radar.math.TimeUtils.*
import com.terrycode.radar.topic.{EntityDetectionEvent, EntitySpeed}
import com.typesafe.scalalogging.Logger
import fs2.Stream
import org.apache.flink.api.common.accumulators.ListAccumulator
import org.apache.flink.api.common.functions.AggregateFunction

import java.time.{LocalDateTime, Duration as JDuration}
import java.util.Optional
import scala.jdk.CollectionConverters.*

final class KnotsAggregator extends AggregateFunction[EntityDetectionEvent, ListAccumulator[EntityDetectionEvent], Optional[EntitySpeed]] {
  private val log = Logger[KnotsAggregator]
  override def createAccumulator(): ListAccumulator[EntityDetectionEvent] =
    ListAccumulator[EntityDetectionEvent]

  override def add(value      : EntityDetectionEvent,
                   accumulator: ListAccumulator[EntityDetectionEvent]): ListAccumulator[EntityDetectionEvent] = {
    accumulator.add(value)
    accumulator
  }

  override def getResult(accumulator: ListAccumulator[EntityDetectionEvent]): Optional[EntitySpeed] = {
    if (accumulator.getLocalValue.isEmpty || accumulator.getLocalValue.size() == 1) return Optional.empty()
    val list = accumulator.getLocalValue.asScala.sortBy(_.capturedTime)
    val totalDistance = Stream
      .emits(list)
      .sliding(2)
      .map(c => {
        val e1 = c.head.get
        val e2 = c.last.get
        HaversineFormulae.calculateDistanceNauticalMiles(e1.lat, e1.lon, e2.lat, e2.lon)
      })
      .reduce(_ + _).toList.head

    val e1         = list.head
    val e2         = list.last
    val duration   = JDuration.between(e1.capturedTime, e2.capturedTime).toHoursFractional
    val nmPerHours = totalDistance / duration

    val capturedSites = list.map(_.radarId).distinct.toArray

    if (nmPerHours < 0.0) {
      log.warn(s"Invalid speed: $nmPerHours, $duration, $list")
    }
    Optional.of(EntitySpeed(e2.entityName, e2.capturedTime, nmPerHours, capturedSites))
  }

  override def merge(a: ListAccumulator[EntityDetectionEvent],
                     b: ListAccumulator[EntityDetectionEvent]): ListAccumulator[EntityDetectionEvent] = {
    a.merge(b)
    a
  }
}