package se.gigurra.leavu3.util

import com.google.common.collect.ConcurrentHashMultiset
import com.twitter.util.{Duration, Future}

/**
  * Created by kjolh on 4/6/2016.
  */
case class Throttler[T](maxConcurrentUsersPerResource: Int = 1) {

  private val pending = ConcurrentHashMultiset.create[String]()

  def access[T](path: String,
                minTimeDelta: Option[Duration])
               (f: => Future[T]): Future[T] = {

    val prevAccessCount = pending.add(path, 1)
    if (prevAccessCount >= maxConcurrentUsersPerResource) {
      pending.remove(path, 1)
      Future.exception(IdenticalRequestPending(path))
    } else {
      f.respond(_ => minTimeDelta match {
        case Some(minTime) => DefaultTimer.onceAfter(minTime)(pending.remove(path, 1))
        case None => pending.remove(path, 1)
      })
    }
  }
}
