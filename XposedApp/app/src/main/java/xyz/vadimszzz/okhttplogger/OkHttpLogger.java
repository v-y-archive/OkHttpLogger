package xyz.vadimszzz.okhttplogger;

import java.io.FileOutputStream;
import java.sql.Timestamp;

import android.app.Application;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static xdroid.toaster.Toaster.toast;
import static xdroid.toaster.Toaster.toastLong;


public class OkHttpLogger implements IXposedHookLoadPackage {

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.contains("org.wikipedia")) {
            return;
        }
        XposedBridge.log("OkHttpLogger: Loaded app: " + lpparam.packageName);

        // Get the okhttp3.internal.connection.RealCall class
        Class RealCall = XposedHelpers.findClassIfExists("okhttp3.internal.connection.RealCall", lpparam.classLoader);
        if (RealCall == null) {
            XposedBridge.log("OkHttpLogger: Request is null");
            return;
        }

        // okhttp3.internal.connection.RealCall:
        //
        // /* compiled from: RealCall.kt */
        // /* loaded from: classes.dex */
        // public final class RealCall implements Call {
        //     @Override // okhttp3.Call
        //     public Response execute() {
        //         if (this.executed.compareAndSet(false, true)) {
        //             enter();
        //             callStart();
        //             try {
        //                 this.client.dispatcher().executed$okhttp(this);
        //                 return getResponseWithInterceptorChain$okhttp();
        //             } finally {
        //                 this.client.dispatcher().finished$okhttp(this);
        //             }
        //         } else {
        //             throw new IllegalStateException("Already Executed".toString());
        //         }
        //     }
        // }

        XposedHelpers.findAndHookMethod(RealCall, "execute", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String reqKey = "originalRequest";
                    String urlKey = "url";

                    // Get the okhttp3.Request object form the RealCall object
                    Object req = XposedHelpers.getObjectField(param.thisObject, reqKey);
                    if (req == null) {
                        XposedBridge.log("OkHttpLogger: Request is null");
                        return;
                    }
        
                    // Get the okhttp3.HttpUrl object from the Request
                    Object url = XposedHelpers.getObjectField(req, urlKey);
                    if (url == null) {
                        XposedBridge.log("OkHttpLogger: url is null");
                        return;
                    }

                    XposedBridge.log("OkHttpLogger: " + url);

                    // Toast
                    toastLong(String.valueOf(url));
            
                    Application app = AndroidAppHelper.currentApplication();
                    if (app == null) {
                        XposedBridge.log("OkHttpLogger: Cannot get app context!");
                        return;
                    }

                    // Append to "_requests.log" in internal storage of the hooked app
                    FileOutputStream fos = app.openFileOutput("_requests.log", Context.MODE_APPEND);
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    fos.write(String.format("%-31s %s\n", timestamp, url).getBytes());
                    fos.close();
                } catch (Exception e) {
                    // Log error and don't crash
                    XposedBridge.log("OkHttpLogger: failed to get request URL: " + e);
                }
            }

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                /* After hooked method. */
            }
        });
    }
}
