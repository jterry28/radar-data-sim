package com.terrycode.radar.sim.model

import java.time.{Instant, Duration as JDuration}
import scala.concurrent.duration.FiniteDuration
import scala.jdk.DurationConverters.*

class VirtualClock(scaleFactor: Int) {
  val referenceTime: Instant = Instant.now()

  def currentScaledTime: (Instant, FiniteDuration) = {
    val durationSince = JDuration.between(referenceTime, Instant.now()).multipliedBy(scaleFactor)
    (referenceTime.plus(durationSince), durationSince.toScala)
  }

  def scaledDuration(duration: FiniteDuration): FiniteDuration = duration * scaleFactor

}
