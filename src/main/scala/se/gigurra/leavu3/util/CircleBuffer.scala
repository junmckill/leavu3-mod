package se.gigurra.leavu3.util

import scala.language.implicitConversions

/**
  * Created by kjolh on 3/12/2016.
  */
case class CircleBuffer[T](item0: T, rest: T*) {
  val items = Seq(item0) ++ rest.toSeq
  private var i = default
  require(default < size, "Cant set default < size")

  def withDefaultIndex(i: Int): CircleBuffer[T] = new CircleBuffer[T](items.head, items.tail:_*) {
    override def default = i
  }

  def withDefaultValue(t: T): CircleBuffer[T] = new CircleBuffer[T](items.head, items.tail:_*) {
    override def default = items.indexWhere(_ == t)
  }

  def default: Int = 0
  def stepUp(): Unit = i = (i + 1) % size
  def stepDown(): Unit = i = (size + i - 1) % size
  def reset(): Unit = i = default
  def current: T = items(i)
  def get = current
  def set(t: T): Unit = {
    require(items.contains(t), s"CircleBuffer[..]: Cannot set to value $t which isn't contained")
    i = items.indexOf(t)
  }
  def size = items.size
}

object CircleBuffer {
  implicit def bf2T[T](b: CircleBuffer[T]): T = b.get
}
