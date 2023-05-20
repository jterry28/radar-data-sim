package com.terrycode.radar.processing

import com.terrycode.radar.math.TimeUtils.*
import com.terrycode.radar.math.HaversineFormulae
import com.terrycode.radar.topic.{EntityDetectionEvent, EntitySpeed}
import com.typesafe.scalalogging.Logger
import fs2.Stream
import org.apache.flink.api.common.accumulators.ListAccumulator
import org.apache.flink.api.common.functions.AggregateFunction

import java.time.{LocalDateTime, Duration as JDuration}
import scala.jdk.CollectionConverters.*

type Distance = Float
final class KnotsAggregator extends AggregateFunction[EntityDetectionEvent, ListAccumulator[(EntityDetectionEvent, Distance)], EntitySpeed] {

  override def createAccumulator(): ListAccumulator[(EntityDetectionEvent, Distance)] =
    ListAccumulator[(EntityDetectionEvent, Distance)]

  override def add(value      : EntityDetectionEvent,
                   accumulator: ListAccumulator[(EntityDetectionEvent, Distance)]): ListAccumulator[(EntityDetectionEvent, Distance)] = {
    if (accumulator.getLocalValue.isEmpty) {
      accumulator.add((value, 0.0f))
    } else {
      val (e1, _)  = accumulator.getLocalValue.asScala.last
      val e2       = value
      val distance = HaversineFormulae.calculateDistanceNauticalMiles(e1.lat, e1.lon, e2.lat, e2.lon)
      accumulator.add((value, distance))
    }
    accumulator
  }

  override def getResult(accumulator: ListAccumulator[(EntityDetectionEvent, Distance)]): EntitySpeed = {
    val list = accumulator.getLocalValue.asScala
    if (list.isEmpty) return EntitySpeed("", LocalDateTime.MIN, 0.0f)
    val (e1, d1) = list.head
    if (list.length == 1) return EntitySpeed(e1.entityName, e1.capturedTime, d1)

    val totalDistance = Stream
      .emits(list)
      .drop(1) // First element distance should be ignored, as it is distance from previous point
      .map(_._2)
      .reduce(_ + _).toList.head

    val (e2, _)    = list.last
    val duration   = JDuration.between(e1.capturedTime, e2.capturedTime).toHoursFractional
    val nmPerHours = totalDistance / duration
    EntitySpeed(e2.entityName, e2.capturedTime, nmPerHours)
  }

  override def merge(a: ListAccumulator[(EntityDetectionEvent, Distance)],
                     b: ListAccumulator[(EntityDetectionEvent, Distance)]): ListAccumulator[(EntityDetectionEvent, Distance)] = {
    val aList = a.getLocalValue.asScala
    val bList = b.getLocalValue.asScala
    if (aList.nonEmpty && bList.nonEmpty) {
      val (e1, _)  = aList.last
      val (e2, _)  = bList.head
      val distance = HaversineFormulae.calculateDistanceNauticalMiles(e1.lat, e1.lon, e2.lat, e2.lon)
      bList.insert(0, (e2, distance))
    }
    a.merge(b)
    a
  }
}