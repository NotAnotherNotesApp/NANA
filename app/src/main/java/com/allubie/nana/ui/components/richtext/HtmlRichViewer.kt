package com.allubie.nana.ui.components.richtext

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.ui.graphics.toArgb
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

@Composable
fun HtmlRichViewer(modifier: Modifier = Modifier, html: String) {
    val state = remember { RichTextState() }
    // Compute current theme text color inside composition
    val colorHex = run {
        val argb = MaterialTheme.colorScheme.onSurface.toArgb()
        val rgb = argb and 0x00FFFFFF
        String.format("#%06X", rgb)
    }

    fun normalizeHtmlWithColor(incoming: String, colorHex: String): String {
        if (incoming.isBlank()) return incoming
        val style = """
            <style>
            body { color: $colorHex; }
            ul, ol { margin: 0 0 0 0; padding-left: 28px; }
            li { list-style-position: outside; margin: 0; padding-left: 2px; }
            </style>
        """.trimIndent()
        return if ("<style>" in incoming) incoming else "$style\n$incoming"
    }

    LaunchedEffect(html, colorHex) {
        state.setHtml(normalizeHtmlWithColor(html, colorHex))
    }
    // Use app typography/color so text is visible in dark/light themes
    ProvideTextStyle(MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)) {
        // Using editor in read-only mode (no toolbar, interactions minimal)
        RichTextEditor(
            state = state,
            modifier = modifier,
            readOnly = true,
            placeholder = {}
        )
    }
}
