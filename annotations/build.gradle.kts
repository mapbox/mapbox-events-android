plugins {
  kotlin("jvm")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.41")

  /**
   * Required for @Keep annotation by the annotation-processor and the resulting generated code
   */
  api("androidx.annotation:annotation:1.1.0")
}