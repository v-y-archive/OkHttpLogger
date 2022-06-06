package xyz.vadimszzz.okhttplogger;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;

import static xdroid.toaster.Toaster.toast;
import static xdroid.toaster.Toaster.toastLong;


/* Log all OkHttp requests. */
public class OkHttpLogger implements IXposedHookLoadPackage {
    private static String moduleName = "OkHttpLogger";
    private static String fileLoggerFilename = "_requests.log";
    private static Boolean hookOnlyRealRequests = true;
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
        toast(String.format("Loaded in %s", lpparam.packageName));
        toastLong(String.format(
            "View log: su -c cat /data/data/%s/files/%s",
            lpparam.packageName, fileLoggerFilename
        ));

        if (!hookOnlyRealRequests) {
            // Hook real and cached OkHttp requests.

            // Get the okhttp3.internal.connection.RealCall class
            Class RealCall = XposedHelpers.findClassIfExists(
                "okhttp3.internal.connection.RealCall",
                lpparam.classLoader
            );
            if (RealCall == null) {
                return;
            }
            // Get the okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp() method
            XposedHelpers.findAndHookMethod(
                RealCall,
                "getResponseWithInterceptorChain$okhttp",
                new RealCall_getResponseWithInterceptorChainHook()
            );
        } else {
            // Hook only real network OkHttp requests.

            // Get the okhttp3.internal.connection.Exchange class
            Class Exchange = XposedHelpers.findClassIfExists(
                "okhttp3.internal.connection.Exchange",
                lpparam.classLoader
            );
            if (Exchange == null) {
                return;
            }
            // Get the okhttp3.internal.connection.Exchange.writeRequestHeadersHook(okhttp3.Request r) method
            XposedHelpers.findAndHookMethod(
                Exchange,
                "writeRequestHeaders",
                "okhttp3.Request",
                new Exchange_writeRequestHeadersHook()
            );
        }
    }
}
