# !/bin/bash
#
# Copyright 2018 Stuart Scott
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e
set -x

# Tools
SDK=$HOME/Library/Android/sdk
BUILD_TOOLS=${SDK}/build-tools/27.0.3
AAPT=${BUILD_TOOLS}/aapt
ADB=${SDK}/platform-tools/adb
APK_SIGNER=${BUILD_TOOLS}/apksigner
DEX=${BUILD_TOOLS}/dx
ZIP_ALIGN=${BUILD_TOOLS}/zipalign
PLATFORM=${SDK}/platforms/android-27/android.jar

# Libraries
LIBRARIES=${HOME}/match/libraries
ANDROID_SUPPORT_TEST=${LIBRARIES}/android-support-test.jar
JUNIT=${LIBRARIES}/junit-4.12.jar

# Output
OUT_ANDROID_APK=out/android/apk
OUT_ANDROID_DEX=out/android/dex
OUT_ANDROID_JAVA=out/android/java
OUT_ANDROID_RES=out/android/res
OUT_ANDROID_RESULTS=out/android/results
OUT_JAVA_CLASSES=out/java/classes
OUT_JAVA_JAR=out/java/jar
OUT_JAVA_MANIFEST=out/java/manifest

# Create output directories
mkdir -p ${OUT_ANDROID_APK}
mkdir -p ${OUT_ANDROID_DEX}/HelloDroid/
mkdir -p ${OUT_ANDROID_DEX}/HelloDroidInstrumentationTest/
mkdir -p ${OUT_ANDROID_DEX}/HelloDroidLib/
mkdir -p ${OUT_ANDROID_JAVA}/HelloDroid/hello/droid/
mkdir -p ${OUT_ANDROID_JAVA}/HelloDroidInstrumentationTest/hello/droid/test/
mkdir -p ${OUT_ANDROID_JAVA}/HelloDroidLib/hello/droid/common/
mkdir -p ${OUT_ANDROID_RES}/HelloDroid/
mkdir -p ${OUT_ANDROID_RES}/HelloDroidLib/
mkdir -p ${OUT_ANDROID_RESULTS}
mkdir -p ${OUT_JAVA_CLASSES}/HelloDroid/
mkdir -p ${OUT_JAVA_CLASSES}/HelloDroidInstrumentationTest/
mkdir -p ${OUT_JAVA_CLASSES}/HelloDroidLib/
mkdir -p ${OUT_JAVA_JAR}/HelloDroid/
mkdir -p ${OUT_JAVA_JAR}/HelloDroidInstrumentationTest/
mkdir -p ${OUT_JAVA_JAR}/HelloDroidLib/
mkdir -p ${OUT_JAVA_MANIFEST}

# Build library
${AAPT} package -v -m -f \
    --auto-add-overlay \
    --shared-lib \
    --output-text-symbols ${OUT_ANDROID_RES}/HelloDroidLib/ \
    -F ${OUT_ANDROID_APK}/HelloDroidLib.apk \
    -M Lib/AndroidManifest.xml \
    -J ${OUT_ANDROID_JAVA}/HelloDroidLib/ \
    -P ${OUT_ANDROID_RES}/HelloDroidLib/public_resources.xml \
    -G ${OUT_ANDROID_RES}/HelloDroidLib/proguard.txt \
    -I ${PLATFORM} \
    -S Lib/resource \
    -S Lib/resource-aapt
javac \
    -bootclasspath ${PLATFORM} \
    -sourcepath ${OUT_ANDROID_JAVA}/HelloDroidLib/ \
    Lib/source/hello/droid/common/Common.java \
    Lib/source/hello/droid/common/MainActivity.java \
    -d ${OUT_JAVA_CLASSES}/HelloDroidLib/
jar \
    cf ${OUT_JAVA_JAR}/HelloDroidLib/classes.jar \
    -C ${OUT_JAVA_CLASSES}/HelloDroidLib/ .
zip -jX ${OUT_ANDROID_APK}/HelloDroidLib.apk \
    ${OUT_JAVA_JAR}/HelloDroidLib/classes.jar
zip -jX ${OUT_ANDROID_APK}/HelloDroidLib.apk \
    ${OUT_ANDROID_RES}/HelloDroidLib/public_resources.xml
zip -jX ${OUT_ANDROID_APK}/HelloDroidLib.apk \
    ${OUT_ANDROID_RES}/HelloDroidLib/R.txt

# Build App
${AAPT} package -v -m -f \
    --auto-add-overlay \
    -F ${OUT_ANDROID_APK}/HelloDroid.unaligned.apk \
    -I ${PLATFORM} \
    -I ${OUT_ANDROID_APK}/HelloDroidLib.apk \
    -J ${OUT_ANDROID_JAVA}/HelloDroid/ \
    -M App/AndroidManifest.xml \
    -S App/resource/
javac \
    -bootclasspath ${PLATFORM} \
    -cp ${OUT_JAVA_JAR}/HelloDroidLib/classes.jar \
    -sourcepath ${OUT_ANDROID_JAVA}/HelloDroid/ \
    App/source/hello/droid/OtherActivity.java \
    -d ${OUT_JAVA_CLASSES}/HelloDroid/
jar \
    cf ${OUT_JAVA_JAR}/HelloDroid/classes.jar \
    -C ${OUT_JAVA_CLASSES}/HelloDroid/ .
${DEX} --dex \
    --verbose \
    --debug \
    --keep-classes \
    --output=${OUT_ANDROID_DEX}/HelloDroid/classes.dex \
    ${OUT_JAVA_JAR}/HelloDroidLib/classes.jar \
    ${OUT_JAVA_CLASSES}/HelloDroid/
zip -jX ${OUT_ANDROID_APK}/HelloDroid.unaligned.apk \
    ${OUT_ANDROID_DEX}/HelloDroid/classes.dex
${ZIP_ALIGN} \
    -f 4 \
    ${OUT_ANDROID_APK}/HelloDroid.unaligned.apk \
    ${OUT_ANDROID_APK}/HelloDroid.aligned.apk
${APK_SIGNER} sign \
    --ks private/hellodroid-release-key.keystore \
    --ks-pass file:private/hellodroid-release-key.password \
    --out ${OUT_ANDROID_APK}/HelloDroid.apk \
    ${OUT_ANDROID_APK}/HelloDroid.aligned.apk

# Build Instrumentation App
${AAPT} package -v -m -f \
    --auto-add-overlay \
    -F ${OUT_ANDROID_APK}/HelloDroidInstrumentationTest.unaligned.apk \
    -J ${OUT_ANDROID_JAVA}/HelloDroidInstrumentationTest/ \
    -I ${PLATFORM} \
    -I ${OUT_ANDROID_APK}/HelloDroid.apk \
    -M App/tests/instrumentation/AndroidManifest.xml
javac \
    -bootclasspath ${PLATFORM} \
    -cp ${OUT_JAVA_JAR}/HelloDroid/classes.jar:${OUT_JAVA_JAR}/HelloDroidLib/classes.jar:${JUNIT}:${ANDROID_SUPPORT_TEST} \
    -sourcepath ${OUT_ANDROID_JAVA}/HelloDroid/:${OUT_ANDROID_JAVA}/HelloDroidInstrumentationTest/:${OUT_ANDROID_JAVA}/HelloDroidLib/ \
    App/tests/instrumentation/hello/droid/HelloDroidInstrumentedTest.java \
    -d ${OUT_JAVA_CLASSES}/HelloDroidInstrumentationTest/
jar \
    cf ${OUT_JAVA_JAR}/HelloDroidInstrumentationTest/classes.jar \
    -C ${OUT_JAVA_CLASSES}/HelloDroidInstrumentationTest/ .
${DEX} --dex \
    --verbose \
    --debug \
    --keep-classes \
    --output=${OUT_ANDROID_DEX}/HelloDroidInstrumentationTest/classes.dex \
    ${JUNIT} \
    ${ANDROID_SUPPORT_TEST} \
    ${OUT_JAVA_CLASSES}/HelloDroidInstrumentationTest/
zip -jX ${OUT_ANDROID_APK}/HelloDroidInstrumentationTest.unaligned.apk \
    ${OUT_ANDROID_DEX}/HelloDroidInstrumentationTest/classes.dex
${ZIP_ALIGN} \
    -f 4 \
    ${OUT_ANDROID_APK}/HelloDroidInstrumentationTest.unaligned.apk \
    ${OUT_ANDROID_APK}/HelloDroidInstrumentationTest.aligned.apk
${APK_SIGNER} sign \
    --ks private/hellodroid-release-key.keystore \
    --ks-pass file:private/hellodroid-release-key.password \
    --out ${OUT_ANDROID_APK}/HelloDroidInstrumentationTest.apk \
    ${OUT_ANDROID_APK}/HelloDroidInstrumentationTest.aligned.apk

# Install APKs
${ADB} install -r ${OUT_ANDROID_APK}/HelloDroid.apk
${ADB} install -r ${OUT_ANDROID_APK}/HelloDroidInstrumentationTest.apk

# Instrument Tests
${ADB} shell am instrument -w hello.droid.test/android.support.test.runner.AndroidJUnitRunner
