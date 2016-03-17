package se.gigurra.leavu3.keyinput


case class LLWindowsKeyboardEvent(wparInt: Int, flags: Int, scanCode: Int, vkCode: Int)
/*
import com.sun.jna.platform.win32.{Kernel32, User32}
import com.sun.jna.platform.win32.WinDef.{LRESULT, WPARAM}
import com.sun.jna.platform.win32.WinUser.{KBDLLHOOKSTRUCT, LowLevelKeyboardProc, MSG, WH_KEYBOARD_LL}

object KeyInput {

  val VK_SHIFT = 0x10
  val VK_CONTROL = 0x11
  val VK_ALT = 0x12

  def isShiftDown: Boolean = isKeyDown(VK_SHIFT)
  def isControlDown: Boolean = isKeyDown(VK_CONTROL)
  def isAltDown: Boolean = isKeyDown(VK_ALT)

  def isKeyDown(key: Int): Boolean = {
    (User32.INSTANCE.GetAsyncKeyState(key) & 0x8000) != 0
  }

  def enterKeyboardHookMessageLoop(listener: LLWindowsKeyboardEvent => Unit): Unit = {

    val lpfn = new LowLevelKeyboardProc {
      override def callback(nCode: Int, wPar: WPARAM, lp: KBDLLHOOKSTRUCT): LRESULT = {
        val event = LLWindowsKeyboardEvent(wPar.intValue, lp.flags, lp.scanCode, lp.vkCode)
        listener.apply(event)
        User32.INSTANCE.CallNextHookEx(null, nCode, wPar, lp.getPointer)
      }
    }

    val hModule = Kernel32.INSTANCE.GetModuleHandle(null)
    val hHook = User32.INSTANCE.SetWindowsHookEx(WH_KEYBOARD_LL, lpfn, hModule, 0)
    if (hHook == null) {
      System.err.println("Failed to create keyboard hook, bailing!")
      System.exit(1)
    }

    val msg = new MSG()
    var quit = false
    while (!quit) {
      val result = User32.INSTANCE.GetMessage(msg, null, 0, 0)
      if (result == -1 || result == 0) {
        System.out.println("Exiting, GetMessage returned " + result)
        quit = true
      } else {
        User32.INSTANCE.TranslateMessage(msg)
        User32.INSTANCE.DispatchMessage(msg)
      }
      Thread.sleep(1)
    }

    if (User32.INSTANCE.UnhookWindowsHookEx(hHook)) {
      System.out.println("Unhooked")
    }
  }

}
*/