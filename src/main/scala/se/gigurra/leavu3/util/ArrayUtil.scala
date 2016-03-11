package se.gigurra.leavu3.util

object ArrayUtil {
  def merge(arrays: Iterable[Array[Float]]): Array[Float] = {

    if (arrays.isEmpty)
      return Array[Float]()

    if (arrays.size == 1)
      return arrays.head

    val out = new Array[Float](arrays.foldLeft(0)(_ + _.length))
    var offs = 0
    for (array <- arrays) {
      System.arraycopy(array, 0, out, offs, array.length)
      offs += array.length
    }
    out
  }
}
