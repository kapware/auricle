# auricle

> You didn't come here to make the choice. </br>
> You've already made it. You're here to try to understand why you made it.

_The Oracle_

**Auricle** is a mobile app intended for gathering quantitive feedback from large audiences that attend presentations (like Toruń JUG meetups: http://torun.jug.pl).

## Usage
The application is intended to be used in a kiosk mode of the device. 

Typical flow:
1. Just after running the app, the initial screen of the device is shown.
2. On the administration screen, start by adding new speaker: type in the name and accept it with enter.
![01-enter-speaker](https://cloud.githubusercontent.com/assets/6385017/25023669/07501404-209b-11e7-9e92-7283300cac2e.png)
3. That will start rating mode (emoticons will be shown).
![02-rating](https://cloud.githubusercontent.com/assets/6385017/25023666/0743ebac-209b-11e7-87fd-0b402929a645.png)
4. Enable kiosk mode here to protect the device, locking it on a single app (ios: triple button, android > 5.0 : pin mode)
5. Pass on the device to the audience to gather feedback.
6. After each rating is given there is a delay that will prevent repetetive ratings and informs users to pass on the device to the next person.
![03-delay](https://cloud.githubusercontent.com/assets/6385017/25023665/07419cf8-209b-11e7-8d91-21b4511bc8b8.png)
7. When the device is back, disable kiosk mode and kill the app (swipe it to kill in the task manager).
![04-kill-the-app](https://cloud.githubusercontent.com/assets/6385017/25023664/073eb696-209b-11e7-83a3-90acf685bdf8.png)
8. Rerun the app to see the cumulative that for speaker.
![05-cumulative-stats](https://cloud.githubusercontent.com/assets/6385017/25023667/0744290a-209b-11e7-8310-11112b3d1536.png)
9. You may want to input https://paste.ee api key to be able to export detailed data for futher analysis (the app gathers each rating individually with a timestamp).
![06-export-to-pastee](https://cloud.githubusercontent.com/assets/6385017/25023668/0745a2ee-209b-11e7-88d5-1eb281dae36d.png)

## Setup

### Prerequisites
1. For prerequisites refer to https://github.com/drapanjanas/re-natal#dependencies.
2. Clone the repository (i.e.: `git clone`). 
3. re-natal 0.5.0 (i.e.: `npm install -g re-natal@0.5.0`)
4. react-native-cli 2.0.1 (i.e.: `npm install -g react-native-cli@2.0.1`)

### Android setup
The app should work, albeit untested, on Jelly Bean 4.1 (requires api 16). However, Android 5.0 is recommended (as it introduces pinning feature aka. kiosk mode: https://developer.android.com/about/versions/android-5.0.html#ScreenPinning). Connect your device using usb cable and enable USB debug mode (Developer settings). Refer to official docs if in doubt: https://developer.android.com/studio/install.html. Then you may try to build and install the app on device.
```
lein prod-build
cd android
./gradlew assembleRelease
keytool -genkey -v -keystore auricle.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore auricle.keystore app/build/outputs/apk/app-release-unsigned.apk alias_name
adb install app/build/outputs/apk/app-release-unsigned.apk 
```

### iOS setup
To install app on ios device, you need a recent *Mac*.
```
lein prod-build
re-natal xcode
```
In Xcode, press `cmd` + `>` and change `Build configuration` to `Release`. Connect your device and pick it from device list. Run your product (`cmd` + `R`). That should build the app and place it on the device.

## Development setup

### iOS development setup
```
re-natal use-figwheel
react-native run-ios
```
When simulator runs it should show red screen with "Unexpected identifier 'GET'" or white screen with "Waiting for Figwheel to load files.". Then run lein:
```
lein repl
```
Here you might want to connect from (spac)emacs to repl (or execute directly from lein repl), and to start figwheel and get clojurescript repl run:
```
(start-figwheel "ios")
```
When `Prompt will show when Figwheel connects to your application` is shown, reload the app in emulator using `Reload M-R`.

### Android development setup
Prerequisite is to have avd created (either with `avdmanager` or AndroidStudio) and emulator running (either with commandline `tools/bin/emulator` or AndroidStudio). I prefer to open auricle project in Android Studio (`auricle/android`) and run project from it. Then issue:
```
re-natal use-figwheel
re-natal use-android-device avd
re-natal use-figwheel
react-native run-android
```
When simulator runs it should show red screen with "Unexpected identifier 'GET'" or white screen with "Waiting for Figwheel to load files.". Then run lein:
```
lein repl
```
Here you might want to connect from (spac)emacs to repl (or execute directly from lein repl), and to start figwheel and get clojurescript repl run:
```
(start-figwheel "android")
```
When `Prompt will show when Figwheel connects to your application` is shown, reload the app in emulator using `Reload R,R`.

## License

Copyright © 2017 kapware

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
