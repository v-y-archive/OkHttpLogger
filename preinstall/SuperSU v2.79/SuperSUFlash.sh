#!/bin/sh
#
# This is a simple shell script that does exactly what SuperSU guided in their code.
# Tested on AVD android 6 armv7 emulator (sdk23) with macOS host
# Before you run this, make sure you have downloaded SuperSu flashable zip from their site, and cd into it
# To this to work properly, run the emulator with -no-snapshot -writable-system flags
# For example: MacBook-Pro-2:tools matandobr$  ./emulator -no-snapshot -writable-system @Nexus5API23
# cd into the extracted SuperSU-v2.82 folder, and run it from there.


# There is one issue I faced - when starting the emulator it won't boot until you disable SELinux (adb shell setenforce 0)
# After the boot completes you can turn it back on.
# Make sure you restart the emulator after running the script so the system app will installed properly
# 

ADB_COMMAND="adb "

ARCH=$(adb shell getprop ro.product.cpu.abi | tr -d '\n\r')
case "$ARCH" in
arm64|aarch64)
  ARCH="arm64"
;;
arm*)
  ARCH="armv7"
;;
x86_64|amd64)
  ARCH="x64"
;;
i[3-6]86|x86)
  ARCH="x86"
;;
esac

echo "Getting emulator ready"
$ADB_COMMAND root
$ADB_COMMAND remount
$ADB_COMMAND shell setenforce 0

echo "Backing up files"
$ADB_COMMAND shell cp /system/bin/app_process /system/bin/app_process_original
$ADB_COMMAND shell cp /system/bin/app_process32 /system/bin/app_process32_original
$ADB_COMMAND shell cp /system/bin/app_process /system/bin/app_process_init

echo "Pushing & chmoding SuperSu.apk"
$ADB_COMMAND push common/Superuser.apk /system/app/SuperSU/SuperSU.apk
$ADB_COMMAND shell chmod 0644 /system/app/SuperSU/SuperSU.apk 
$ADB_COMMAND shell chcon u:object_r:system_file:s0 /system/app/SuperSU/SuperSU.apk 

echo "Pushing & chmoding install-recovery"
$ADB_COMMAND push common/install-recovery.sh /system/etc/install-recovery.sh
$ADB_COMMAND shell chmod 0755 /system/etc/install-recovery.sh
$ADB_COMMAND shell chcon u:object_r:toolbox_exec:s0 /system/etc/install-recovery.sh
$ADB_COMMAND shell ln -s /system/etc/install-recovery.sh /system/bin/install-recovery.sh

echo "Pushing & chmoding su"
$ADB_COMMAND push $ARCH/su /system/xbin/su      
$ADB_COMMAND push $ARCH/su /system/bin/.ext/.su 
$ADB_COMMAND push $ARCH/su /system/xbin/daemonsu
$ADB_COMMAND shell chmod 0755 /system/xbin/su      
$ADB_COMMAND shell chmod 0755 /system/bin/.ext/.su 
$ADB_COMMAND shell chmod 0755 /system/xbin/daemonsu
$ADB_COMMAND shell chcon u:object_r:system_file:s0 /system/xbin/su       
$ADB_COMMAND shell chcon u:object_r:system_file:s0 /system/bin/.ext/.su  
$ADB_COMMAND shell chcon u:object_r:system_file:s0 /system/xbin/daemonsu 

echo "Pushing & chmoding supolicy & libsupol.so"
$ADB_COMMAND push $ARCH/supolicy    /system/xbin/supolicy
$ADB_COMMAND push $ARCH/libsupol.so /system/lib/libsupol.so
$ADB_COMMAND shell chmod 0755 /system/xbin/supolicy
$ADB_COMMAND shell chmod 0644 /system/lib/libsupol.so
$ADB_COMMAND shell chcon u:object_r:system_file:s0 /system/xbin/supolicy
$ADB_COMMAND shell chcon u:object_r:system_file:s0 /system/lib/libsupol.so

echo "Symlinking app_process to supersu"
$ADB_COMMAND shell rm /system/bin/app_process
$ADB_COMMAND shell rm /system/bin/app_process32
$ADB_COMMAND shell ln -s /system/xbin/daemonsu /system/bin/app_process
$ADB_COMMAND shell ln -s /system/xbin/daemonsu /system/bin/app_process32

echo "Finising"
$ADB_COMMAND shell touch /system/etc/.installed_su_daemon
$ADB_COMMAND shell chmod 0644 /system/etc/.installed_su_daemon
$ADB_COMMAND shell chcon u:object_r:system_file:s0 /system/etc/.installed_su_daemon
$ADB_COMMAND shell /system/xbin/su --install

