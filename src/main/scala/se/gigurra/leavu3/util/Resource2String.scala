package se.gigurra.leavu3.util

/**
  * Created by kjolh on 3/11/2016.
  */
object Resource2String {
  def apply(path: String, enc: String = "UTF-8"): String = {
    val stream = getClass.getClassLoader.getResourceAsStream(path)
    scala.io.Source.fromInputStream(stream, enc).mkString
  }
}
