package com.mapbox.annotation.processor

import androidx.annotation.Keep
import com.google.auto.service.AutoService
import com.mapbox.annotation.MODULE_PROVIDER_CLASS_NAME_PREFIX
import com.mapbox.annotation.MODULE_PROVIDER_PACKAGE
import com.mapbox.annotation.module.MapboxModule
import com.mapbox.annotation.module.MapboxModuleType
import com.mapbox.annotation.processor.ModuleProviderGenerator.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.jvm.jvmStatic
import java.nio.file.Paths
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ModuleProviderGenerator : AbstractProcessor() {

  override fun getSupportedAnnotationTypes(): MutableSet<String> {
    return mutableSetOf(MapboxModule::class.java.name)
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latest()
  }

  override fun process(
    set: MutableSet<out TypeElement>?,
    roundEnvironment: RoundEnvironment?
  ): Boolean {
    val modules = mutableListOf<Module>()

    roundEnvironment?.getElementsAnnotatedWith(MapboxModule::class.java)?.forEach {
      val pack = processingEnv.elementUtils.getPackageOf(it).toString()
      val className = it.simpleName.toString()
      val type = it.getAnnotation(MapboxModule::class.java).type

      modules.find { module -> module.moduleType == type }?.let { module ->
        processingEnv.messager.errorMessage {
          """
            Module implementation already declared: $type
            Previously declared implementation in ${module.implPackage}.${module.implClassName}.
            Requested implementation in $pack.$className
          """.trimIndent()
        }
      }

      modules.add(Module(type, pack, className))
    }

    modules.forEach { generateModuleProvider(it) }

    return true
  }

  private fun generateModuleProvider(module: Module) {
    processingEnv.messager.noteMessage { "Generating module provider entry for ${module.implPackage}.${module.implClassName}" }

    val file = FileSpec.builder(
        MODULE_PROVIDER_PACKAGE,
      "${MODULE_PROVIDER_CLASS_NAME_PREFIX}_${module.moduleType.name}"
    )
      .addImport(module.moduleType.interfacePackage, module.moduleType.interfaceClassName)
      .addImport(module.implPackage, module.implClassName)
      .addType(
        TypeSpec.objectBuilder("${MODULE_PROVIDER_CLASS_NAME_PREFIX}_${module.moduleType.name}")
          .addKdoc("Module provider creates and instantiates ${module.moduleType.name} module.")
          .addAnnotation(Keep::class)
          .addFunction(
            FunSpec.builder("create${module.moduleType.name}")
              .addKdoc("Create an instance of the ${module.moduleType.name} module")
              .jvmStatic() // required to invoke a static method via Java reflection
              .addStatement("return ${module.implClassName}()")
              .build()
          )
          .build()
      )
      .build()

    processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let {
      file.writeTo(Paths.get(it))
    }
  }

  companion object {
    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
  }
}

private class Module(
    val moduleType: MapboxModuleType,
    val implPackage: String,
    val implClassName: String
)