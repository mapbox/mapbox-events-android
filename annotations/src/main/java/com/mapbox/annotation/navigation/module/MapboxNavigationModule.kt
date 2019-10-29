package com.mapbox.annotation.navigation.module

/**
 * @param skipConfiguration Defaults to false. If set to true, the generator will not expose the module provider.
 *
 * The SDK will then try to either call an empty constructor of the annotated implementation class or inject predefined Mapbox parameters. The latter is for internal use only.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapboxNavigationModule(val type: MapboxNavigationModuleType,
                                        val skipConfiguration: Boolean = false)