package xyz.vadimszzz.okhttplogger;

import java.io.*;
import java.util.stream.Collectors; 
import de.robv.android.xposed.XposedBridge;


/* XposedBridge logger with prefix. */
public class XposedLogger {
    private String prefix;

    public XposedLogger(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Log to Xposed log. View through adb logcat.
     * 
     * @param String message        Message will be formatted if additional arguments passed.
     * @param Object[] objects      
     */
    public void log(String message, Object ... objects) {
        if (objects.length > 0) {
            message = String.format(message, (Object[]) objects);
        }
        String prefixedMessage = new BufferedReader(new StringReader(message)).lines()
            .collect(Collectors.joining("\n"+this.prefix+": ", this.prefix+": ", ""));
        XposedBridge.log(prefixedMessage);
    }
}
