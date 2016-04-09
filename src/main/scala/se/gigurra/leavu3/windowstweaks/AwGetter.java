package se.gigurra.leavu3.windowstweaks;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Created by kjolh on 4/9/2016.
 */
public interface AwGetter extends StdCallLibrary, WinUser, WinNT {

    AwGetter INSTANCE = (AwGetter)Native.loadLibrary(
                "user32",
                AwGetter.class,
                W32APIOptions.DEFAULT_OPTIONS);

    WinDef.HWND GetActiveWindow();
}
