package com.terrycode.radar.math

import java.time.Duration as JDuration
import scala.concurrent.duration.FiniteDuration

object TimeUtils {
  
  extension (d: JDuration) {
    def toHoursFractional: Float = d.toNanos / 3_600_000_000_000f
  }
  
  extension (d: FiniteDuration) {
    def toHoursFractional: Float = d.toNanos / 3_600_000_000_000f
  }
  
}
