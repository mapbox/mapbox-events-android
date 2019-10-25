package com.mapbox.annotation.module

enum class MapboxModuleType(val interfacePackage: String, val interfaceClassName: String) {
  LibraryLoader("com.mapbox.base.module", "LibraryLoader")
}