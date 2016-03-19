package se.gigurra.leavu3.util

/**
  * Created by kjolh on 3/19/2016.
  */
case class Eventually[T]() {
  private var item = null.asInstanceOf[T]
  def get(factory: => T): T = synchronized {
    if (item == null)
      item = factory
    item
  }
}
