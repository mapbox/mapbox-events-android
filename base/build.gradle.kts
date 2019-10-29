plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdkVersion(28)
  defaultConfig {
    minSdkVersion(14)
    targetSdkVersion(28)
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.41")
  implementation("androidx.annotation:annotation:1.1.0")
}