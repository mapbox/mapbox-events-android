checkstyle:
	./gradlew checkstyle

test:
	./gradlew :libtelemetry:downloadSchema
	./gradlew test

test-coverage:
	./gradlew testDebugUnitTestCoverage
	./gradlew testFullDebugUnitTestCoverage

release:
	./gradlew :libcore:assembleRelease
	./gradlew :libtelemetry:assembleRelease

javadoc:
	./gradlew :libcore:javadocrelease
	./gradlew :libtelemetry:javadocFullRelease

publish-core:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :libcore:uploadArchives

publish-telem:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :libtelemetry:uploadArchives

publish-telem-lite:
	export IS_LOCAL_DEVELOPMENT=false; export IS_LITE_RELEASE=true; ./gradlew :libtelemetry:uploadArchives

publish-local-core:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :libcore:uploadArchives

publish-local-telem:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :libtelemetry:uploadArchives

publish-local-telem-lite:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; export IS_LITE_RELEASE=true; ./gradlew :libtelemetry:uploadArchives

graphs:
	./gradlew :libcore:generateDependencyGraphMapboxLibraries
	./gradlew :libtelemetry:generateDependencyGraphMapboxLibraries

.PHONY: brew-java-install
brew-java-install:
	brew tap homebrew/cask-versions
	brew cask install adoptopen./jdk8