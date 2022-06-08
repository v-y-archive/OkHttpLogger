# Xposed Logger Module for OkHttp
This is a module for the [Xposed Framework](https://xposed.info) that logs all HTTP(S) requests made via [OkHttp](https://square.github.io/okhttp/).

# Internals

## Placing the Hooks

Lifetime of a OkHttp request:

 *  A **`okhttp3.Request()` constructor or `okhttp3.Request$Builder`** might yield some false positives, since Request objects can be created without issuing an HTTP call.
 *  Calling **`okhttp3.OkHttpClient.newCall(Request r)`** isn't reliable.
 *  Calling **`okhttp3.internal.connection.RealCall.execute()`** (synchronous) or **`okhttp3.RealCall.internal.connection.enqueue(Callback c)`** (asynchronous). This also includes calls that were only scheduled but not executed.
 *  Both `execute()` and `enqueue()` lead to **`okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp()`** that executes **`okhttp3.internal.http.RealInterceptorChain.proceed()`** and request is going through the interceptor chain:
    *  **`okhttp3.internal.http.RetryAndFollowUpInterceptor(OkHttpClient c)`**;
    *  **`okhttp3.internal.http.BridgeInterceptor(CookieJar j)`**;
    *  **`okhttp3.internal.cache.CacheInterceptor(Cache c)`** serves requests from the cache and writes responses to the cache. Everything before this interceptor doesn't matter and doesn't guarantee a real network request.
    *  **`okhttp3.internal.connection.ConnectInterceptor`**;
    *  **`okhttp3.internal.http.CallServerInterceptor(Boolean forWebSocket)`** which issues the actual request on the network.

This module implements hooking `okhttp3.internal.http.CallServerInterceptor.intercept(Interceptor.Chain c)` and obtains the `okhttp3.Request` object. It logs requested URLs between the last user-supplied application interceptor and the OkHttp core.

All requests are logged with timestamps in a logfile `_requests.log` in the private storage of each application.

# Build

```
./build
```

Make sure Java version >= 11. Build with gradle and most recent version of Android SDK build tools.

# Install

```
./deploy [-r]

# OPTIONS:
#  -r 		reboot device after installing
```

The generated APK is in `OkHttpLogger/build/outputs/apk/<buildtype>/`. Once installed, it should show up as `OkHttpLogger` in the Xposed Installer.

# Setup test environment

Setup AVD emulator:

* ```
  sdkmanager --install "emulator" "extras;intel;Hardware_Accelerated_Execution_Manager" "system-images;android-24;google_apis_playstore;x86"
  avdmanager create avd -k "system-images;android-24;google_apis_playstore;x86" -n Android7_x86_Xposed
  ```

* ```
  ${ANDROID_SDK_ROOT}/emulator/emulator @Android7_x86_Xposed -no-snapshot -no-boot-anim -gpu host
  ./patchavd ${ANDROID_SDK_ROOT}/system-images/android-24/google_apis_playstore/x86/ramdisk.img
  ${ANDROID_SDK_ROOT}/emulator/emulator @Android7_x86_Xposed -no-boot-anim -gpu host
  ```

* Open Magisk, if you will see "Requires Additional Setup" press OK, reboot.

* Open Magisk, drag & drop [xposed-systemless-24.zip](https://github.com/vadimszzz/xposed-systemless/releases/tag/v90) to the emulator, press `Modules`, install Xposed Magisk module.

# Performance tips

### Running ARMv7/ARM64 image on x86 host with maximum emulation performance:

1. Close all Google Chrome instances and shutdown your antivirus.

2. Better create ARMv7 image without Google APIs. The latest ARMv7 image is Android 7 API 24.

3. Fix "cannot add library /usr/local/android-sdk/emulator/qemu/darwin-x86_64/lib64/vulkan/libvulkan.dylib: failed":  
   `ln -s ${ANDROID_SDK_ROOT}/emulator/lib64 ${ANDROID_SDK_ROOT}/emulator/qemu/darwin-x86_64/lib64`

4. `${ANDROID_SDK_ROOT}/emulator/emulator @NAME -no-snapshot -no-boot-anim -gpu host -qemu -icount auto -smp 4,sockets=1,cores=2,threads=2,maxcpus=4`  

   `maxcpus` must be a half of your real CPU cores for the best performance but i usually set all 4  
   `sockets` * `cores` * `threads` must be not greater than `maxcpus`  
   `smp` must be not greater than `maxcpus`  

   You can try this examples:  

   `-qemu -icount auto -smp 6,sockets=1,cores=6,threads=1,maxcpus=6`  
   `-qemu -smp 6,sockets=1,cores=6,threads=1,maxcpus=6`  
   `-qemu -icount auto -smp 4,sockets=1,cores=2,threads=2,maxcpus=4`  
   `-qemu -smp 4,sockets=1,cores=2,threads=2,maxcpus=4`  
   `-qemu -icount auto -smp 2,sockets=1,cores=2,threads=1,maxcpus=2`  
   `-qemu -smp 2,sockets=1,cores=2,threads=1,maxcpus=2`  

5. You should wait for 15 minutes even if you have the best hardware.

6. After successful booting press `...` button, `Snapshots`, `Take snapshot`. It will save your time if something goes wrong.
   To start the emulator next time use the same command without `-no-snapshot` option:  
   `${ANDROID_SDK_ROOT}/emulator/emulator @NAME -no-boot-anim -gpu host -qemu -icount auto -smp 4,sockets=1,cores=2,threads=2,maxcpus=4`  


### Running x86/x86_64 image on x86_64 host with maximum emulation performance:

1. Install Intel HAXM.

2. Fix "cannot add library /usr/local/android-sdk/emulator/qemu/darwin-x86_64/lib64/vulkan/libvulkan.dylib: failed":  
   `ln -s ${ANDROID_SDK_ROOT}/emulator/lib64 ${ANDROID_SDK_ROOT}/emulator/qemu/darwin-x86_64/lib64`

3. `${ANDROID_SDK_ROOT}/emulator/emulator @NAME -no-boot-anim -gpu host`

More information: https://developer.android.com/studio/run/emulator-acceleration

