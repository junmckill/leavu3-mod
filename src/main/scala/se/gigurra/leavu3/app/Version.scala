package se.gigurra.leavu3.app

import java.awt.Desktop
import java.net.URI

import com.twitter.util.{Duration, JavaTimer, Time}
import se.gigurra.leavu3.util.Resource2String
import se.gigurra.serviceutils.twitter.logging.Logging

import scala.language.implicitConversions
import scala.util.Try

/**
  * Created by kjolh on 3/20/2016.
  */
object Version extends Logging {

  val versionUrl = "http://build.culvertsoft.se/dcs/leavu3-version.txt"
  val downloadUrl = "http://build.culvertsoft.se/dcs/"
  var latest = Try(scala.io.Source.fromURL(versionUrl, "UTF-8").mkString).getOrElse("unknown")
  val current = Try(Resource2String("version.txt")).getOrElse("unknown")

  def downloadLatest(): Unit = {
    if(Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI(downloadUrl))
    } else {
      logger.error(s"Can't auto update - browser not supported!")
    }
  }

  override def toString: String = current

}
