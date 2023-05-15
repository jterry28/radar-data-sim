package com.terrycode.radar.sim.model

import com.terrycode.radar.sim.model.BoundingBox
import com.terrycode.radar.sim.model.FlyingObject.*

import java.time.{Duration, Instant}
import java.util.UUID
import scala.util.Random

case class FlyingObject(name           : String,
                        initializedTime: Instant,
                        initialLat     : Float,
                        initialLon     : Float,
                        altitude       : Float,
                        knots          : Float,
                        courseDeg      : Float) {

  def getCurrentPosition(currentTime: Instant): (Float, Float) = {
    val hoursTraveled = Duration.between(initializedTime, currentTime).getSeconds / 3600f
    val bearing       = Math.toRadians(courseDeg)
    val distance      = knots * hoursTraveled * kmPerNM

    // Haversine Formula
    val lat  = Math.toRadians(initialLat)
    val lon  = Math.toRadians(initialLon)
    val lat2 = Math.toDegrees(Math.asin(Math.sin(lat) * Math.cos(distance / radius) + Math.cos(Math.toRadians(lat))
      * Math.sin(distance / radius) * Math.cos(bearing)))
    val lon2 = Math.toDegrees(lon + Math.atan2(Math.sin(bearing) * Math.sin(distance / radius) * Math.cos(lat),
                                               Math.cos(distance / radius) - Math.sin(lat) * Math.sin(lat2)))
    (lat2.toFloat, lon2.toFloat)
  }

}

object FlyingObject {
  private val radius  = 6378.1370 //Radius of the Earth
  private val kmPerNM = 1.852

  def withinBounds(box: BoundingBox, initializedTime: Instant): FlyingObject = {
    FlyingObject(UUID.randomUUID().toString,
                 initializedTime,
                 Random.between(box.minLat, box.maxLat),
                 Random.between(box.minLon, box.maxLon),
                 Random.between(500, 160000), // Typical altitudes of various flying objects
                 Random.between(25, 2000), // Typical speeds
                 Random.between(0, 360)) // Possible course in degrees
  }
}
