package xyz.vadimszzz.okhttplogger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static xdroid.core.ObjectUtils.notNull;


/* okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp() hook */
public class RealCall_getResponseWithInterceptorChainHook extends XC_MethodHook {
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        try {
            // Get the okhttp3.Request object form the RealCall
            Object req = notNull(XposedHelpers.getObjectField(param.thisObject, "originalRequest"));

            // Get the okhttp3.HttpUrl object from the Request
            Object url = notNull(XposedHelpers.getObjectField(req, "url"));
            String urlString = String.valueOf(url);

            OkHttpLogger.getXposedLogger().log(urlString);
            OkHttpLogger.getFileLogger().log(urlString);
        } catch (Exception e) {
            // Log error and don't crash
            OkHttpLogger.getXposedLogger().log("failed to get request URL: " + e);
        }
    }
}
