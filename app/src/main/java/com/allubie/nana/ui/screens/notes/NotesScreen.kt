package com.allubie.nana.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import com.allubie.nana.ui.theme.NanaIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.allubie.nana.data.model.Note
import com.allubie.nana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToViewer: (Long) -> Unit,
    onNavigateToEditor: (Long?) -> Unit,
    onNavigateToChecklist: (Long?) -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory)
) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    
    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val otherNotes = remember(notes) { notes.filter { !it.isPinned } }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Archived Notes") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToArchive()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Inventory2,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Trash") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToTrash()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteSweep,
                                        contentDescription = null
                                    )
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExpandableFab(
                expanded = showFabMenu,
                onExpandedChange = { showFabMenu = it },
                onNoteClick = {
                    showFabMenu = false
                    onNavigateToEditor(null)
                },
                onListClick = {
                    showFabMenu = false
                    onNavigateToChecklist(null)
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Pinned section - horizontal scroll
            if (pinnedNotes.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column {
                        Text(
                            text = "PINNED",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            pinnedNotes.forEach { note ->
                                PinnedNoteCard(
                                    note = note,
                                    onClick = { 
                                        if (note.isChecklist) {
                                            onNavigateToChecklist(note.id)
                                        } else {
                                            onNavigateToViewer(note.id)
                                        }
                                    },
                                    onLongClick = { },
                                    onPinClick = { viewModel.togglePin(note) },
                                    onArchive = { viewModel.archiveNote(note) },
                                    onDelete = { viewModel.deleteNote(note) },
                                    modifier = Modifier.width(256.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Recent section header
            if (otherNotes.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = "RECENT",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
            }
            
            // Recent notes in masonry grid
            items(otherNotes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onClick = { 
                        if (note.isChecklist) {
                            onNavigateToChecklist(note.id)
                        } else {
                            onNavigateToViewer(note.id)
                        }
                    },
                    onLongClick = { },
                    onPinClick = { viewModel.togglePin(note) },
                    onArchive = { viewModel.archiveNote(note) },
                    onDelete = { viewModel.deleteNote(note) }
                )
            }
            
            // Empty state
            if (notes.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No notes yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to create your first note",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
            
            // Bottom spacer for FAB
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        }
    }
}

// Helper function to strip HTML tags and get plain text, preserving list markers
private fun stripHtml(html: String): String {
    var result = html
    
    // Track list context for ordered lists
    var listCounter = 0
    var inOrderedList = false
    
    // Handle ordered lists - replace <ol> tags and number list items
    result = result.replace(Regex("<ol[^>]*>")) { 
        inOrderedList = true
        listCounter = 0
        ""
    }
    result = result.replace(Regex("</ol>")) {
        inOrderedList = false
        ""
    }
    
    // Handle unordered lists
    result = result.replace(Regex("<ul[^>]*>"), "")
    result = result.replace(Regex("</ul>"), "")
    
    // Replace list items with appropriate markers
    // For simplicity, use bullet for unordered and dash for ordered (since we can't track state in single regex)
    result = result.replace(Regex("<li[^>]*>"), "\n- ")
    result = result.replace(Regex("</li>"), "")
    
    // Handle paragraphs and line breaks
    result = result.replace(Regex("<p[^>]*>"), "\n")
    result = result.replace(Regex("</p>"), "")
    result = result.replace(Regex("<br[^>]*>"), "\n")
    result = result.replace(Regex("<div[^>]*>"), "\n")
    result = result.replace(Regex("</div>"), "")
    
    // Remove remaining HTML tags
    result = result.replace(Regex("<[^>]*>"), "")
    
    // Handle HTML entities
    result = result
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&period;", ".")
        .replace("&comma;", ",")
        .replace("&colon;", ":")
        .replace("&semi;", ";")
        .replace("&excl;", "!")
        .replace("&quest;", "?")
        .replace("&hyphen;", "-")
        .replace("&dash;", "-")
        .replace("&ndash;", "-")
        .replace("&mdash;", "-")
        .replace("&lpar;", "(")
        .replace("&rpar;", ")")
        .replace("&lsqb;", "[")
        .replace("&rsqb;", "]")
        .replace("&lcub;", "{")
        .replace("&rcub;", "}")
        .replace("&num;", "#")
        .replace("&dollar;", "$")
        .replace("&percnt;", "%")
        .replace("&ast;", "*")
        .replace("&plus;", "+")
        .replace("&equals;", "=")
        .replace("&commat;", "@")
        .replace("&sol;", "/")
        .replace("&bsol;", "\\")
        .replace("&verbar;", "|")
        .replace("&tilde;", "~")
        .replace("&circ;", "^")
        .replace("&grave;", "`")
        .replace(Regex("&#(\\d+);")) { matchResult ->
            val code = matchResult.groupValues[1].toIntOrNull()
            if (code != null) code.toChar().toString() else matchResult.value
        }
        .replace(Regex("&#x([0-9a-fA-F]+);")) { matchResult ->
            val code = matchResult.groupValues[1].toIntOrNull(16)
            if (code != null) code.toChar().toString() else matchResult.value
        }
    
    // Clean up multiple newlines and spaces
    result = result.replace(Regex("\\n{3,}"), "\n\n")
    result = result.replace(Regex(" +"), " ")
    result = result.trim()
    
    // Remove leading newline if present
    if (result.startsWith("\n")) {
        result = result.substring(1)
    }
    
    return result
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedNoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPinClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete this note?") },
            text = { Text("\"${note.title.ifEmpty { "Untitled" }}\" will be moved to trash.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Different gradient for different note colors (using theme colors)
    val (startColor, endColor) = when (note.color) {
        1 -> NoteGradientRed
        2 -> NoteGradientPink
        3 -> NoteGradientPurple
        4 -> NoteGradientBlue
        7 -> NoteGradientYellow
        else -> NoteGradientDefault
    }
    
    // Plain text content for preview
    val plainContent = remember(note.content) { stripHtml(note.content) }
    
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(startColor, endColor)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    // Pin icon - aligned to right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checklist indicator
                        if (note.isChecklist) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Checklist,
                                    contentDescription = "Checklist",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "LIST",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        
                        Icon(
                            imageVector = NanaIcons.KeepFilled,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onPinClick() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Title - bold, leading-tight
                    Text(
                        text = note.title.ifEmpty { "Untitled" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Content preview - plain text, line-clamp-2 style
                    Text(
                        text = plainContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSlate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                
                    // Labels and time row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.labels.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                            ) {
                                Text(
                                    text = note.labels.split(",").firstOrNull()?.trim() ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = formatRelativeTime(note.updatedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
        
        // Context menu dropdown
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 8.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Unpin Note") },
                onClick = { 
                    showMenu = false
                    onPinClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = NanaIcons.Keep,
                        contentDescription = null
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Archive") },
                onClick = { 
                    showMenu = false
                    onArchive()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = null
                    )
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                onClick = { 
                    showMenu = false
                    showDeleteConfirmation = true
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPinClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isArchived: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete this note?") },
            text = { Text("\"${note.title.ifEmpty { "Untitled" }}\" will be moved to trash.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Use theme-aware surface color for cards
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    
    // Plain text content for preview
    val plainContent = remember(note.content) { stripHtml(note.content) }
    
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            color = cardColor,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Checklist indicator
                if (note.isChecklist) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Checklist,
                            contentDescription = "Checklist",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "LIST",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                // Title with pin button
                if (note.title.isNotEmpty()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (plainContent.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = plainContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp
                    )
                }
                
                if (note.labels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        note.labels.split(",").take(2).forEach { label ->
                            val labelColor = getLabelColor(label.trim())
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = labelColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = label.trim().uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = labelColor,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Context menu dropdown
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 8.dp, y = 0.dp)
        ) {
            if (isArchived) {
                // Archive screen menu: Unarchive and Delete
                DropdownMenuItem(
                    text = { Text("Unarchive") },
                    onClick = { 
                        showMenu = false
                        onArchive() // onArchive is used as unarchive in archive screen
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Unarchive,
                            contentDescription = null
                        )
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = { 
                        showMenu = false
                        showDeleteConfirmation = true
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            } else {
                // Normal notes screen menu: Pin, Archive, Delete
                DropdownMenuItem(
                    text = { Text(if (note.isPinned) "Unpin Note" else "Pin Note") },
                    onClick = { 
                        showMenu = false
                        onPinClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = NanaIcons.Keep,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Archive") },
                    onClick = { 
                        showMenu = false
                        onArchive()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = null
                        )
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = { 
                        showMenu = false
                        showDeleteConfirmation = true
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> if (days == 1L) "Yesterday" else "${days}d ago"
        weeks < 4 -> "${weeks}w ago"
        months < 12 -> "${months}mo ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

private fun getLabelColor(label: String): Color {
    return when (label.lowercase()) {
        "personal" -> LabelPersonal
        "work" -> LabelWork
        "ideas" -> LabelIdeas
        "important" -> LabelImportant
        "study" -> LabelStudy
        "todo" -> Info
        "journal" -> CategoryShopping
        "recipes" -> CategoryOther
        "travel" -> CategoryEducation
        "health" -> CategoryHealth
        else -> CategoryBills
    }
}

@Composable
private fun ExpandableFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onNoteClick: () -> Unit,
    onListClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "rotation"
    )
    
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mini FABs (visible when expanded)
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(150)) + 
                    scaleIn(initialScale = 0.8f, animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)) + 
                   scaleOut(targetScale = 0.8f, animationSpec = tween(150))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // List option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "List",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onListClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Checklist,
                            contentDescription = "New List",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Note option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Note",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onNoteClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "New Note",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // Main FAB
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Add",
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }
    }
}
