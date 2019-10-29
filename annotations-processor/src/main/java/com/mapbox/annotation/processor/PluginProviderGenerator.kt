package com.mapbox.annotation.processor

import androidx.annotation.Keep
import com.google.auto.service.AutoService
import com.mapbox.annotation.PLUGIN_PROVIDER_CLASS_NAME_PREFIX
import com.mapbox.annotation.PLUGIN_PROVIDER_PACKAGE
import com.mapbox.annotation.maps.plugin.MapPlugin
import com.mapbox.annotation.maps.plugin.MapPluginType
import com.mapbox.annotation.processor.PluginProviderGenerator.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.jvm.jvmStatic
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class PluginProviderGenerator : AbstractProcessor() {
  override fun getSupportedAnnotationTypes(): MutableSet<String> {
    return mutableSetOf(MapPlugin::class.java.name)
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latest()
  }

  override fun process(
    set: MutableSet<out TypeElement>?,
    roundEnvironment: RoundEnvironment?
  ): Boolean {
    val plugins = mutableListOf<Plugin>()

    roundEnvironment?.getElementsAnnotatedWith(MapPlugin::class.java)?.forEach {
      val pack = processingEnv.elementUtils.getPackageOf(it).toString()
      val className = it.simpleName.toString()
      val type = it.getAnnotation(MapPlugin::class.java).type

      plugins.add(Plugin(type, pack, className))
    }

    plugins.forEach { generatePluginRegistry(it) }

    return true
  }

  private fun generatePluginRegistry(plugin: Plugin) {
    processingEnv.messager.noteMessage { "Generating plugin provider entry for ${plugin.implPackage}.${plugin.implClassName}" }

    val file = FileSpec.builder(
      PLUGIN_PROVIDER_PACKAGE,
      "${PLUGIN_PROVIDER_CLASS_NAME_PREFIX}_${plugin.pluginType.name}_${plugin.implClassName}"
    )
      .addImport(plugin.pluginType.interfacePackage, plugin.pluginType.interfaceClassName)
      .addImport(plugin.implPackage, plugin.implClassName)
      .addType(
        TypeSpec.objectBuilder("${PLUGIN_PROVIDER_CLASS_NAME_PREFIX}_${plugin.pluginType.name}_${plugin.implClassName}")
          .addKdoc("Plugin registry creates and instantiates ${plugin.pluginType.name} plugin.")
          .addAnnotation(Keep::class)
          .addFunction(
            FunSpec.builder("create${plugin.pluginType.name}")
              .addKdoc("Create an instance of the ${plugin.pluginType.name} plugin")
              .jvmStatic() // required to invoke a static method via Java reflection
              .addStatement("return ${plugin.implClassName}()")
              .build()
          ).build()
      ).build()

    processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let {
      file.writeTo(Paths.get(it))

      // Generate plugin config file in assets directory
      val projectPath = Paths.get(it).parent.parent.parent.parent.parent
      val assetsDirectory = File(
        projectPath.toFile(),
        "src/main/assets/$KAPT_PLUGIN_ASSET_DIR_NAME/"
      )
      assetsDirectory.deleteRecursively()
      val assetFile = File(
        projectPath.toFile(),
        "src/main/assets/$KAPT_PLUGIN_ASSET_DIR_NAME/$PLUGIN_PROVIDER_PACKAGE.${PLUGIN_PROVIDER_CLASS_NAME_PREFIX}_${plugin.pluginType.name}_${plugin.implClassName}"
      )
      Files.createDirectories(Paths.get(assetFile.parent))
      assetFile.writeText("// Generated Mapbox plugin configuration file, do not remove!")
    }
  }

  companion object {
    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    const val KAPT_PLUGIN_ASSET_DIR_NAME = "mapbox_plugin_config"
  }
}

private class Plugin(
  val pluginType: MapPluginType,
  val implPackage: String,
  val implClassName: String
)