package com.terrycode.radar.sim.model

import cats.effect.Async

import java.time.{Instant, Duration as JDuration}
import scala.concurrent.duration.FiniteDuration
import scala.jdk.DurationConverters.*

class VirtualClock[F[_] : Async](scaleFactor: Int) {
  val referenceTime: Instant = Instant.now()

  def currentScaledTime: (Instant, FiniteDuration) = {
    val durationSince = JDuration.between(referenceTime, Instant.now()).multipliedBy(scaleFactor)
    (referenceTime.plus(durationSince), durationSince.toScala)
  }

  def scaleElapsedDuration(duration: FiniteDuration): FiniteDuration = duration * scaleFactor

  def scaledSleep(duration: FiniteDuration): F[Unit] = Async[F].sleep(duration / scaleFactor)  
  
}
