package com.github.asadaguitar.projection

import cats.effect.IO

trait ProjectionHandler[A <: Event] {

  def run(events: List[A]): IO[Unit]
}
