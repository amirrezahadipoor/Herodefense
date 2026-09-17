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
# so the launch happens here explicitly -- after the tests, so it cannot interfere with them: stop the
# process, start the launcher with the platform's own timing switch, and let logcat carry the `Displayed`
# line into the capture the CI step reads. A failure to start is tolerated: the gate then reports a missing
# measurement, which is the honest answer, rather than failing a build for a reason that is not the build.
adb shell am force-stop com.amirrezahadipoor.herodefense.debug || true
adb shell am start -W -n com.amirrezahadipoor.herodefense.debug/com.amirrezahadipoor.herodefense.android.AndroidLauncher || true
sleep 4

adb logcat -d > android/build/reports/androidTests/diagnostics/emulator-logcat.txt || true
exit "$status"
