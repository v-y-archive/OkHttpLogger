package xyz.vadimszzz.okhttplogger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static xdroid.core.ObjectUtils.notNull;


// okhttp3.internal.http.CallServerInterceptor:
//
// /* compiled from: CallServerInterceptor.kt */
// /* loaded from: classes.dex */
// public final class CallServerInterceptor implements Interceptor {
//     private final boolean forWebSocket;
//     ...
//     public Response intercept(Interceptor.Chain chain) {
//         RealInterceptorChain realChain = chain;
//         Exchange exchange = realChain.exchange;
//         Request request = realChain.request;
//         ...
//         IOException sendRequestException = null;
//         try {
//             exchange.writeRequestHeaders(request);  <----
//             ...

// okhttp3.internal.connection.Exchange:
//
// /* compiled from: Exchange.kt */
// /* loaded from: classes.dex */
// public final class Exchange {
//     ...
//     public final void writeRequestHeaders(Request request) throws IOException {
//         Intrinsics.checkParameterIsNotNull(request, "request");
//         try {
//             this.eventListener.requestHeadersStart(this.call);
//             this.codec.writeRequestHeaders(request);  <----
//             this.eventListener.requestHeadersEnd(this.call, request);
//         } catch (IOException e) {
//             this.eventListener.requestFailed(this.call, e);
//             trackFailure(e);
//             throw e;
//         }
//     }
//     ...
// }

/* okhttp3.internal.connection.Exchange.writeRequestHeaders(okhttp3.Request r) hook. */
public class Exchange_writeRequestHeadersHook extends XC_MethodHook {
    protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        try {
            // Get the okhttp3.HttpUrl object from the Request
            Object url = notNull(XposedHelpers.getObjectField(param.args[0], "url"));
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