#!/bin/sh
#
# This is a simple shell script that is based on Xposed's update-binary script
# Tested on AVD android 6 armv7 emulator (sdk23) with macOS host
# Before you run this, make sure you have downloaded Xposed flashable zip from their site, and cd into it
# To this to work properly, run the emulator with -no-snapshot -writable-system flags
# For example: MacBook-Pro-2:tools matandobr$  ./emulator -no-snapshot -writable-system @Nexus5API23
# You should flash SuperSU before flashing Xposed - https://gist.github.com/matandobr/48665a60adb4ae5d7a3b956819aa42d9
# 

ADB_COMMAND="adb "

cp_perm() {
  cp -f $1 $2 || exit 1
  set_perm $2 $3 $4 $5 $6
}

set_perm() {
  chown $2:$3 $1 || exit 1
  chmod $4 $1 || exit 1
  if [ "$5" ]; then
    chcon $5 $1 2>/dev/null
  else
    chcon 'u:object_r:system_file:s0' $1 2>/dev/null
  fi
}

install_nobackup() {
  cp_perm ./$1 $1 $2 $3 $4 $5
}

install_and_link() {
  TARGET=$1
  XPOSED="${1}_xposed"
  BACKUP="${1}_original"
  if [ ! -f ./$XPOSED ]; then
    return
  fi
  cp_perm ./$XPOSED $XPOSED $2 $3 $4 $5
  if [ ! -f $BACKUP ]; then
    mv $TARGET $BACKUP || exit 1
    ln -s $XPOSED $TARGET || exit 1
    chcon -h 'u:object_r:system_file:s0' $TARGET 2>/dev/null
  fi
}

install_overwrite() {
  TARGET=$1
  if [ ! -f ./$TARGET ]; then
    return
  fi
  BACKUP="${1}.orig"
  NO_ORIG="${1}.no_orig"
  if [ ! -f $TARGET ]; then
    touch $NO_ORIG || exit 1
    set_perm $NO_ORIG 0 0 600
  elif [ -f $BACKUP ]; then
    rm -f $TARGET
    gzip $BACKUP || exit 1
    set_perm "${BACKUP}.gz" 0 0 600
  elif [ ! -f "${BACKUP}.gz" -a ! -f $NO_ORIG ]; then
    mv $TARGET $BACKUP || exit 1
    gzip $BACKUP || exit 1
    set_perm "${BACKUP}.gz" 0 0 600
  fi
  cp_perm ./$TARGET $TARGET $2 $3 $4 $5
}

echo "- Placing files"
install_nobackup /system/xposed.prop                      0    0 0644
install_nobackup /system/framework/XposedBridge.jar       0    0 0644

install_and_link  /system/bin/app_process32               0 2000 0755 u:object_r:zygote_exec:s0
install_overwrite /system/bin/dex2oat                     0 2000 0755 u:object_r:dex2oat_exec:s0
install_overwrite /system/bin/oatdump                     0 2000 0755
install_overwrite /system/bin/patchoat                    0 2000 0755 u:object_r:dex2oat_exec:s0
install_overwrite /system/lib/libart.so                   0    0 0644
install_overwrite /system/lib/libart-compiler.so          0    0 0644
install_overwrite /system/lib/libart-disassembler.so      0    0 0644
install_overwrite /system/lib/libsigchain.so              0    0 0644
install_nobackup  /system/lib/libxposed_art.so            0    0 0644

find /system /vendor -type f -name '*.odex.gz' 2>/dev/null | while read f; do mv "$f" "$f.xposed"; done

echo "Done, You can now restart and install the GUI apk"

