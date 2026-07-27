package com.allubie.nana.util

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BugReportUtils {

    fun sendBugReportWithLogs(context: Context, appVersion: String) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(Date())
            val runtime = Runtime.getRuntime()
            val freeMemMb = runtime.freeMemory() / (1024 * 1024)
            val maxMemMb = runtime.maxMemory() / (1024 * 1024)
            val totalMemMb = runtime.totalMemory() / (1024 * 1024)
            val usableStorageMb = context.filesDir.usableSpace / (1024 * 1024)

            val logContent = buildString {
                appendLine("Nana App Diagnostic Log")
                appendLine("Generated: $timestamp")
                appendLine("App Version: $appVersion")
                appendLine("Package: ${context.packageName}")
                appendLine()
                appendLine("Device Information")
                appendLine("Manufacturer: ${Build.MANUFACTURER}")
                appendLine("Model: ${Build.MODEL}")
                appendLine("Brand: ${Build.BRAND}")
                appendLine("Device: ${Build.DEVICE}")
                appendLine("Hardware: ${Build.HARDWARE}")
                appendLine("Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine("Display Build: ${Build.DISPLAY}")
                appendLine()
                appendLine("Environment & Metrics")
                appendLine("Locale: ${Locale.getDefault()}")
                appendLine("Timezone: ${TimeZone.getDefault().id}")
                appendLine("Free Heap Mem: ${freeMemMb}MB")
                appendLine("Max Heap Mem: ${maxMemMb}MB")
                appendLine("Total Heap Mem: ${totalMemMb}MB")
                appendLine("Usable Storage: ${usableStorageMb}MB")
            }

            val logFile = File(context.cacheDir, "nana_diagnostic_log.txt").apply {
                writeText(logContent)
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, logFile)

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("istiaque.ahmed@outlook.sa"))
                putExtra(Intent.EXTRA_SUBJECT, "Nana Bug Report (v$appVersion)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Please describe the bug or issue you experienced below:\n\n\n\n[Diagnostic log nana_diagnostic_log.txt attached automatically]"
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(emailIntent, "Send Bug Report via...")
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback to mailto intent if chooser/file provider fails
            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:istiaque.ahmed@outlook.sa")
                putExtra(Intent.EXTRA_SUBJECT, "Nana Bug Report (v$appVersion)")
            }
            context.startActivity(fallbackIntent)
        }
    }
}
