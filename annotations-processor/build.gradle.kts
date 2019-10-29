plugins {
  kotlin("jvm")
  kotlin("kapt")
}

dependencies {
  implementation(project(":annotations"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.41")
  implementation("com.squareup:kotlinpoet:1.3.0")
  implementation("com.google.auto.service:auto-service:1.0-rc6")
  kapt("com.google.auto.service:auto-service:1.0-rc6")
}