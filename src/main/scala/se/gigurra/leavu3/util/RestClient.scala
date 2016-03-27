package se.gigurra.leavu3.util

import java.net.InetAddress

import com.twitter.finagle.Http
import com.twitter.finagle.http._
import com.twitter.util.{JavaTimer, _}
import se.gigurra.serviceutils.twitter.service.ServiceException

import scala.collection.mutable

/**
  * Created by kjolh on 3/10/2016.
  */
case class RestClient(addr: String, port: Int, name: String)(implicit val timer: JavaTimer = DefaultTimer.underlying) {

  // Check valid address first
  try InetAddress.getByName(addr) catch {
    case NonFatal(e) => throw new RuntimeException(s"Unable to connect to $name @ $addr:$port", e)
  }

  private val client = Http.client.newService(s"$addr:$port")
  private val pending = new mutable.HashMap[String, Unit]()
  private val timeout = Duration.fromSeconds(3)

  def withNewRemote(addr: String, port: Int): RestClient = {
    RestClient(addr, port, name)(timer)
  }

  def get(path: String, maxAge: Option[Long] = None): Future[String] = {
    doIfNotAlreadyPending(path)(doGet(path, maxAge))
  }

  def put(path: String)(data: => String): Future[Unit] = {
    doIfNotAlreadyPending(path)(doPut(path, data))
  }

  def post(path: String)(data: => String): Future[Unit] = {
    doIfNotAlreadyPending(path)(doPost(path, data))
  }

  private def doIfNotAlreadyPending[T](id: String)(f: => Future[T]): Future[T] = synchronized {
    pending.put(id, ()) match {
      case Some(prev) => Future.exception(IdenticalRequestPending(id))
      case None => f.respond (_ => removePending(id)) // done synchronized
    }
  }

  private def removePending(id: String): Unit = synchronized {
    pending.remove(id)
  }

  private def doGet(path: String, cacheMaxAgeMillis: Option[Long] = None): Future[String] = {
    val params = cacheMaxAgeMillis.toSeq.map(x => "max_cached_age" -> x.toString)
    val request = Request(path, params:_*)
    client.apply(request).raiseWithin(timeout).flatMap {
      case OkResponse(response)  => Future.value(response.contentString)
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }

  private def doPost(path: String, data: String): Future[Unit] = {
    val request = Request(Version.Http11, Method.Post, path)
    request.setContentString(data)
    client.apply(request).raiseWithin(timeout).flatMap {
      case OkResponse(response)  => Future.Unit
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }

  private def doPut(path: String, data: String): Future[Unit] = {
    val request = Request(Version.Http11, Method.Put, path)
    request.setContentString(data)
    client.apply(request).raiseWithin(timeout).flatMap {
      case OkResponse(response)  => Future.Unit
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }
}

object OkResponse {
  def unapply(response: Response): Option[Response] = response.status match {
    case Status.Ok  => Some(response)
    case _          => None
  }
}

object NotFound {
  def unapply(response: Response): Option[Response] = response.status match {
    case Status.NotFound  => Some(response)
    case _                => None
  }
}

object BadResponse {
  def unapply(response: Response): Option[Response] = response.status match {
    case Status.Ok  => None
    case _          => Some(response)
  }
}

case class IdenticalRequestPending(id: String) extends RuntimeException(s"Identical request to id $id already pending")
