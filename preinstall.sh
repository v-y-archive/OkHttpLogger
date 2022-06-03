#!/bin/bash

echo 'RUN android-24/google_apis/x86 EMULATOR FIRST WITH -no-snapshot option'
cd $(dirname $0)/preinstall/rootAVD
sh rootAVD.sh ${ANDROID_SDK_ROOT}/system-images/android-24/google_apis_playstore/x86/ramdisk.img
cd -