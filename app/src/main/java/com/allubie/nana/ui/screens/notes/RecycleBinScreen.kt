package com.allubie.nana.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.viewmodel.NotesViewModel
import com.allubie.nana.ui.theme.Spacing
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    viewModel: NotesViewModel,
    onBackPressed: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val deletedNotes by viewModel.deletedNotes.collectAsState(initial = emptyList())
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var showRestoreSelectedDialog by remember { mutableStateOf(false) }
    var selectedNotes by remember { mutableStateOf(setOf<String>()) }
    
    val displayNotes = deletedNotes.map { note: com.allubie.nana.data.entity.NoteEntity ->
        Note(
            id = note.id,
            title = note.title,
            content = note.content,
            isPinned = note.isPinned,
            category = note.category,
            createdAt = formatDate(note.createdAt)
        )
    }
    
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty Recycle Bin") },
            text = { Text("Are you sure you want to permanently delete all notes in the recycle bin? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.emptyTrash()
                        showEmptyTrashDialog = false
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showRestoreSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreSelectedDialog = false },
            title = { Text("Restore Selected Notes") },
            text = { Text("Are you sure you want to restore ${selectedNotes.size} selected note${if (selectedNotes.size > 1) "s" else ""}?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        selectedNotes.forEach { noteId ->
                            viewModel.restoreFromTrash(noteId)
                        }
                        selectedNotes = setOf()
                        showRestoreSelectedDialog = false
                    }
                ) {
                    Text("Restore Selected")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreSelectedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            title = { Text("Delete Selected Notes") },
            text = { Text("Are you sure you want to permanently delete ${selectedNotes.size} selected note${if (selectedNotes.size > 1) "s" else ""}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        selectedNotes.forEach { noteId ->
                            deletedNotes.find { it.id == noteId }?.let { note ->
                                viewModel.deleteNotePermanently(note)
                            }
                        }
                        selectedNotes = setOf()
                        showDeleteSelectedDialog = false
                    }
                ) {
                    Text("Delete Selected")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Recycle Bin") },
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
                    text = "Recycle bin is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Action buttons at the top
                if (displayNotes.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.medium)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedNotes.isNotEmpty()) {
                            Button(
                                onClick = { showRestoreSelectedDialog = true }
                            ) {
                                Text(
                                    "Restore Selected (${selectedNotes.size})",
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = { showDeleteSelectedDialog = true }
                            ) {
                                Text(
                                    "Delete Selected (${selectedNotes.size})",
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        Button(
                            onClick = { showEmptyTrashDialog = true }
                        ) {
                            Text(
                                "Delete All",
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Notes list
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayNotes) { note ->
                        RecycleBinNoteCard(
                            note = note,
                            isSelected = selectedNotes.contains(note.id),
                            onSelectionChange = { isSelected ->
                                selectedNotes = if (isSelected) {
                                    selectedNotes + note.id
                                } else {
                                    selectedNotes - note.id
                                }
                            }
                        )
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

@Composable
fun RecycleBinNoteCard(
    note: Note,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                // Selection checkbox
                IconButton(
                    onClick = { onSelectionChange(!isSelected) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Selected" else "Not selected",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Note content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (note.title.isNotBlank()) note.title else "Untitled",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = note.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
