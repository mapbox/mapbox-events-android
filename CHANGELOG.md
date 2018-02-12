# Changelog for Mapbox Android Telemetry and Mapbox Android Core

Mapbox welcomes participation and contributions from everyone.

## Mapbox Android Telemetry

### v3.0.0-beta.1
- Port Location Code from MAS - [#6](https://github.com/mapbox/mapbox-events-android/pull/6)
- Check for Location Permission - [#12](https://github.com/mapbox/mapbox-events-android/pull/12)
- Add AudioType Support - [#13](https://github.com/mapbox/mapbox-events-android/pull/13)
- Add Navigation Events - [#16](https://github.com/mapbox/mapbox-events-android/pull/16)
- Make Location Engine Priority Editable - [#18](https://github.com/mapbox/mapbox-events-android/pull/18)
- Support Multiple user Agent - [#19](https://github.com/mapbox/mapbox-events-android/pull/19)
- Adjust PowerMode Annotations - [#21](https://github.com/mapbox/mapbox-events-android/pull/21)
- Debug Logging API - [#22](https://github.com/mapbox/mapbox-events-android/pull/22)
- Remove send a single event method and use sendEvents instead - [#25](https://github.com/mapbox/mapbox-events-android/pull/25)
- Add family type checking to map and navigation event factories - [#26](https://github.com/mapbox/mapbox-events-android/pull/26)
- Default Location Engine Priority se tto No_Power - [#30](https://github.com/mapbox/mapbox-events-android/pull/30)
- Request application context only once - [#33](https://github.com/mapbox/mapbox-events-android/pull/33)
- Optional Callback for MapboxTelemetry - [#38](https://github.com/mapbox/mapbox-events-android/pull/38)
- Location Engine Priority range check - [#39](https://github.com/mapbox/mapbox-events-android/pull/39)
- Require UserId for MapLoadEvent - [#41](https://github.com/mapbox/mapbox-events-android/pull/41)
- Require UserId for AppUserTurnstile - [#44](https://github.com/mapbox/mapbox-events-android/pull/44)
- Store enabledTelemetry Boolean - [#46](https://github.com/mapbox/mapbox-events-android/pull/46)
- Store MapboxVendorId - [#47](https://github.com/mapbox/mapbox-events-android/pull/47)
- Null check for creating MapboxTelemetry - [#48](https://github.com/mapbox/mapbox-events-android/pull/48)
- Stop Telemetry when app closed - [#49](https://github.com/mapbox/mapbox-events-android/pull/49)
- Lazy token initialization - [#52](https://github.com/mapbox/mapbox-events-android/pull/52)
- SessionId rotation interval time in correct range - [#53](https://github.com/mapbox/mapbox-events-android/pull/53)
- Lazy token initialization - [#52](https://github.com/mapbox/mapbox-events-android/pull/52)
- Environment Enum now package-private - [#62](https://github.com/mapbox/mapbox-events-android/pull/62)

## Mapbox Android Core

### v0.2.0-beta.1

- Make permissions listener public - [#29](https://github.com/mapbox/mapbox-events-android/pull/29)
- Remove LOST from LocationEngineProvider - [#58](https://github.com/mapbox/mapbox-events-android/pull/58)