package net.hogerheijde.taxonomystore.server.util

import java.time.Duration

object Timer {

  case class TimedResult[T](value: T, duration: Duration) {
    override def toString: String = s"$value, took ${duration}"
  }
  def apply[T](f: => T): TimedResult[T] = {
    val start = System.nanoTime
    val t = f
    val end = System.nanoTime
    TimedResult(t, Duration.ofNanos(end - start))
  }

}