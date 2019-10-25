package com.mapbox.annotation.maps.plugin

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapPlugin(val type: MapPluginType)