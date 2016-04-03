package se.gigurra.leavu3.windowstweaks

import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.serviceutils.twitter.logging.Logging

/**
  * Created by kjolh on 3/17/2016.
  */
object WindowTweaks extends Logging {

  val displayClass = Class.forName("org.lwjgl.opengl.Display")
  val windowsClass = Class.forName("org.lwjgl.opengl.WindowsDisplay")
  val display = displayClass.reflectField("display_impl")

  case class Rect(x: Int, y: Int, width: Int, height: Int)

  def apply(configuration: Configuration): Unit = {

    if (configuration.noFocusOnClick)
      setNeverCaptureFocus()

    if (configuration.alwaysOnTop)
      setAlwaysOnTop()
  }

  implicit class RichClass(cls: Class[_]) {

    def reflectField(name: String, o: Object = null): Object = {
      val field = cls.getDeclaredField(name)
      field.setAccessible(true)
      field.get(o)
    }

    def reflectGetter(name: String, o: Object = null): Object = {
      val method = cls.getDeclaredMethod(name)
      method.setAccessible(true)
      method.invoke(o)
    }

  }

  implicit class RichObjecy(o: Object) {
    def reflectField(name: String):Object = {
      o.getClass.reflectField(name, o)
    }

    def reflectGetter(name: String): Object = {
      o.getClass.reflectGetter(name, o)
    }

  }

  def getWindowPosition: Rect = {
    Rect(
      display.reflectGetter("getX").asInstanceOf[Int],
      display.reflectGetter("getY").asInstanceOf[Int],
      display.reflectGetter("getWidth").asInstanceOf[Int],
      display.reflectGetter("getHeight").asInstanceOf[Int]
    )
  }

  def setNeverCaptureFocus(on: Boolean = true): Unit = {

    if (windowsClass.isAssignableFrom(display.getClass)) {

      val GWL_EXSTYLE = -20
      val WS_EX_NOACTIVATE = 0x08000000L

      val getWindowLongMethod = display.getClass.getDeclaredMethods.find(_.getName == "getWindowLongPtr").get
      val setWindowLongMethod = display.getClass.getDeclaredMethods.find(_.getName == "setWindowLongPtr").get

      getWindowLongMethod.setAccessible(true)
      setWindowLongMethod.setAccessible(true)

      val hwnd = display.reflectField("hwnd").asInstanceOf[Long]

      def setWindowLong(index: Int, value: Long): Unit = setWindowLongMethod.invoke(display, hwnd: java.lang.Long, index : java.lang.Integer, value : java.lang.Long)
      def getWindowLong(index: Int): Long = getWindowLongMethod.invoke(display, hwnd: java.lang.Long, index: java.lang.Integer).asInstanceOf[Long]

      val prevStyle = getWindowLong(GWL_EXSTYLE)
      val withStyle = prevStyle | WS_EX_NOACTIVATE
      val withoutStyle = withStyle - WS_EX_NOACTIVATE

      setWindowLong(GWL_EXSTYLE, if (on) withStyle else withoutStyle)

    } else {
      logger.warning(s"Setting configuration.noFocusOnClick unavailable on this operating system!")
    }
  }

  def setAlwaysOnTop(on: Boolean = true): Unit = {

    if (windowsClass.isAssignableFrom(display.getClass)) {

      val SWP_FRAMECHANGED = 0x0020L
      val HWND_TOPMOST = -1L
      val HWND_BOTTOM = 1L

      val hwnd = display.reflectField("hwnd").asInstanceOf[Long]
      val windowPos = getWindowPosition

      val setWindowPosMethod = display.getClass.getDeclaredMethods.find(_.getName == "setWindowPos").get
      setWindowPosMethod.setAccessible(true)
      setWindowPosMethod.invoke(
        display,
        hwnd: java.lang.Long,
        (if (on) HWND_TOPMOST else HWND_BOTTOM): java.lang.Long,
        windowPos.x: java.lang.Integer,
        windowPos.y: java.lang.Integer,
        windowPos.width: java.lang.Integer,
        windowPos.height: java.lang.Integer,
        SWP_FRAMECHANGED: java.lang.Long
      )

    } else {
      logger.warning(s"Setting configuration.alwaysOnTop unavailable on this operating system!")
    }
  }

}
