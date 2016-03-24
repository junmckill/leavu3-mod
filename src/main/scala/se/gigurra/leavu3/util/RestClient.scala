package se.gigurra.leavu3.util

import java.net.InetAddress

import com.twitter.finagle.Http
import com.twitter.finagle.http._
import com.twitter.util._
import se.gigurra.serviceutils.twitter.service.ServiceException

/**
  * Created by kjolh on 3/10/2016.
  */
case class RestClient(addr: String, port: Int)(implicit val timer: JavaTimer = new JavaTimer(isDaemon = true)) {

  // Check valid address first
  InetAddress.getByName(addr)

  private val client = Http.client.newService(s"$addr:$port")

  def close(): Unit = {
    timer.stop()
  }

  def get(path: String, cacheMaxAgeMillis: Option[Long] = None, timeout: Duration = Duration.fromSeconds(3)): Future[String] = {
    val params = cacheMaxAgeMillis.toSeq.map(x =>"max_cached_age" -> x.toString)
    val request = Request(path, params:_*)
    client.apply(request).raiseWithin(timeout).flatMap {
      case OkResponse(response)  => Future.value(response.contentString)
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }

  def getBlocking(path: String, cacheMaxAgeMillis: Option[Long] = None, timeout: Duration = Duration.fromSeconds(3)): String = {
    Await.result(get(path, cacheMaxAgeMillis, timeout))
  }

  def post(path: String, data: String): Future[Unit] = {
    val request = Request(Version.Http11, Method.Post, path)
    request.setContentString(data)
    client.apply(request) flatMap {
      case OkResponse(response)  => Future.Unit
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }

  def postBlocking(path: String, data: String, timeout: Duration = Duration.fromSeconds(3)): Unit = {
    Await.result(post(path, data), timeout)
  }

  def put(path: String, data: String): Future[Unit] = {
    val request = Request(Version.Http11, Method.Put, path)
    request.setContentString(data)
    client.apply(request) flatMap {
      case OkResponse(response)  => Future.Unit
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }

  def putBlocking(path: String, data: String, timeout: Duration = Duration.fromSeconds(3)): Unit = {
    Await.result(put(path, data), timeout)
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

