package com.mapbox.annotation.processor

import androidx.annotation.Keep
import com.mapbox.annotation.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.jvmStatic
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedOptions(ModuleProviderGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
abstract class ModuleProviderGenerator(
  private val moduleProviderPackage: String
) : AbstractProcessor() {

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latest()
  }

  override fun getSupportedAnnotationTypes(): MutableSet<String> {
    return mutableSetOf(getAnnotationClass().name)
  }

  internal abstract fun getAnnotationClass(): Class<Annotation>

  internal abstract fun getModules(roundEnvironment: RoundEnvironment?): List<Module>

  override fun process(
    set: MutableSet<out TypeElement>?,
    roundEnvironment: RoundEnvironment?
  ): Boolean {
    val modules = getModules(roundEnvironment)

    // look for duplicated implementations
    modules.groupBy { it.interfacePackage + it.interfaceClassName }.values.forEach {
      if (it.size > 1) {
        val first = it[0]
        processingEnv.messager.errorMessage {
          """
            Module provider already declared for ${first.interfacePackage}.${first.interfaceClassName}
            in ${first.implPackage}.${first.implClassName}.
          """.trimIndent()
        }
      }
    }

    modules.forEach {
      generateModuleConfiguration(it)
    }
    return true
  }

  private fun generateModuleConfiguration(module: Module) {
    processingEnv.messager.noteMessage { "Generating module configuration class for ${module.implPackage}.${module.implClassName}" }

    val fileBuilder = FileSpec.builder(
      moduleProviderPackage,
      String.format(MODULE_CONFIGURATION_CLASS_NAME_FORMAT, module.name)
    )

    val typeBuilder =
      TypeSpec.objectBuilder(String.format(MODULE_CONFIGURATION_CLASS_NAME_FORMAT, module.name))
        .addKdoc("Configuration provider for ${module.name} module.")
        .addAnnotation(Keep::class)
        .addProperty(
          PropertySpec.builder(
            MODULE_CONFIGURATION_SKIP_VARIABLE, Boolean::class)
            .initializer(module.skipConfiguration.toString())
            .jvmStatic()
            .build()
        )

    if (module.skipConfiguration) {
      // if configuration is skipped, generate only impl package and class paths for manual instantiation
      typeBuilder.addProperty(
        PropertySpec.builder(
          MODULE_CONFIGURATION_SKIPPED_PACKAGE, String::class)
          .initializer("\"${module.implPackage}\"")
          .jvmStatic()
          .build())
      typeBuilder.addProperty(
        PropertySpec.builder(
          MODULE_CONFIGURATION_SKIPPED_CLASS, String::class)
          .initializer("\"${module.implClassName}\"")
          .jvmStatic()
          .build())
    } else {
      // if not skipped, generate module instance provider field that has to be passed by the user
      val providerInterface =
        TypeSpec.interfaceBuilder(MODULE_CONFIGURATION_PROVIDER_CLASS_NAME)
          .addFunction(
            FunSpec.builder(String.format(MODULE_CONFIGURATION_PROVIDER_METHOD_FORMAT, module.name))
              .addModifiers(KModifier.ABSTRACT)
              .returns(ClassName.bestGuess("${module.interfacePackage}.${module.interfaceClassName}"))
              .build()
          )
          .build()

      typeBuilder.addProperty(
        PropertySpec.builder(
          MODULE_CONFIGURATION_PROVIDER_VARIABLE_NAME,
          ClassName.bestGuess(MODULE_CONFIGURATION_PROVIDER_CLASS_NAME).copy(nullable = true)
        )
          .addKdoc(
            """
                Set this dependency provider before initializing any components of the modularized library.
                
                When you're not using the library anymore, you should pass `null` to clean up the provider reference and prevent memory leaks.
              """.trimIndent())
          .mutable()
          .initializer("null")
          .jvmStatic()
          .build()
      )
        .addType(providerInterface)
    }

    fileBuilder.addType(typeBuilder.build())

    val file = fileBuilder.build()

    processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let {
      file.writeTo(Paths.get(it))
    }
  }

  companion object {
    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
  }
}
