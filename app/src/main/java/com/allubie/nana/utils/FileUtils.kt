package com.allubie.nana.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object FileUtils {
    private const val TAG = "FileUtils"
    private const val IMAGES_DIR = "note_images"

    /**
     * Copies a content URI to a persistent internal file. Even if the URI already points to our
     * FileProvider (e.g. temp camera file in cache), we still migrate it into the dedicated
     * note_images directory unless it's already there. This avoids losing images stored only in cache.
     */
    fun copyUriToLocalFile(context: Context, sourceUri: Uri?): Uri? {
        if (sourceUri == null) return null
        return try {
            val uriString = sourceUri.toString()

            // Determine if already a persistent note image (path contains our images dir)
            val alreadyPersistent = uriString.contains("${context.packageName}.fileprovider") &&
                uriString.contains(IMAGES_DIR)
            if (alreadyPersistent) {
                Log.d(TAG, "Reusing existing persistent URI: $sourceUri")
                return sourceUri
            }

            // Ensure destination directory
            val imagesDir = File(context.filesDir, IMAGES_DIR).apply { if (!exists()) mkdirs() }

            val extension = getFileExtension(context, sourceUri) ?: "jpg"
            val filename = "image_${UUID.randomUUID()}.$extension"
            val destFile = File(imagesDir, filename)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: run {
                Log.e(TAG, "Failed to open input stream for $sourceUri")
                return null
            }

            val persisted = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destFile
            )
            Log.d(TAG, "Persisted image: $persisted (from $sourceUri)")
            persisted
        } catch (e: Exception) {
            Log.e(TAG, "Error copying URI to local file", e)
            null
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        return try {
            when {
                uri.scheme == "content" -> {
                    val mimeType = context.contentResolver.getType(uri)
                    MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                }
                else -> {
                    val path = uri.path ?: return null
                    val ext = MimeTypeMap.getFileExtensionFromUrl(path)
                    if (ext.isNullOrBlank()) null else ext
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not determine extension for $uri", e)
            null
        }
    }
}
