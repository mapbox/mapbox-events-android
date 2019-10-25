plugins {
  kotlin("jvm")
}

dependencies {
  implementation(Dependencies.kotlin)

  /**
   * Required for @Keep annotation by the annotation-processor and the resulting generated code
   */
  api(Dependencies.annotations)
}