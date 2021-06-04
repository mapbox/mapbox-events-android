checkstyle:
	./gradlew checkstyle

test:
	./gradlew :libtelemetry:downloadSchema
	./gradlew test

test-coverage:
	./gradlew testDebugUnitTestCoverage

release:
	./gradlew :libcore:assembleRelease
	./gradlew :libtelemetry:assembleRelease

javadoc:
	./gradlew :libcore:javadocrelease
	./gradlew :libtelemetry:javadocRelease

publish-core:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :libcore:uploadArchives

publish-telem:
	export IS_LOCAL_DEVELOPMENT=false; ./gradlew :libtelemetry:uploadArchives

publish-local-core:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :libcore:uploadArchives

publish-local-telem:
	# This publishes to ~/.m2/repository/com/mapbox/mapboxsdk
	export IS_LOCAL_DEVELOPMENT=true; ./gradlew :libtelemetry:uploadArchives

.PHONY: publish-core-to-sdk-registry
publish-core-to-sdk-registry:
	./gradlew :libcore:mapboxSDKRegistryUpload;

.PHONY: publish-telemetry-to-sdk-registry
publish-telemetry-to-sdk-registry:
	./gradlew :libtelemetry:mapboxSDKRegistryUpload;

.PHONY: publish-all-to-sdk-registry
publish-all-to-sdk-registry:
	./gradlew mapboxSDKRegistryUpload;

graphs:
	./gradlew :libcore:generateDependencyGraphMapboxLibraries
	./gradlew :libtelemetry:generateDependencyGraphMapboxLibraries

.PHONY: brew-java-install
brew-java-install:
	brew tap homebrew/cask-versions
	brew cask install adoptopen./jdk8
