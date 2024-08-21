# AlphaShot

This is a Kotlin Multiplatform Compose app, that uses the Bluetooth Low Energy interface on the
Sony Alpha series cameras (ones that support Bluetooth) to take pictures.

Currently, the project supports basic remote triggers, to capture photos.

## Features in the pipeline

* Make a good intervalometer by extending the ability to take remote-triggers.

* The app should soon support the ability to geo-tag the photos on the Camera, using the GPS on the
  smartphone. This way all the photos can include location metadata.

## Development

* For Compose Previews when building UI, use the `desktopMain` source set. Use the gradle task `run`
to run the project. Any attempts to use the Gutter `Run` button will fail with opaque errors. 

* Also, confusingly there are registered tasks called `jvmRun` and `destopRun`, but when you attempt
to use them, those will fail with a different set of opaque errors. For more information refer to
[this](https://youtrack.jetbrains.com/issue/CMP-5893/desktopRun-jvmRun-cant-be-used-to-run-Compose-Desktop-project) YouTrack.