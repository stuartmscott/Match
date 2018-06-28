HelloDroid
==========

This sample shows how Match can create an Android APK and Library, as well as compile and run Unit and Instrumentation Tests.

## match
Contains the build rules of the program, consisting of the;
1. Libraries used
2. Libraries created
3. Apks created
4. Tests compiled and executed

## App/
Holds a simple Android APK which depends on Lib.

### source/
Holds the source code.

### tests/unit/
Holds the unit tests.

### tests/instrumentation/
Holds the instrumentation tests.

## Lib/
Holds a simple Android Library containing common code and resources.

### source/
Holds the source code.

### tests/
Holds the tests.

#Setup
To setup Match to build Android targets, ensure the Android SDK is installed and its path is defined in Match's config file, eg:

## ~/match/config
android-sdk-location=$HOME/Library/Android/sdk