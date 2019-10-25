package com.mapbox.annotation.module

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapboxModule(val type: MapboxModuleType)