package com.mapbox.annotation.processor

/**
 * Holds information needed for configuration class generation.
 * @param skipConfiguration if true, generate only package and class names for empty constructor invocation or Mapbox default module arguments injection
 * @param name module type parameter name
 */
internal data class Module(
  val skipConfiguration: Boolean,
  val name: String,
  val interfacePackage: String,
  val interfaceClassName: String,
  val implPackage: String,
  val implClassName: String
)