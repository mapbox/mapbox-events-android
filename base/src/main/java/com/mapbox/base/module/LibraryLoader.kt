package com.mapbox.base.module

import android.content.Context

/**
 * Library loader definition.
 */
interface LibraryLoader {

  /**
   * Load native library given a context and library name.
   */
  fun load(context: Context, libraryName: String)
}