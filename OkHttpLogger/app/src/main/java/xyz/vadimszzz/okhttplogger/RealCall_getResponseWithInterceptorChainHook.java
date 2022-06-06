package xyz.vadimszzz.okhttplogger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static xdroid.core.ObjectUtils.notNull;


// okhttp3.internal.connection.RealCall:
//
// /* compiled from: RealCall.kt */
// /* loaded from: classes.dex */
// public final class RealCall implements Call {
//     ...
//     private final Request originalRequest;
//     ...
//     @Override // okhttp3.Call
//     public Response execute() {
//         if (this.executed.compareAndSet(false, true)) {
//             enter();
//             callStart();
//             try {
//                 this.client.dispatcher().executed$okhttp(this);
//                 return getResponseWithInterceptorChain$okhttp(); // <----
//             } finally {
//                 this.client.dispatcher().finished$okhttp(this);
//             }
//         } else {
//             throw new IllegalStateException("Already Executed".toString());
//         }
//     }
// }

/* okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp() hook. */
public class RealCall_getResponseWithInterceptorChainHook extends XC_MethodHook {
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        try {
            // Get the okhttp3.Request object form the RealCall
            Object request = notNull(XposedHelpers.getObjectField(param.thisObject, "originalRequest"));

            // Get the okhttp3.HttpUrl object from the Request
            Object url = notNull(XposedHelpers.getObjectField(request, "url"));
            String urlString = String.valueOf(url);

            OkHttpLogger.getXposedLogger().log(urlString);
            OkHttpLogger.getFileLogger().log(urlString);
        } catch (Exception e) {
            // Log error and don't crash
            OkHttpLogger.getXposedLogger().log("failed to get request URL: %s", e);
        }
    }
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        /* After hooked method. */
    }
}
