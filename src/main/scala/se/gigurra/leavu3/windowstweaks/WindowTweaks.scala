package se.gigurra.leavu3.windowstweaks

import se.gigurra.leavu3.Configuration
import se.gigurra.serviceutils.twitter.logging.Logging

/**
  * Created by kjolh on 3/17/2016.
  */
object WindowTweaks extends Logging {

  def apply(configuration: Configuration): Unit = {

    if (configuration.noFocusOnClick)
      setNeverCaptureFocus()

    if (configuration.alwaysOnTop)
      setAlwaysOnTop(configuration)
  }

  private def setNeverCaptureFocus(): Unit = {

    val displayClass = Class.forName("org.lwjgl.opengl.Display")
    val windowsClass = Class.forName("org.lwjgl.opengl.WindowsDisplay")
    val implField = displayClass.getDeclaredField("display_impl")
    implField.setAccessible(true)
    val display = implField.get(null)

    if (windowsClass.isAssignableFrom(display.getClass)) {
      val GWL_EXSTYLE = -20
      val WS_EX_NOACTIVATE = 0x08000000L
      val getWindowLongMethod = display.getClass.getDeclaredMethods.find(_.getName == "getWindowLongPtr").get
      val setWindowLongMethod = display.getClass.getDeclaredMethods.find(_.getName == "setWindowLongPtr").get
      getWindowLongMethod.setAccessible(true)
      setWindowLongMethod.setAccessible(true)

      val hwndField = display.getClass.getDeclaredField("hwnd")
      hwndField.setAccessible(true)
      val hwnd = hwndField.get(display).asInstanceOf[Long]

      def setWindowLong(index: Int, value: Long): Unit = setWindowLongMethod.invoke(display, hwnd: java.lang.Long, index : java.lang.Integer, value : java.lang.Long)
      def getWindowLong(index: Int): Long = getWindowLongMethod.invoke(display, hwnd: java.lang.Long, index: java.lang.Integer).asInstanceOf[Long]
      setWindowLong(GWL_EXSTYLE, getWindowLong(GWL_EXSTYLE) | WS_EX_NOACTIVATE)
    } else {
      logger.warning(s"Setting configuration.noFocusOnClick unavailable on this operating system!")
    }
  }

  private def setAlwaysOnTop(configuration: Configuration): Unit = {

    val displayClass = Class.forName("org.lwjgl.opengl.Display")
    val windowsClass = Class.forName("org.lwjgl.opengl.WindowsDisplay")
    val implField = displayClass.getDeclaredField("display_impl")
    implField.setAccessible(true)
    val display = implField.get(null)

    if (windowsClass.isAssignableFrom(display.getClass)) {

      val SWP_FRAMECHANGED = 0x0020L
      val HWND_TOPMOST = -1L

      val hwndField = display.getClass.getDeclaredField("hwnd")
      hwndField.setAccessible(true)
      val hwnd = hwndField.get(display).asInstanceOf[Long]

      val setWindowPosMethod = display.getClass.getDeclaredMethods.find(_.getName == "setWindowPos").get
      setWindowPosMethod.setAccessible(true)
      setWindowPosMethod.invoke(
        display,
        hwnd: java.lang.Long,
        HWND_TOPMOST: java.lang.Long,
        configuration.x: java.lang.Integer,
        configuration.y: java.lang.Integer,
        configuration.width: java.lang.Integer,
        configuration.height: java.lang.Integer,
        SWP_FRAMECHANGED: java.lang.Long
      )

    } else {
      logger.warning(s"Setting configuration.alwaysOnTop unavailable on this operating system!")
    }
  }

}
