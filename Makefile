checkstyle:
	./gradlew checkstyle

test-run:
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

# Build Driver Targets

.PHONY: prep
prep:
	echo "TODO prep Android Project"

.PHONY: build
build:
	echo "TODO build Android Project"

.PHONY: test
test:
	echo "TODO test Android Project"

.PHONY: docs
docs:
	echo "TODO docs Android Project"

.PHONY: pack
pack:
	echo "TODO pack Android Project"

.PHONY: farm
farm:
	echo "TODO farm Android Project"

.PHONY: stage
stage:
	echo "TODO stage Android Project"

.PHONY: clean
clean:
	echo "TODO clean Android Project"
