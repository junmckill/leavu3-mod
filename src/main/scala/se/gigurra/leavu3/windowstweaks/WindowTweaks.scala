package se.gigurra.leavu3.windowstweaks

import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.util.JavaReflectImplicits
import se.gigurra.serviceutils.twitter.logging.Logging

/**
  * Created by kjolh on 3/17/2016.
  */
object WindowTweaks extends Logging with JavaReflectImplicits {

  val displayClass = Class.forName("org.lwjgl.opengl.Display")
  val display = displayClass.reflectField("display_impl")

  var extraWidth: Int = 16 // Default on windows
  var extraHeight: Int = 39 // Default on windows

  case class Rect(x: Int, y: Int, width: Int, height: Int)

  def apply(configuration: Configuration): Unit = {

    if (configuration.noFocusOnClick)
      setNeverCaptureFocus()

    if (configuration.alwaysOnTop)
      setAlwaysOnTop()
  }

  def isPlatformWindows: Boolean = {
    display.getClass.getName == "org.lwjgl.opengl.WindowsDisplay"
  }

  def getWindowPosition: Rect = {
    Rect(
      display.reflectGetter("getX").asInstanceOf[Int],
      display.reflectGetter("getY").asInstanceOf[Int],
      display.reflectGetter("getWidth").asInstanceOf[Int] + extraWidth,
      display.reflectGetter("getHeight").asInstanceOf[Int] + extraHeight
    )
  }

  def setNeverCaptureFocus(on: Boolean = true): Unit = {

    if (isPlatformWindows) {

      val GWL_EXSTYLE = -20
      val WS_EX_NOACTIVATE = 0x08000000L

      val hwnd = display.reflectField("hwnd").asInstanceOf[Long]

      def setWindowLong(index: Int, value: Long): Unit = display.reflectInvoke("setWindowLongPtr", hwnd: java.lang.Long, index : java.lang.Integer, value : java.lang.Long) //setWindowLongMethod.invoke(display, hwnd: java.lang.Long, index : java.lang.Integer, value : java.lang.Long)
      def getWindowLong(index: Int): Long = display.reflectInvoke("getWindowLongPtr", hwnd: java.lang.Long, index: java.lang.Integer).asInstanceOf[Long]

      val prevStyle = getWindowLong(GWL_EXSTYLE)
      val withStyle = prevStyle | WS_EX_NOACTIVATE
      val withoutStyle = withStyle - WS_EX_NOACTIVATE

      setWindowLong(GWL_EXSTYLE, if (on) withStyle else withoutStyle)

    } else {
      logger.warning(s"Setting configuration.noFocusOnClick unavailable on this operating system!")
    }
  }

  def setAlwaysOnTop(on: Boolean = true): Unit = {

    if (isPlatformWindows) {

      val SWP_FRAMECHANGED = 0x0020L
      val HWND_TOPMOST = -1L
      val HWND_BOTTOM = 1L

      val hwnd = display.reflectField("hwnd").asInstanceOf[Long]
      val oldWindowPos = getWindowPosition

      def invoke() = display.reflectInvoke(
        "setWindowPos",
        hwnd: java.lang.Long,
        (if (on) HWND_TOPMOST else HWND_BOTTOM): java.lang.Long,
        oldWindowPos.x: java.lang.Integer,
        oldWindowPos.y: java.lang.Integer,
        oldWindowPos.width: java.lang.Integer,
        oldWindowPos.height: java.lang.Integer,
        SWP_FRAMECHANGED: java.lang.Long
      )

      invoke()

      // Ensure that the window actually gets the desired size, otherwise, adjust
      val newWindowPos = getWindowPosition
      if (newWindowPos != oldWindowPos) {
        extraWidth = oldWindowPos.width - newWindowPos.width
        extraHeight = oldWindowPos.height - newWindowPos.height
        invoke()
      }

    } else {
      logger.warning(s"Setting configuration.alwaysOnTop unavailable on this operating system!")
    }
  }

}
