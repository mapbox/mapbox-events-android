package com.mapbox.annotation.navigation.module

enum class MapboxNavigationModuleType(val interfacePackage: String, val interfaceClassName: String) {
  HybridRouter("com.mapbox.navigation.base.route", "Router"),
  OffboardRouter("com.mapbox.navigation.base.route", "Router"),
  OnboardRouter("com.mapbox.navigation.base.route", "Router"),
  DirectionsSession("com.mapbox.navigation.base.route", "DirectionsSession"),
  TripNotification("com.mapbox.navigation.base.trip", "TripNotification"),
  TripService("com.mapbox.navigation.base.trip", "TripService"),
  TripSession("com.mapbox.navigation.base.trip", "TripSession"),
  Logger("com.mapbox.navigation.base.logger", "Logger")
}