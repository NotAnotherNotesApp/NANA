package com.allubie.nana.util

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.allubie.nana.R
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
                appendLine(context.getString(R.string.diag_title))
                appendLine(context.getString(R.string.diag_generated, timestamp))
                appendLine(context.getString(R.string.diag_app_version, appVersion))
                appendLine(context.getString(R.string.diag_package, context.packageName))
                appendLine()
                appendLine(context.getString(R.string.diag_device_info))
                appendLine(context.getString(R.string.diag_manufacturer, Build.MANUFACTURER))
                appendLine(context.getString(R.string.diag_model, Build.MODEL))
                appendLine(context.getString(R.string.diag_brand, Build.BRAND))
                appendLine(context.getString(R.string.diag_device, Build.DEVICE))
                appendLine(context.getString(R.string.diag_hardware, Build.HARDWARE))
                appendLine(context.getString(R.string.diag_android_os, Build.VERSION.RELEASE, Build.VERSION.SDK_INT))
                appendLine(context.getString(R.string.diag_display_build, Build.DISPLAY))
                appendLine()
                appendLine(context.getString(R.string.diag_environment))
                appendLine(context.getString(R.string.diag_locale, Locale.getDefault().toString()))
                appendLine(context.getString(R.string.diag_timezone, TimeZone.getDefault().id))
                appendLine(context.getString(R.string.diag_free_heap, freeMemMb))
                appendLine(context.getString(R.string.diag_max_heap, maxMemMb))
                appendLine(context.getString(R.string.diag_total_heap, totalMemMb))
                appendLine(context.getString(R.string.diag_usable_storage, usableStorageMb))
            }

            val logFile = File(context.cacheDir, "nana_diagnostic_log.txt").apply {
                writeText(logContent)
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, logFile)

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:istiaque.ahmed@outlook.sa")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("istiaque.ahmed@outlook.sa"))
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.template_bug_report_subject, appVersion))
                putExtra(
                    Intent.EXTRA_TEXT,
                    context.getString(R.string.bug_report_email_body)
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                context.startActivity(emailIntent)
            } catch (e: Exception) {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("istiaque.ahmed@outlook.sa"))
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.template_bug_report_subject, appVersion))
                    putExtra(
                        Intent.EXTRA_TEXT,
                        context.getString(R.string.bug_report_email_body)
                    )
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.dialog_send_bug_report_via)))
            }
        } catch (e: Exception) {
            // Fallback to mailto intent if chooser/file provider fails
            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:istiaque.ahmed@outlook.sa")
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.template_bug_report_subject, appVersion))
            }
            context.startActivity(fallbackIntent)
        }
    }
}
