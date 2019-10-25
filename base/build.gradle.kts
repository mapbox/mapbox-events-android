plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdkVersion(AndroidVersions.compileSdkVersion)
  defaultConfig {
    minSdkVersion(AndroidVersions.minSdkVersion)
    targetSdkVersion(AndroidVersions.targetSdkVersion)
  }
}

dependencies {
  implementation(Dependencies.kotlin)
  implementation(Dependencies.annotations)
}