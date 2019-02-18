package com.mapbox.android.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;

/**
 * File utility class
 */
public final class FileUtils {
  private static final String LOG_TAG = "FileUtils";
  private static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 4096;

  private FileUtils() {
  }

  /**
   * Return file from context.getFilesDir()/fileName
   *
   * @param context  application context
   * @param fileName path to the file
   * @return instance of the file object.
   */
  @NonNull
  public static File getFile(@NonNull Context context, @NonNull String fileName) {
    return new File(context.getFilesDir(), fileName);
  }

  /**
   * Read from file.
   *
   * @param file valid reference to the file.
   * @return content read from the file.
   */
  @NonNull
  public static String readFromFile(@NonNull File file) throws FileNotFoundException {
    InputStream inputStream = new FileInputStream(file);
    Reader inputStreamReader = new InputStreamReader(inputStream);
    StringWriter output = new StringWriter();
    try {
      final char[] buffer = new char[DEFAULT_BUFFER_SIZE_IN_BYTES];
      int count;
      while ((count = inputStreamReader.read(buffer)) != -1) {
        output.write(buffer, 0, count);
      }
    } catch (IOException ioe) {
      Log.w(LOG_TAG, ioe.toString());
    } finally {
      try {
        inputStreamReader.close();
      } catch (IOException ioe) {
        Log.e(LOG_TAG, ioe.toString());
      }
    }
    return output.toString();
  }

  /**
   * Write to file.
   *
   * @param file    valid reference to the file.
   * @param content content to write to the file.
   */
  public static void writeToFile(@NonNull File file, @NonNull String content) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
    try {
      writer.write(content);
      writer.flush();
    } catch (IOException ioe) {
      Log.e(LOG_TAG, ioe.toString());
    } finally {
      try {
        writer.close();
      } catch (IOException ioe) {
        Log.e(LOG_TAG, ioe.toString());
      }
    }
  }

  /**
   * Delete file.
   *
   * @param file to delete.
   */
  public static void deleteFile(@NonNull File file) {
    boolean deleted = file.delete();
    if (!deleted) {
      Log.w(LOG_TAG, "Could not delete file: " + file);
    }
  }
}
