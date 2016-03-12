package se.gigurra.leavu3.util

/**
  * Created by kjolh on 3/11/2016.
  */
object Resource2String {
  def apply(path: String, enc: String = "UTF-8"): String = {

    val url = getClass.getClassLoader.getResource(path).toURI
    if (url == null) {
      throw new RuntimeException("Resource not found: " + path)
    } else {
      scala.io.Source.fromFile(url, enc).mkString
     }
  }
}
