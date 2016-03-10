package se.gigurra.leavu3.util

import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.{Future, Duration, Await}

/**
  * Created by kjolh on 3/10/2016.
  */
case class RestClient(addr: String, port: Int) {

  private val client = Http.client.newService(s"$addr:$port")

  def poll(path: String): Future[String] = {
    val request = Request(path)
    client.apply(request).map(_.contentString)
  }

  def pollBlocking(path: String, timeout: Duration = Duration.fromSeconds(3)): String = {
    Await.result(poll(path), timeout)
  }
}
