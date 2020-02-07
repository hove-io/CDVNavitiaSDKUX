#!/bin/bash

echo "
*************************************************************
***  Remove and recreate dependencies and generated files ***
*************************************************************

-> say yes

"

# Clean plugins
ionic repair

echo "Setup Cordova for Android X"

# platforms/android/gradle.properties
if grep -Fxq "android.useAndroidX=true" platforms/android/gradle.properties
then
    # code if found
    echo "AndroidX is properly configured"
else
    # code if not found
    echo "AndroidX will be properly configured"
    echo "

# AndroidX
android.useAndroidX=true
android.enableJetifier=true" >> platforms/android/gradle.properties
fi

# Update Gradle Plugin
echo "Update Gradle Plugin to 3.5.3 "

sed -ie 's/com.android.tools.build:gradle:[0-9]\{1,\}.[0-9]\{1,\}.[0-9]\{1,\}/com.android.tools.build:gradle:3.5.3/' platforms/android/build.gradle

# sdk version and build tools
echo "Update SDK version and Build Tools"

sed -ie 's/defaultBuildToolsVersion="[0-9]\{2,\}.[0-9]\{1,\}.[0-9]\{1,\}"/defaultBuildToolsVersion="29.0.2"/' platforms/android/build.gradle
sed -ie 's/defaultMinSdkVersion=[0-9]\{2,\}/defaultMinSdkVersion=21/' platforms/android/build.gradle
sed -ie 's/defaultTargetSdkVersion=[0-9]\{2,\}/defaultTargetSdkVersion=29/' platforms/android/build.gradle
sed -ie 's/defaultCompileSdkVersion=[0-9]\{2,\}/defaultCompileSdkVersion=29/' platforms/android/build.gradle

echo "
****************************************************
***         Test App is ready to launch!         ***
****************************************************

*** 1. Create Environment variable:
    export CORDOVA_ANDROID_GRADLE_DISTRIBUTION_URL=https://services.gradle.org/distributions/gradle-6.1.1-all.zip

*** 2. Emulate Android:
    ionic cordova emulate android
"
