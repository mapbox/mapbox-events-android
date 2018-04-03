# Changelog for Mapbox Android Telemetry and Mapbox Android Core

Mapbox welcomes participation and contributions from everyone.

## Mapbox Android Telemetry

### v3.0.0-beta.3
- Correct UserAgent - [#92](https://github.com/mapbox/mapbox-events-android/pull/92)
- Metadata tag optional - [#91](https://github.com/mapbox/mapbox-events-android/pull/91)
- Adjust header titles in javadoc - [#89](https://github.com/mapbox/mapbox-events-android/pull/89)

### v3.0.0-beta.2
- Fix Duplicate Location Data - [#80](https://github.com/mapbox/mapbox-events-android/pull/80)
- Fix Overwritten Alarm - [#79](https://github.com/mapbox/mapbox-events-android/pull/79)
- Enable Telemetry Manifest Tag - [#77](https://github.com/mapbox/mapbox-events-android/pull/77)
- Remove System.out Call - [#73](https://github.com/mapbox/mapbox-events-android/pull/73)
- Update OkHttp to version 3.10.0 - [#72](https://github.com/mapbox/mapbox-events-android/pull/72)
- Repo Refactoring - [#67](https://github.com/mapbox/mapbox-events-android/pull/67)

### v3.0.0-beta.1
- Environment Enum now package-private - [#62](https://github.com/mapbox/mapbox-events-android/pull/62)
- SessionId rotation interval time in correct range - [#53](https://github.com/mapbox/mapbox-events-android/pull/53)
- Lazy token initialization - [#52](https://github.com/mapbox/mapbox-events-android/pull/52)
- Stop Telemetry when app closed - [#49](https://github.com/mapbox/mapbox-events-android/pull/49)
- Null check for creating MapboxTelemetry - [#48](https://github.com/mapbox/mapbox-events-android/pull/48)
- Store MapboxVendorId - [#47](https://github.com/mapbox/mapbox-events-android/pull/47)
- Store enabledTelemetry Boolean - [#46](https://github.com/mapbox/mapbox-events-android/pull/46)
- Require UserId for AppUserTurnstile - [#44](https://github.com/mapbox/mapbox-events-android/pull/44)
- Require UserId for MapLoadEvent - [#41](https://github.com/mapbox/mapbox-events-android/pull/41)
- Location Engine Priority range check - [#39](https://github.com/mapbox/mapbox-events-android/pull/39)
- Optional Callback for MapboxTelemetry - [#38](https://github.com/mapbox/mapbox-events-android/pull/38)
- Request application context only once - [#33](https://github.com/mapbox/mapbox-events-android/pull/33)
- Default Location Engine Priority se tto No_Power - [#30](https://github.com/mapbox/mapbox-events-android/pull/30)
- Add family type checking to map and navigation event factories - [#26](https://github.com/mapbox/mapbox-events-android/pull/26)
- Remove send a single event method and use sendEvents instead - [#25](https://github.com/mapbox/mapbox-events-android/pull/25)
- Debug Logging API - [#22](https://github.com/mapbox/mapbox-events-android/pull/22)
- Adjust PowerMode Annotations - [#21](https://github.com/mapbox/mapbox-events-android/pull/21)
- Support Multiple user Agent - [#19](https://github.com/mapbox/mapbox-events-android/pull/19)
- Make Location Engine Priority Editable - [#18](https://github.com/mapbox/mapbox-events-android/pull/18)
- Add Navigation Events - [#16](https://github.com/mapbox/mapbox-events-android/pull/16)
- Add AudioType Support - [#13](https://github.com/mapbox/mapbox-events-android/pull/13)
- Check for Location Permission - [#12](https://github.com/mapbox/mapbox-events-android/pull/12)
- Port Location Code from MAS - [#6](https://github.com/mapbox/mapbox-events-android/pull/6)

## Mapbox Android Core

### v0.2.0-beta.3
- Set fastestInterval and smallestDisplacement in AndroidLocationEngine - [#93](https://github.com/mapbox/mapbox-events-android/pull/93)
- Adjust header titles in javadoc - [#89](https://github.com/mapbox/mapbox-events-android/pull/89)

### v0.2.0-beta.2
- Update Proguard Rules - [#81](https://github.com/mapbox/mapbox-events-android/pull/81)

### v0.2.0-beta.1
- Remove LOST from LocationEngineProvider - [#58](https://github.com/mapbox/mapbox-events-android/pull/58)
- Make permissions listener public - [#29](https://github.com/mapbox/mapbox-events-android/pull/29)