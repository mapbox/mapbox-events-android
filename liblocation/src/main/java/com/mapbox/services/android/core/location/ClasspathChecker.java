package com.mapbox.services.android.core.location;


class ClasspathChecker {

  boolean hasDependencyOnClasspath(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException ignored) {
      // Empty
    }
    return false;
  }
}
