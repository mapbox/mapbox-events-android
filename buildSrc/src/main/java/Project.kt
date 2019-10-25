object AndroidVersions {
    const val minSdkVersion = 19
    const val targetSdkVersion = 28
    const val compileSdkVersion = 28
}

object Dependencies {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val annotations = "androidx.annotation:annotation:${Versions.androidX}"
    const val kotlinPoet = "com.squareup:kotlinpoet:${Versions.kotlinPoet}"
    const val serviceProvider = "com.google.auto.service:auto-service:${Versions.serviceProvider}"
}

private object Versions {
    const val kotlin = "1.3.41"
    const val androidX = "1.1.0"
    const val kotlinPoet = "1.3.0"
    const val serviceProvider = "1.0-rc6"
}