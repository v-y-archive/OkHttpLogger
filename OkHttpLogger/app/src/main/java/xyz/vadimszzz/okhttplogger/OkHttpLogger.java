package xyz.vadimszzz.okhttplogger;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;

import xyz.vadimszzz.okhttplogger.XposedLogger;
import xyz.vadimszzz.okhttplogger.RealCall_getResponseWithInterceptorChainHook;


/* OkHttp hooker and logger. */
public class OkHttpLogger implements IXposedHookLoadPackage {
    private static String moduleName = "OkHttpLogger";
    private static String fileLoggerFilename = "_requests.log";
    private static Boolean hookOnlyRealRequests = false;
    private static XposedLogger xposedLogger;
    private static FileLogger fileLogger;

    /**
     * Get FileLogger instance.
     * 
     * @return FileLogger
     */
    public static FileLogger getFileLogger() {
        if (fileLogger == null) {
           fileLogger = new FileLogger(fileLoggerFilename);
        }
        return fileLogger;
    }

    /**
     * Get XposedLogger instance.
     * 
     * @return XposedLogger
     */
    public static XposedLogger getXposedLogger() {
        if (xposedLogger == null) {
            xposedLogger = new XposedLogger(moduleName);
        }
        return xposedLogger;
    }

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (XposedHelpers.findClassIfExists("okhttp3.OkHttpClient", lpparam.classLoader) == null) {
            return;
        }
        OkHttpLogger.getXposedLogger().log(
            "OkHttp found in %s\n" + 
            "View log: adb shell su -c cat /data/data/%s/files/%s",
            lpparam.packageName, lpparam.packageName, fileLoggerFilename
        );

        if (!hookOnlyRealRequests) {
            // Get the okhttp3.internal.connection.RealCall class
            Class RealCall = XposedHelpers.findClassIfExists("okhttp3.internal.connection.RealCall", lpparam.classLoader);
            if (RealCall == null) {
                return;
            }
            XposedHelpers.findAndHookMethod(RealCall, "getResponseWithInterceptorChain$okhttp",
                                                                            new RealCall_getResponseWithInterceptorChainHook());
        } else {

        }
    }
}
