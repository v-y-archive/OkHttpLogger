

# How to run Android Studio Emulator without lags

### Running ARMv7/ARM64 image on x86 host with maximum emulation performance:

1. Close all Google Chrome instances and shutdown your antivirus.

2. Better create ARMv7 image without Google APIs.

3. Fix "cannot add library /usr/local/android-sdk/emulator/qemu/darwin-x86_64/lib64/vulkan/libvulkan.dylib: failed":  
   ` λ ln -s ${ANDROID_SDK_ROOT}/emulator/lib64 ${ANDROID_SDK_ROOT}/emulator/qemu/darwin-x86_64/lib64`

4. ` λ ${ANDROID_SDK_ROOT}/emulator/emulator @NAME -no-snapshot -no-boot-anim -gpu host -qemu -icount auto -smp 4,sockets=1,cores=2,threads=2,maxcpus=4`  
   
   `maxcpus` must be a half of your real CPU cores for the best performance but i usually set all 4  
   `sockets` * `cores` * `threads` must be not greater than `maxcpus`  
   `smp` must be not greater than `maxcpus`  

   You can test this examples:  
   
   `-qemu -icount auto -smp 6,sockets=1,cores=6,threads=1,maxcpus=6`
   `-qemu -smp 6,sockets=1,cores=6,threads=1,maxcpus=6`
   `-qemu -icount auto -smp 4,sockets=1,cores=2,threads=2,maxcpus=4`
   `-qemu -smp 4,sockets=1,cores=2,threads=2,maxcpus=4`
   `-qemu -icount auto -smp 2,sockets=1,cores=2,threads=1,maxcpus=2`
   `-qemu -smp 2,sockets=1,cores=2,threads=1,maxcpus=2`

5. You should wait for 15 minutes even if you have the best hardware.

6. After successful booting press `...` button, `Snapshots`, `Take snapshot`. It will save your time if something goes wrong.
   To start the emulator next time use the same command without `-no-snapshot` option:  
   ` λ ${ANDROID_SDK_ROOT}/emulator/emulator @NAME -no-boot-anim -gpu host -qemu -icount auto -smp 4,sockets=1,cores=2,threads=2,maxcpus=4`  


### Running x86/x86_64 image on x86_64 host with maximum emulation performance:

1. Install Intel HAXM.

2. Fix "cannot add library /usr/local/android-sdk/emulator/qemu/darwin-x86_64/lib64/vulkan/libvulkan.dylib: failed":  
   ` λ ln -s ${ANDROID_SDK_ROOT}/emulator/lib64 ${ANDROID_SDK_ROOT}/emulator/qemu/darwin-x86_64/lib64`

3. ` λ ${ANDROID_SDK_ROOT}/emulator/emulator @NAME -no-boot-anim -gpu host`

More information: https://developer.android.com/studio/run/emulator-acceleration