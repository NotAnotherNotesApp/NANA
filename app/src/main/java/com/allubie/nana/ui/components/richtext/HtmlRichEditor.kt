package com.allubie.nana.ui.components.richtext

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.StrikethroughS
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import kotlinx.coroutines.delay

/**
 * Wrapper around library rich editor returning both HTML and plain text.
 */
@Composable
fun HtmlRichEditor(
    modifier: Modifier = Modifier,
    initialHtml: String = "",
    onHtmlChange: (html: String, plain: String) -> Unit,
    toolbar: Boolean = true,
) {
    val state = remember { RichTextState() }
    var initialized by remember { mutableStateOf(false) }

    // Persistent toolbar formatting state (applies to new lines)
    var boldSelected by remember { mutableStateOf(false) }
    var italicSelected by remember { mutableStateOf(false) }
    var underlineSelected by remember { mutableStateOf(false) }
    var strikeSelected by remember { mutableStateOf(false) }
    var headingLevel by remember { mutableStateOf(0) } // 0 = normal, 1 = H1, 2 = H2, 3 = H3
    var currentHeadingSpan by remember { mutableStateOf<SpanStyle?>(null) }

    LaunchedEffect(initialHtml) {
        if (!initialized && initialHtml.isNotBlank()) {
            state.setHtml(initialHtml)
            initialized = true
        }
    }

    fun normalizeHtmlLists(html: String): String {
        if (html.isBlank()) return html
        val style = """
            <style>
            /* Normalize list alignment across devices */
            ul, ol { margin: 0 0 0 0; padding-left: 28px; }
            li { list-style-position: outside; margin: 0; padding-left: 2px; }
            </style>
        """.trimIndent()
        return if ("<style>" in html) html else "$style\n$html"
    }

    LaunchedEffect(state) {
        var previousText = state.toText()
        snapshotFlow { state.toHtml() to state.toText() }
            .collect { (html, plain) ->
                if (plain.length == previousText.length + 1 && plain.lastOrNull() == '\n') {
                    // Reapply active formatting + heading when user starts a new line
                    val headingSpan = currentHeadingSpan
                    ensureActiveFormatting(
                        state,
                        boldSelected,
                        italicSelected,
                        underlineSelected,
                        strikeSelected,
                        headingSpan
                    )
                }
                previousText = plain
                onHtmlChange(normalizeHtmlLists(html), plain)
            }
    }

    if (toolbar) {
        val current = state.currentSpanStyle
        val isBold = current.fontWeight == FontWeight.Bold
        val isItalic = current.fontStyle == FontStyle.Italic
        val isUnderline = current.textDecoration?.contains(TextDecoration.Underline) == true
        val isStrike = current.textDecoration?.contains(TextDecoration.LineThrough) == true

    // No forced sync to avoid interfering with user toggles.

        @Composable
        fun FormatToggle(
            selected: Boolean,
            onClick: () -> Unit,
            icon: androidx.compose.ui.graphics.vector.ImageVector,
            desc: String
        ) {
            // Single darker shade when selected; transparent when not.
            val bg by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                label = "formatBg"
            )
            val contentColor = if (selected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(bg, CircleShape)
            ) {
                Icon(icon, contentDescription = desc, tint = contentColor, modifier = Modifier.size(22.dp))
            }
        }

        @Composable
        fun TextFormatToggle(
            text: String,
            selected: Boolean,
            onClick: () -> Unit,
            desc: String
        ) {
            val bg by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                label = "formatTextBg"
            )
            val contentColor = if (selected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(bg, CircleShape)
            ) {
                androidx.compose.material3.Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 1.dp,
            shadowElevation = 0.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Leading spacer to avoid first button touching border
                Spacer(modifier = Modifier.width(2.dp))
                // Heading group - apply immediately on selection
                TextFormatToggle(
                    text = "Tx",
                    selected = headingLevel == 0,
                    onClick = {
                        if (headingLevel != 0) {
                            currentHeadingSpan?.let { state.toggleSpanStyle(it) }
                            currentHeadingSpan = null
                            headingLevel = 0
                        }
                    },
                    desc = "Normal text"
                )
                TextFormatToggle(
                    text = "H1",
                    selected = headingLevel == 1,
                    onClick = {
                        val newLevel = if (headingLevel == 1) 0 else 1
                        if (newLevel == 0) {
                            currentHeadingSpan?.let { state.toggleSpanStyle(it) }
                            currentHeadingSpan = null
                        } else {
                            val newSpan = deriveHeadingSpan(newLevel)!!
                            currentHeadingSpan?.let { if (it != newSpan) state.toggleSpanStyle(it) }
                            state.toggleSpanStyle(newSpan)
                            currentHeadingSpan = newSpan
                        }
                        headingLevel = newLevel
                    },
                    desc = "Heading 1"
                )
                TextFormatToggle(
                    text = "H2",
                    selected = headingLevel == 2,
                    onClick = {
                        val newLevel = if (headingLevel == 2) 0 else 2
                        if (newLevel == 0) {
                            currentHeadingSpan?.let { state.toggleSpanStyle(it) }
                            currentHeadingSpan = null
                        } else {
                            val newSpan = deriveHeadingSpan(newLevel)!!
                            currentHeadingSpan?.let { if (it != newSpan) state.toggleSpanStyle(it) }
                            state.toggleSpanStyle(newSpan)
                            currentHeadingSpan = newSpan
                        }
                        headingLevel = newLevel
                    },
                    desc = "Heading 2"
                )
                TextFormatToggle(
                    text = "H3",
                    selected = headingLevel == 3,
                    onClick = {
                        val newLevel = if (headingLevel == 3) 0 else 3
                        if (newLevel == 0) {
                            currentHeadingSpan?.let { state.toggleSpanStyle(it) }
                            currentHeadingSpan = null
                        } else {
                            val newSpan = deriveHeadingSpan(newLevel)!!
                            currentHeadingSpan?.let { if (it != newSpan) state.toggleSpanStyle(it) }
                            state.toggleSpanStyle(newSpan)
                            currentHeadingSpan = newSpan
                        }
                        headingLevel = newLevel
                    },
                    desc = "Heading 3"
                )
                // Removed separator for cleaner toolbar
                // Inline style group
                FormatToggle(boldSelected || isBold, {
                    state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    boldSelected = !boldSelected
                }, Icons.Default.FormatBold, "Bold")
                FormatToggle(italicSelected || isItalic, {
                    state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    italicSelected = !italicSelected
                }, Icons.Default.FormatItalic, "Italic")
                FormatToggle(underlineSelected || isUnderline, {
                    state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    underlineSelected = !underlineSelected
                }, Icons.Default.FormatUnderlined, "Underline")
                FormatToggle(strikeSelected || isStrike, {
                    state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    strikeSelected = !strikeSelected
                }, Icons.Default.StrikethroughS, "Strikethrough")
                // Removed divider for cleaner toolbar
                // Lists
                FormatToggle(state.isUnorderedList, { state.toggleUnorderedList() }, Icons.AutoMirrored.Filled.FormatListBulleted, "Bulleted List")
                FormatToggle(state.isOrderedList, { state.toggleOrderedList() }, Icons.Default.FormatListNumbered, "Numbered List")
            }
        }
    // Removed bottom divider to match requested toolbar style
    }
    // Use Surface to align background with screen and avoid bottom seam line.
    // Apply the incoming modifier to the Surface so weight/fill sizing from parents take effect.
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        RichTextEditor(
            state = state,
            modifier = Modifier.fillMaxSize(),
            placeholder = { androidx.compose.material3.Text("Start writing your note...") },
        )
    }
}

private fun ensureActiveFormatting(
    state: RichTextState,
    bold: Boolean,
    italic: Boolean,
    underline: Boolean,
    strike: Boolean,
    headingSpan: SpanStyle? = null
) {
    val current = state.currentSpanStyle
    if (bold && current.fontWeight != FontWeight.Bold) state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
    if (italic && current.fontStyle != FontStyle.Italic) state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
    val hasUnderline = current.textDecoration?.contains(TextDecoration.Underline) == true
    val hasStrike = current.textDecoration?.contains(TextDecoration.LineThrough) == true
    if (underline && !hasUnderline) state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
    if (strike && !hasStrike) state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
    headingSpan?.let { span ->
        // Naive: if font size differs, apply new heading span
        if (current.fontSize != span.fontSize || current.fontWeight != span.fontWeight) {
            state.toggleSpanStyle(span)
        }
    }
}

private fun deriveHeadingSpan(level: Int): SpanStyle? = when (level) {
    1 -> SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
    2 -> SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    3 -> SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
    else -> null
}
