[![CircleCI](https://circleci.com/gh/mapbox/mapbox-events-android/tree/master.svg?style=svg&circle-token=b206c88b942901329c5d8632a9e5d1b8cd501a61)](https://circleci.com/gh/mapbox/mapbox-events-android/tree/master)
[![codecov](https://codecov.io/gh/mapbox/mapbox-events-android/branch/master/graph/badge.svg)](https://codecov.io/gh/mapbox/mapbox-events-android)

# Mapbox Mobile Events

This repository houses the Mapbox Telemetry and Core Libraries for Android.

### About `libtelemetry`

The Mapbox Telemetry Library for Android is used to collect _anonymous_ and _aggregated_ information about Mapbox usage. We use telemetry from all Mapbox SDKs to improve our map, directions, travel times, and search. We collect anonymous data about how users interact with the map to help developers build better location based applications.

Visit [https://www.mapbox.com/telemetry](https://www.mapbox.com/telemetry/) for higher-level information about telemetry and data security at Mapbox

### About `libcore`

The Mapbox Core Library for Android is a set of utilities that help you with permissions, device location, and connectivity within your Android project. You can:

- Check for, request, and respond to any number of Android system permissions such as device location or camera.
- Check for and respond to a change in the device's internet connectivity status.
- Retrieve a device's real-time location.

## Getting Started

If you are looking to include the Core Library inside of your project, please take a look at [the detailed instructions](https://www.mapbox.com/android-docs/core/overview/) found in our docs. If you are interested in building from source, read the contributing guide inside of this project.

The snippet to add to your app-level `build.gradle` file to use  is the following:

```
// Mapbox Core Library for Android

compile 'com.mapbox.mapboxsdk:mapbox-android-core:0.2.1'

```

To run the [sample code](#sample-code) on a device or emulator, include your [developer access token](https://www.mapbox.com/help/define-access-token/) in `developer-config.xml` found in the project. 

## Documentation

You'll find all of the documentation for the Core Library on [our Mapbox Core page](https://www.mapbox.com/android-docs/core/overview). This includes information on installation, using the API, and links to the API reference.

## Getting Help

- **Need help with your code?**: Look for previous questions on the [#mapbox tag](https://stackoverflow.com/questions/tagged/mapbox+android) — or [ask a new question](https://stackoverflow.com/questions/tagged/mapbox+android).
- **Have a bug to report?** [Open an issue](https://github.com/mapbox/mapbox-events-android/issues). If possible, include the version of Mapbox Core that you're using, a full log, and a project that shows the issue.
- **Have a feature request?** [Open an issue](https://github.com/mapbox/mapbox-events-android/issues/new). Tell us what the feature should do and why you want the feature.

## Using Snapshots

If you want to test recent bug fixes or features that have not been packaged in an official release yet, you can use a `-SNAPSHOT` release of the current development version of the Mapbox Core Library via Gradle, available on [Sonatype](https://oss.sonatype.org/content/repositories/snapshots/com/mapbox/mapboxsdk/).

```gradle
repositories {
    mavenCentral()
    maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
}

dependencies {
    compile 'com.mapbox.mapboxsdk:mapbox-android-core:0.3.0'
}
```

## <a name="sample-code">Sample code

[This repo's test app](https://github.com/mapbox/mapbox-events-android/blob/master/app/src/main/java/com/mapbox/android/events/testapp/MainActivity.java) can help you get started with the Core library and to inspire you.

## Contributing

We welcome feedback, translations, and code contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.
