#!/usr/bin/env bash
set -uo pipefail

adb logcat -c
# Suppress Android's first-launch immersive-mode education overlay so touch evidence
# captures the app itself; gameplay interaction remains entirely touch-driven.
adb shell settings put secure immersive_mode_confirmations confirmed
# The API-35 emulator launcher (Quickstep) ANRs under swiftshader and its system dialog
# would otherwise sit on top of every evidence capture. Hide system ANR/crash dialogs
# and stop the launcher; the game itself is untouched.
adb shell settings put global hide_error_dialogs 1
adb shell am force-stop com.android.launcher3 || true
set +e
./scripts/gradle.sh :android:connectedDebugAndroidTest --stacktrace --info
status=$?
set -e
mkdir -p android/build/reports/androidTests/diagnostics

# Roadmap R8.4: a cold start measured on the emulator, by the platform rather than by the app. The
# instrumentation run above starts the app through the test runner, which never produces a `Displayed` line,
# so the launch happens here explicitly -- after the tests, so it cannot interfere with them: install the APK
# the run just built, start the launcher with the platform's own timing switch, and let logcat carry the
# `Displayed` line into the capture the CI step reads.
#
# The install is not optional, and the first CI run is why: `connectedDebugAndroidTest` uninstalls both APKs
# when it finishes, so the launch after it answered "Activity class ... does not exist" and the startup gate
# correctly reported a missing measurement. The component is resolved from the installed package rather than
# written out here, so a renamed activity cannot silently break this again.
apk=android/build/outputs/apk/debug/android-debug.apk
if [ -f "$apk" ]; then
  adb install -r -t "$apk" || true
fi
component=$(adb shell cmd package resolve-activity --brief com.amirrezahadipoor.herodefense.debug 2>/dev/null | tail -n 1 | tr -d '\r')
if [ -z "$component" ]; then
  component=com.amirrezahadipoor.herodefense.debug/com.amirrezahadipoor.herodefense.android.AndroidLauncher
fi
echo "cold start target: $component"
adb shell am force-stop com.amirrezahadipoor.herodefense.debug || true
sleep 1
adb shell am start -W -n "$component" || true
sleep 4

adb logcat -d > android/build/reports/androidTests/diagnostics/emulator-logcat.txt || true
exit "$status"
