---
name: 🚀 Release
about: Mobile Telemetry Release Issue
title: "\U0001F680 [Core/Telemetry] Release x.x.x"
labels: "release"
assignees:
---

**Timeline**
When is the release scheduled?

**Pre-release Testing**
- [ ] Manual downstream test app test with snapshot
- [ ] Manual offline test with telemetry test app
- [ ] Check for memory leaks
- [ ] Run manual battery tests

**Release Checklist**
- [ ] Update snapshot version `x.x.x-SNAPSHOT`
- [ ] Update `CHANGELOG.md`
- [ ] Tag `x.x.x` in GitHub
- [ ] Make GitHub Release
- [ ] Publish `x.x.x` artifact to Maven Central

**Post Release**
- [ ] Add release javadocs to appropriate spot in /android-docs `api` folder @langsmith
- Create integration tickets in downstream SDK repositories
        [ ] Maps: https://github.com/mapbox/mapbox-gl-native-android
        [ ] Unity: https://github.com/mapbox/mapbox-unity-sdk/
        [ ] Navigation: https://github.com/mapbox/mapbox-navigation-android
        [ ] Vision: https://github.com/mapbox/mapbox-vision-android
        [ ] Search: https://github.com/mapbox/mapbox-search-android

/cc: @mapbox/mobile-telemetry
