# Changelog for Mapbox Android Telemetry and Mapbox Android Core

Mapbox welcomes participation and contributions from everyone.

## Mapbox Android Telemetry

### v3.3.0
- ACCESS_FINE_LOCATION Permission Bug fix - [#227](https://github.com/mapbox/mapbox-events-android/pull/227)
- Fix FileAttachment Queue Bug - [#226](https://github.com/mapbox/mapbox-events-android/pull/226)
- Offline Maps Event - [#193](https://github.com/mapbox/mapbox-events-android/pull/193)

### v3.2.1
- StopTelemetryService obtainBoundInstances bug fix - [#221](https://github.com/mapbox/mapbox-events-android/pull/221)
- Telemetry proguard configuration - [#218](https://github.com/mapbox/mapbox-events-android/pull/218)
- More specific Proguard rules - [#217](https://github.com/mapbox/mapbox-events-android/pull/217)
- Detect BinderProxy - [#209](https://github.com/mapbox/mapbox-events-android/pull/209)

### v3.2.0
- Change fieldIds to fileIds - [#212](https://github.com/mapbox/mapbox-events-android/pull/212)
- Attachment Event - [#205](https://github.com/mapbox/mapbox-events-android/pull/205)
- Null Checks for obtainSharedPreferences - [#202](https://github.com/mapbox/mapbox-events-android/pull/202)
- Create General VisionEvent - [#197](https://github.com/mapbox/mapbox-events-android/pull/197)
- Add Maps UserAgent - [#179](https://github.com/mapbox/mapbox-events-android/pull/179)
- New Hashes and Blacklist - [#151](https://github.com/mapbox/mapbox-events-android/pull/151)

### v3.1.5
- Check Application Context when stopping service - [#190](https://github.com/mapbox/mapbox-events-android/pull/190)
- Crash Fix When Disabling Location Permissions Manually - [#184](https://github.com/mapbox/mapbox-events-android/pull/184)
- Downgrade ArchLifecycleVersion - [#183](https://github.com/mapbox/mapbox-events-android/pull/183)
- Reduce API level for Current State check - [#180](https://github.com/mapbox/mapbox-events-android/pull/180)

### v3.1.4
- Adjust Wakeup - [#173](https://github.com/mapbox/mapbox-events-android/pull/173)
- Remove MAS Dependency - [#164](https://github.com/mapbox/mapbox-events-android/pull/164)

### v3.1.3
- Oreo Background Service Crash Fix - [#157](https://github.com/mapbox/mapbox-events-android/pull/157)
- TelemetryLocationEnabler Null Bug Fix - [#156](https://github.com/mapbox/mapbox-events-android/pull/156)
- Make LocationEvent constructor Package-Private - [#155](https://github.com/mapbox/mapbox-events-android/pull/155)

### v3.1.2
- Service Stability Fix - [#152](https://github.com/mapbox/mapbox-events-android/pull/152)

### v3.1.1
- Missing Application Context - [#144](https://github.com/mapbox/mapbox-events-android/pull/144)
- Close Okhttp Response - [#139](https://github.com/mapbox/mapbox-events-android/pull/139)
- Fix for attempting to unbind an unregistered service - [#136](https://github.com/mapbox/mapbox-events-android/pull/136)

### v3.1.0
- New China Certificate Hashes - [#133](https://github.com/mapbox/mapbox-events-android/pull/133)
- Decouple Core and Telemetry Releases - [#131](https://github.com/mapbox/mapbox-events-android/pull/131)

### v3.0.3
- Remove unnecessary update telemetry state from enable and disable methods - [#126](https://github.com/mapbox/mapbox-events-android/pull/126)

### v3.0.2
- Make `Mapbox-Android-Core` dependency `0.2.0` instead of the current snapshot

### v3.0.1
- Add service running check - [#117](https://github.com/mapbox/mapbox-events-android/pull/117)

### v3.0.0
- Add Alarm unregistering safe check - [#110](https://github.com/mapbox/mapbox-events-android/pull/110)
- Fix Proguard issues - [#109](https://github.com/mapbox/mapbox-events-android/pull/109)
- Generate user id from feedback event data internally - [#108](https://github.com/mapbox/mapbox-events-android/pull/108)
- Fix date fields - [#107](https://github.com/mapbox/mapbox-events-android/pull/107)
- Expose obtain universal unique identifier method publicly - [#106](https://github.com/mapbox/mapbox-events-android/pull/106)
- Remove unnecessary audio type setter from navigation metadata - [#105](https://github.com/mapbox/mapbox-events-android/pull/105)

### v3.0.0-beta.4
- Integration Changes for Navigation Events and Metrics - [#99](https://github.com/mapbox/mapbox-events-android/pull/99)
- Sticky Service Crash Fix - [#98](https://github.com/mapbox/mapbox-events-android/pull/98)
- Save and Restore Circle-Ci Dependencies - [#96](https://github.com/mapbox/mapbox-events-android/pull/96)

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

### v0.2.1
- Update Proguard Rules for GMS - [#189](https://github.com/mapbox/mapbox-events-android/pull/189)
- GoogleLocationEngine lack of priority crash Fix - [#178](https://github.com/mapbox/mapbox-events-android/pull/178)

### v0.2.0
- FastestInterval Documentation Update - [#111](https://github.com/mapbox/mapbox-events-android/pull/111)

### v0.2.0-beta.4
- LocationEngine Singleton Bug - [#100](https://github.com/mapbox/mapbox-events-android/pull/100)

### v0.2.0-beta.3
- Set fastestInterval and smallestDisplacement in AndroidLocationEngine - [#93](https://github.com/mapbox/mapbox-events-android/pull/93)
- Adjust header titles in javadoc - [#89](https://github.com/mapbox/mapbox-events-android/pull/89)

### v0.2.0-beta.2
- Update Proguard Rules - [#81](https://github.com/mapbox/mapbox-events-android/pull/81)

### v0.2.0-beta.1
- Remove LOST from LocationEngineProvider - [#58](https://github.com/mapbox/mapbox-events-android/pull/58)
- Make permissions listener public - [#29](https://github.com/mapbox/mapbox-events-android/pull/29)