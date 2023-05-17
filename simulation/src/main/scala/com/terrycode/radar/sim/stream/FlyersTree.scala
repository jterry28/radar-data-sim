package com.terrycode.radar.sim.stream

import cats.Parallel
import cats.effect.kernel.{Concurrent, Ref, Temporal}
import com.github.plokhotnyuk.rtree2d.core.*
import com.github.plokhotnyuk.rtree2d.core.SphericalEarth.*
import com.terrycode.radar.sim.model.*
import fs2.*

import scala.concurrent.duration.*

final class FlyersTree[F[_] : Concurrent](clock: VirtualClock, numFlyers: Int) {

  private val flyers = (0 to numFlyers).view
    .map(_ => FlyingObject.withinBounds(BoundingBox.US_LOWER_48, clock.referenceTime))
    .toArray

  val treeRef: F[Ref[F, RTree[FlyingObject]]] = Ref.of(RTree(flyers.view.map(f => entry(f.initialLat, f.initialLon, f))))

  def makeStream(implicit ev: Temporal[F]): Stream[F, Unit] = {
    Stream.eval(treeRef)
      .flatMap { ref =>
        Stream.awakeEvery[F](1.seconds)
          .map(clock.scaledDuration)
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
