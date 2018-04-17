checkstyle:
	./gradlew checkstyle

test:
	./gradlew :libtelemetry:test

release:
	./gradlew :libtelemetry:assembleRelease

javadoc:
	./gradlew :libtelemetry:javadocrelease

publish:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :libtelemetry:uploadArchives

publish-local:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :libtelemetry:uploadArchives

graphs:
	./gradlew :libcore:generateDependencyGraphMapboxLibraries
	./gradlew :libtelemetry:generateDependencyGraphMapboxLibraries
