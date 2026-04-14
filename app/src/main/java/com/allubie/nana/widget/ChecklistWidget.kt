package com.allubie.nana.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.allubie.nana.MainActivity
import com.allubie.nana.R
import com.allubie.nana.data.NanaDatabase
import com.allubie.nana.data.model.ChecklistItem
import com.allubie.nana.widget.NanaWidgetColorProviders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

private val ChecklistItemIdKey = ActionParameters.Key<Long>("checklist_item_id")

/**
 * A checklist home screen widget following the Android platform-samples reference pattern.
 *
 * Uses [SizeMode.Exact] for precise rendering control, [Scaffold] + [TitleBar] for proper
 * Material 3 widget structure, and reactive [collectAsState] for live data updates.
 */
class ChecklistWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = NanaDatabase.getDatabase(context)

        // Load note + items in a single IO dispatch for fast initial render
        val (checklistNote, initialItems) = withContext(Dispatchers.IO) {
            val note = try {
                db.noteDao().getAllNotesSync()
                    .filter { it.isChecklist && !it.isDeleted && !it.isArchived }
                    .maxByOrNull { it.updatedAt }
            } catch (_: Exception) { null }

            val items = if (note != null) {
                try {
                    db.checklistItemDao().getAllItemsSync()
                        .filter { it.noteId == note.id }
                        .sortedBy { it.position }
                } catch (_: Exception) { emptyList() }
            } else emptyList<ChecklistItem>()

            Pair(note, items)
        }

        // Reactive Flow — collectAsState observes this and recomposes on DB changes
        val itemsFlow = if (checklistNote != null) {
            db.checklistItemDao().getItemsForNote(checklistNote.id)
        } else {
            flowOf(emptyList())
        }

        provideContent {
            val items by itemsFlow.collectAsState(initial = initialItems)

            GlanceTheme(colors = NanaWidgetColorProviders) {
                ChecklistWidgetContent(
                    context = context,
                    noteId = checklistNote?.id,
                    noteTitle = checklistNote?.title,
                    items = items,
                )
            }
        }
    }
}

@Composable
private fun ChecklistWidgetContent(
    context: Context,
    noteId: Long?,
    noteTitle: String?,
    items: List<ChecklistItem>,
) {
    val openAction = actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", if (noteId != null) "notes/checklist/$noteId" else "notes/checklist/-1")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

    Scaffold(
        backgroundColor = GlanceTheme.colors.widgetBackground,
        titleBar = {
            TitleBar(
                startIcon = ImageProvider(R.drawable.ic_widget_checkbox_checked),
                title = noteTitle?.ifEmpty { "Checklist" } ?: "Checklist",
                iconColor = GlanceTheme.colors.widgetBackground,
                textColor = GlanceTheme.colors.onSurface,
                actions = {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_add),
                        contentDescription = "Open checklist",
                        modifier = GlanceModifier
                            .size(40.dp)
                            .padding(8.dp)
                            .clickable(openAction),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary)
                    )
                }
            )
        }
    ) {
        if (noteId == null || items.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize().clickable(openAction),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (noteId == null) "No checklists yet.\nTap to create one."
                    else "All done!",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(items, itemId = { it.id }) { item ->
                    ChecklistItemRow(item)
                }
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(item: ChecklistItem) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(
                if (item.isChecked) R.drawable.ic_widget_checkbox_checked
                else R.drawable.ic_widget_checkbox_unchecked
            ),
            contentDescription = if (item.isChecked) "Uncheck" else "Check",
            modifier = GlanceModifier
                .size(40.dp)
                .padding(8.dp)
                .clickable(
                    actionRunCallback<ToggleChecklistItemAction>(
                        actionParametersOf(ChecklistItemIdKey to item.id)
                    )
                ),
            colorFilter = ColorFilter.tint(
                if (item.isChecked) GlanceTheme.colors.primary
                else GlanceTheme.colors.onSurfaceVariant
            )
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = item.text,
            style = TextStyle(
                color = if (item.isChecked) GlanceTheme.colors.onSurfaceVariant
                else GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough
                else TextDecoration.None
            ),
            maxLines = 2,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}

class ToggleChecklistItemAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val itemId = parameters[ChecklistItemIdKey] ?: return
        val db = NanaDatabase.getDatabase(context)
        val dao = db.checklistItemDao()

        val item = dao.getItemById(itemId) ?: return
        dao.updateItem(item.copy(isChecked = !item.isChecked))
        ChecklistWidget().update(context, glanceId)
    }
}

class ChecklistWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = ChecklistWidget()
}
