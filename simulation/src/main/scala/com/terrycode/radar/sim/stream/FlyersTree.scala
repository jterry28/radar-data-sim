package com.terrycode.radar.sim.stream

import cats.effect.kernel.Temporal
import cats.effect.Ref
import com.github.plokhotnyuk.rtree2d.core.*
import com.github.plokhotnyuk.rtree2d.core.SphericalEarth.*
import com.terrycode.radar.sim.model.*
import fs2.*

import scala.concurrent.duration.*

final class FlyersTree[F[_] : Temporal](clock: VirtualClock[F], numFlyers: Int) {

  private val flyers = (0 until numFlyers).view
    .map(_ => FlyingEntity.withinBounds(BoundingBox.US_LOWER_48, clock.referenceTime))
    .toArray

  val treeRef: F[Ref[F, RTree[FlyingEntity]]] = Ref.of(RTree(flyers.view.map(f => entry(f.initialLat, f.initialLon, f))))

  def makeStream: Stream[F, Unit] = {
    Stream.eval(treeRef)
      .flatMap { ref =>
        Stream.awakeEvery[F](1.seconds)
          .map(clock.scaleElapsedDuration)
          .evalMap { d =>
            ref.update { _ =>
              RTree {
                flyers.view.map { f =>
                  val (lat, lon) = f.currentPosition(d)
                  entry(lat, lon, f)
                }
              }
            }
          }
      }
  }
}
