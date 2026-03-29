package com.allubie.nana.widget

import android.content.Context
import android.content.Intent
import android.text.Html
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allubie.nana.MainActivity
import com.allubie.nana.R
import com.allubie.nana.data.NanaDatabase
import com.allubie.nana.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import com.allubie.nana.widget.NanaWidgetColorProviders

class RecentNotesWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = NanaDatabase.getDatabase(context)
        val initialNotes = withContext(Dispatchers.IO) {
            runCatching { db.noteDao().getRecentNonChecklistNotesOnce(3) }
                .getOrDefault(emptyList())
        }

        provideContent {
            val notes by db.noteDao().getRecentNonChecklistNotes(3)
                .collectAsState(initial = initialNotes)

            GlanceTheme(colors = NanaWidgetColorProviders) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(12.dp)
                        .cornerRadius(R.dimen.widget_outer_radius)
                ) {
                    // Header
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RECENT NOTES",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (notes.isEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notes yet",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    } else {
                        notes.forEachIndexed { index, note ->
                            NoteCard(context, note)
                            if (index < notes.lastIndex) {
                                Spacer(modifier = GlanceModifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(context: Context, note: Note) {
    val plainContent = stripHtml(note.content)
    val preview = if (plainContent.length > 50) plainContent.take(50) + "..." else plainContent

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(R.dimen.widget_inner_radius)
            .clickable(
                actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        putExtra("navigate_to", "notes/view/${note.id}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                )
            )
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                maxLines = 1
            )
            if (preview.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = preview,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

private fun stripHtml(html: String): String {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim()
}

class RecentNotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = RecentNotesWidget()
}
