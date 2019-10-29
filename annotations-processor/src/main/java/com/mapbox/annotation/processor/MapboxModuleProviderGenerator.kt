package com.mapbox.annotation.processor

import com.google.auto.service.AutoService
import com.mapbox.annotation.MODULE_PROVIDER_PACKAGE
import com.mapbox.annotation.module.MapboxModule
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class MapboxModuleProviderGenerator : ModuleProviderGenerator(
  MODULE_PROVIDER_PACKAGE
) {
  override fun getAnnotationClass() = MapboxModule::class.java as Class<Annotation>

  override fun getModules(roundEnvironment: RoundEnvironment?): List<Module> {
    val modules = mutableListOf<Module>()

    roundEnvironment?.getElementsAnnotatedWith(getAnnotationClass())?.forEach { element ->
      if (element is TypeElement) {
        val implPackage = processingEnv.elementUtils.getPackageOf(element).toString()
        val implClassName = element.simpleName.toString()

        val annotation = element.getAnnotation(MapboxModule::class.java)
        val skipConfig = annotation.skipConfiguration
        val type = annotation.type

        modules.add(
          Module(
            skipConfig,
            type.name,
            type.interfacePackage,
            type.interfaceClassName,
            implPackage,
            implClassName
          )
        )
      }
    }

    return modules.toList()
  }
}