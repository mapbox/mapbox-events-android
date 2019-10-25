package com.mapbox.annotation.maps.plugin

enum class MapPluginType(val interfacePackage: String, val interfaceClassName: String) {
  ViewPlugin("com.mapbox.maps.plugin.base", "ViewPlugin")
}