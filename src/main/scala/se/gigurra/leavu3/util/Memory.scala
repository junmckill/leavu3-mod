package se.gigurra.leavu3.util

import java.util.concurrent.TimeUnit

import com.google.common.cache.CacheBuilder
import se.gigurra.leavu3.datamodel.Contact
import scala.language.implicitConversions
import scala.collection.JavaConversions._

/**
  * Created by kjolh on 3/31/2016.
  */
abstract class Memory[T](timeoutSeconds: Double) {

  protected def idOf(t: T): String

  private val data = CacheBuilder.newBuilder().expireAfterWrite((timeoutSeconds * 1000.0).toLong, TimeUnit.MILLISECONDS).build[String, Memorized[T]]() //  mutable.HashMap[String, Memorized[T]]

  def update(ts: Seq[T]): Unit = {
    ts foreach update
  }

  def get(t: T): Option[Memorized[T]] = {
    Option(data.getIfPresent(idOf(t)))
  }

  def all: Seq[Memorized[T]] = {
    data.asMap().values().filter(_ => true).toSeq
  }

  protected def update(t: T, gameTimeStamp: Double): Unit = {
    data.put(idOf(t), Memorized(t, timeoutSeconds, gameTimeStamp, CurTime.seconds))
  }

  protected def update(t: T): Unit = {
    update(t, CurTime.seconds)
  }
}

case class Memorized[T](t: T, timeoutSeconds: Double, gameTimestamp: Double, timeoutTimestamp: Double) {
  def age = CurTime.seconds - gameTimestamp
  def timeoutAge = CurTime.seconds - timeoutTimestamp
  def expired = timeoutAge > timeoutSeconds
  def hidden = age > timeoutSeconds
  def news: Double = math.max(0.0, 1.0 - age / timeoutSeconds)
}

object Memorized {
  implicit def stored2T[T](st: Memorized[T]): T = st.t
}

case class ContactMemory(timeoutSeconds: Double = 10.0) extends Memory[Contact](timeoutSeconds) {
  override protected def idOf(t: Contact): String = t.id.toString
}

class PositionChangeMemory(timeoutSeconds: Double = 10.0) extends ContactMemory(timeoutSeconds) {
  protected override def update(t: Contact): Unit = {
    get(t) match {
      case Some(prevContact) if prevContact.position == t.position && t.velocity.norm > 0.01 =>
        super.update(t, gameTimeStamp = prevContact.gameTimestamp)
      case _ =>
        super.update(t)
    }
  }
}
