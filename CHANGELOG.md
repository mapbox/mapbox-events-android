# Changelog for Mapbox Android Telemetry and Mapbox Android Core

Mapbox welcomes participation and contributions from everyone.

## Mapbox Android Telemetry

### v4.2.0
- Enable event creation outside of telem sdk - [#318](https://github.com/mapbox/mapbox-events-android/pull/318)

### v4.1.1
- Fix binary size checker archive name - [#304](https://github.com/mapbox/mapbox-events-android/pull/304)
- Remove jacoco from release builds - [#303](https://github.com/mapbox/mapbox-events-android/pull/303)

### v4.1.0
- Publish metrics to AWS on master and release only - [#293](https://github.com/mapbox/mapbox-events-android/pull/293)
- Push mobile metrics to loading dock - [#288](https://github.com/mapbox/mapbox-events-android/pull/288)
- Add new CI user template - [#286](https://github.com/mapbox/mapbox-events-android/pull/286)
- Add codecov.yml - [#285](https://github.com/mapbox/mapbox-events-android/pull/285)
- Add codecov badge to readme - [#282](https://github.com/mapbox/mapbox-events-android/pull/282)
- Add codecov.io to CI - [#280](https://github.com/mapbox/mapbox-events-android/pull/280)
- Integrate jacoco plugin with events repo - [#279](https://github.com/mapbox/mapbox-events-android/pull/279)
- Record binary size - [#278](https://github.com/mapbox/mapbox-events-android/pull/278)
- Add lifecycle methods to track lifecycle of parent activity - [#254](https://github.com/mapbox/mapbox-events-android/pull/254)

### v4.0.0
- Update dependencies and cleanup gradle scripts - [#237](https://github.com/mapbox/mapbox-events-android/pull/237)
- Telemetry SDK background location updates + batch locations - [#236](https://github.com/mapbox/mapbox-events-android/pull/236)

### v3.5.7
- Refactor Certificate Blacklist - [#311](https://github.com/mapbox/mapbox-events-android/pull/311)

### v3.5.6
- Add UserAgent header to Config Request - [#297](https://github.com/mapbox/mapbox-events-android/pull/297)
- Add lifecycle methods to track lifecycle of parent activity - [#254](https://github.com/mapbox/mapbox-events-android/pull/254)

### v3.5.5
- Add Vision UserAgent - [#292](https://github.com/mapbox/mapbox-events-android/pull/292)

### v3.5.4
- Fix missed cellNetworkType field in map drag event - [#276](https://github.com/mapbox/mapbox-events-android/pull/276)
- Fix crash due to null context reference in LocationBroadcastReceiver - [#272](https://github.com/mapbox/mapbox-events-android/pull/272)
- Update AlarmManager flag - [#267](https://github.com/mapbox/mapbox-events-android/pull/267)

### v3.5.2
- Fix TelemetryService and applicationContext issue - [#265](https://github.com/mapbox/mapbox-events-android/pull/265)

### v3.5.1
- PermissionChecker NPE - [#241](https://github.com/mapbox/mapbox-events-android/pull/241)

### v3.5.0
- Typo fixes - [#246](https://github.com/mapbox/mapbox-events-android/pull/246)
- Add support for Vision object detection event - [#245](https://github.com/mapbox/mapbox-events-android/pull/245)

### v3.4.0
- Add map.offlineDownload.start and map.offlineDownload.complete events - [#239](https://github.com/mapbox/mapbox-events-android/pull/239)
- Update Load Event with sdkIdentifier and sdkVersion - [#235](https://github.com/mapbox/mapbox-events-android/pull/235)

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

### v1.1.0
- Refactor listener management infrastructure & background mode support - [#290](https://github.com/mapbox/mapbox-events-android/pull/290)
- Add @Nullable annotation to getLastLocation method - [#264](https://github.com/mapbox/mapbox-events-android/pull/264)

### v1.0.0
- Handle potential 'null' last location returned by fused client - [#257](https://github.com/mapbox/mapbox-events-android/pull/257)
- Mapbox Fused Location Engine - [#247](https://github.com/mapbox/mapbox-events-android/pull/247)
- Fix background location engine bugs - [#240](https://github.com/mapbox/mapbox-events-android/pull/240)
- Location API refactor - [#229](https://github.com/mapbox/mapbox-events-android/pull/229)

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