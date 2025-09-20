package com.allubie.nana.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.viewmodel.NotesViewModel
import com.allubie.nana.ui.theme.Spacing
import com.allubie.nana.ui.components.SwipeableItemCard
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedNotesScreen(
    viewModel: NotesViewModel,
    onBackPressed: () -> Unit,
    onNoteClick: (String) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val archivedNotes by viewModel.archivedNotes.collectAsState(initial = emptyList())
    
    val displayNotes = archivedNotes.map { note: com.allubie.nana.data.entity.NoteEntity ->
        Note(
            id = note.id,
            title = note.title,
            content = note.content,
            isPinned = note.isPinned,
            category = note.category,
            createdAt = formatDate(note.createdAt)
        )
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Archived Notes") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (displayNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No archived notes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(Spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(displayNotes) { note ->
                    SwipeableItemCard(
                        isPinned = note.isPinned,
                        showArchive = false,
                        onPin = { viewModel.togglePin(note.id, note.isPinned) },
                        onArchive = { /* Use unarchive instead */ },
                        onDelete = { viewModel.moveToTrash(note.id) }
                    ) {
                        Column {
                            NoteListItem(
                                note = note,
                                onClick = { onNoteClick(note.id) }
                            )
                            // Add unarchive action
                            IconButton(
                                onClick = { viewModel.toggleArchive(note.id, true) }
                            ) {
                                Icon(
                                    Icons.Default.Unarchive,
                                    contentDescription = "Unarchive"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    return when {
        localDateTime.date == now.date -> "Today"
        localDateTime.date.toEpochDays() == now.date.toEpochDays() - 1 -> "Yesterday"
        else -> {
            val daysDiff = now.date.toEpochDays() - localDateTime.date.toEpochDays()
            when {
                daysDiff < 7 -> "$daysDiff days ago"
                daysDiff < 30 -> "${daysDiff / 7} week${if (daysDiff / 7 > 1) "s" else ""} ago"
                else -> "${daysDiff / 30} month${if (daysDiff / 30 > 1) "s" else ""} ago"
            }
        }
    }
}
