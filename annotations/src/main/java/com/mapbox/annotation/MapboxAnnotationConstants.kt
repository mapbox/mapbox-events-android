@file:JvmName("MapboxAnnotationConstants")

package com.mapbox.annotation

const val MODULE_PROVIDER_PACKAGE = "com.mapbox.module"
const val MODULE_PROVIDER_PACKAGE_NAVIGATION = "$MODULE_PROVIDER_PACKAGE.navigation"

const val MODULE_CONFIGURATION_CLASS_NAME_FORMAT = "Mapbox_%sModuleConfiguration"
const val MODULE_CONFIGURATION_SKIP_VARIABLE = "skipConfiguration"
const val MODULE_CONFIGURATION_SKIPPED_PACKAGE = "implPackage"
const val MODULE_CONFIGURATION_SKIPPED_CLASS = "implClassName"
const val MODULE_CONFIGURATION_PROVIDER_CLASS_NAME = "ModuleProvider"
const val MODULE_CONFIGURATION_PROVIDER_VARIABLE_NAME = "moduleProvider"
const val MODULE_CONFIGURATION_PROVIDER_METHOD_FORMAT = "create%s"

const val PLUGIN_PROVIDER_CLASS_NAME_PREFIX = "Mapbox_PluginProvider"
const val PLUGIN_PROVIDER_PACKAGE = "com.mapbox.plugin"