package se.gigurra.leavu3.util

import se.gigurra.leavu3.datamodel.Contact

import scala.collection.mutable
import scala.language.implicitConversions

/**
  * Created by kjolh on 3/31/2016.
  */
abstract class Memory[T](timeoutSeconds: Double) {

  protected def idOf(t: T): String

  private val data = new mutable.HashMap[String, Memorized[T]]

  def update(ts: Seq[T]): Seq[Memorized[T]] = synchronized {
    clearExpired()
    ts foreach update
    all
  }

  def get(t: T): Option[Memorized[T]] = synchronized {
    data.get(idOf(t))
  }

  private def all: Seq[Memorized[T]] = {
    data.values.toArray.toSeq
  }

  private def update(t: T): Unit = {
    val id = idOf(t)
    data += id -> Memorized(t, timeoutSeconds, CurTime.seconds)
  }

  private def clearExpired(): Unit = {
    data --= data.filter(_._2.expired).keys
  }
}

case class Memorized[T](t: T, timeoutSeconds: Double, timestamp: Double) {
  def age = CurTime.seconds - timestamp
  def expired = age > timeoutSeconds
  def news: Double = math.max(0.0, 1.0 - age / timeoutSeconds)
}

object Memorized {
  implicit def stored2T[T](st: Memorized[T]): T = st.t
}

case class ContactMemory(timeoutSeconds: Double = 10.0) extends Memory[Contact](timeoutSeconds) {
  override protected def idOf(t: Contact): String = t.id.toString
}
