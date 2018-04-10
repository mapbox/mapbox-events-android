checkstyle:
	./gradlew checkstyle

test:
	./gradlew :libcore:test
	./gradlew :libtelemetry:test

release:
	./gradlew :libcore:assembleRelease
	./gradlew :libtelemetry:assembleRelease

javadoc:
	./gradlew :libcore:javadocrelease
	./gradlew :libtelemetry:javadocrelease

publish:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :libcore:uploadArchives
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :libtelemetry:uploadArchives

publish-local:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :libcore:uploadArchives
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :libtelemetry:uploadArchives

# Creates a dependency graph using Graphviz
MBLIB_ANDROID_GRADLE = ./gradlew --parallel --max-workers=$(JOBS) -Pmapbox.buildtype=$(buildtype)

.PHONY: android-graph
android-graph:
        cd libtelemetry && $(MBLIB_ANDROID_GRADLE) -Pmapbox.abis=none :libtelemetry:generateDependencyGraphMapboxLibraries