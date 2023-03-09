package com.github.asadaguitar.projection

trait UnsafetyCodec[A, B] {
  def fromLeft(a: A): B
}

object UnsafetyCodec {
  def create[A, B](f: A => B): UnsafetyCodec[A, B] = (a: A) => f(a)
  def apply[A, B](implicit codec: UnsafetyCodec[A, B]): UnsafetyCodec[A, B] =
    codec
}
