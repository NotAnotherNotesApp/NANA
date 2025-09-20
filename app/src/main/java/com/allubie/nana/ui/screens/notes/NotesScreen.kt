package com.allubie.nana.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.allubie.nana.ui.components.SwipeableItemCard
import com.allubie.nana.ui.theme.Spacing
import com.allubie.nana.ui.viewmodel.NotesViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val category: String? = null,
    val noteType: String = "text",
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNoteClick: (String) -> Unit = {},
    onCreateNote: (String) -> Unit = {},
    onArchivedNotesClick: () -> Unit = {},
    onRecycleBinClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val allNotes by viewModel.notes.collectAsState(initial = emptyList())

    var isSearchVisible by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFabExpanded by remember { mutableStateOf(false) }

    val displayNotes = allNotes.map { note: com.allubie.nana.data.entity.NoteEntity ->
        Note(
            id = note.id.toString(),
            title = note.title,
            content = note.content,
            isPinned = note.isPinned,
            category = note.category,
            noteType = note.noteType,
            createdAt = formatDate(note.createdAt)
        )
    }

    val filteredNotes = remember(displayNotes, searchQuery) {
        if (searchQuery.isBlank()) displayNotes else displayNotes.filter { n ->
            n.title.contains(searchQuery, ignoreCase = true) || n.content.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) focusRequester.requestFocus() else searchQuery = ""
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = com.allubie.nana.R.string.notes_title)) },
                actions = {
                    IconButton(onClick = {
                        isSearchVisible = !isSearchVisible
                        if (!isSearchVisible) keyboardController?.hide()
                    }) {
                        Icon(
                            if (isSearchVisible) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (isSearchVisible) stringResource(id = com.allubie.nana.R.string.action_close) else stringResource(id = com.allubie.nana.R.string.action_search)
                        )
                    }
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(id = com.allubie.nana.R.string.action_more))

                        val menuShape = RoundedCornerShape(12.dp)
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = menuShape,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .clip(menuShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), menuShape)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = com.allubie.nana.R.string.notes_archived)) },
                                onClick = { showOverflowMenu = false; onArchivedNotesClick() }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = com.allubie.nana.R.string.notes_recycle_bin)) },
                                onClick = { showOverflowMenu = false; onRecycleBinClick() }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = com.allubie.nana.R.string.action_settings)) },
                                onClick = { showOverflowMenu = false; onSettingsClick() }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            // Keep-style FAB menu: pill buttons + circular toggle
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.End) {
                AnimatedVisibility(visible = isFabExpanded, enter = fadeIn(tween(180)) + expandVertically(), exit = fadeOut(tween(140)) + shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.End) {
                        Surface(
                            onClick = { onCreateNote("text"); isFabExpanded = false },
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Text(text = stringResource(id = com.allubie.nana.R.string.fab_add_text), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Surface(
                            onClick = { onCreateNote("list"); isFabExpanded = false },
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Text(text = stringResource(id = com.allubie.nana.R.string.fab_add_list), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    if (isFabExpanded) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(id = com.allubie.nana.R.string.fab_menu_close))
                    } else {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(id = com.allubie.nana.R.string.fab_menu_open))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (isSearchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(id = com.allubie.nana.R.string.notes_search_hint)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Filled.Close, contentDescription = stringResource(id = com.allubie.nana.R.string.action_clear)) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenPadding).focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { keyboardController?.hide() })
                    )
                    Spacer(Modifier.height(Spacing.itemSpacing))
                }

                if (filteredNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (searchQuery.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Notes, contentDescription = null, modifier = Modifier.size(Spacing.iconMassive), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                Spacer(Modifier.height(Spacing.screenPadding))
                                Text(text = stringResource(id = com.allubie.nana.R.string.notes_no_notes), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(Spacing.itemSpacing))
                                Text(text = stringResource(id = com.allubie.nana.R.string.notes_no_notes_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Text(text = stringResource(id = com.allubie.nana.R.string.notes_no_notes_found), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(Spacing.screenPadding), modifier = Modifier.fillMaxSize()) {
                        items(filteredNotes) { note ->
                            SwipeableItemCard(
                                isPinned = note.isPinned,
                                onPin = { viewModel.togglePin(note.id, note.isPinned) },
                                onArchive = { viewModel.archiveNote(note.id) },
                                onDelete = { viewModel.moveToTrash(note.id) }
                            ) {
                                NoteListItem(note = note) { onNoteClick(note.id) }
                            }
                            Spacer(Modifier.height(Spacing.itemSpacing))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteListItem(note: Note, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(Spacing.cornerMedium)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(Spacing.cornerMedium)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = note.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (note.isPinned) {
                        Icon(imageVector = Icons.Filled.PushPin, contentDescription = stringResource(id = com.allubie.nana.R.string.action_pin), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Spacing.iconSmall))
                    }
                }
                Spacer(Modifier.height(Spacing.extraSmall))
                Text(text = note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (note.noteType) {
                                "list", "checklist" -> Icons.AutoMirrored.Filled.List
                                "image" -> Icons.Filled.Image
                                else -> Icons.AutoMirrored.Filled.Notes
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Spacing.iconSmall)
                        )
                        note.category?.let { category ->
                            Box(
                                modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = Spacing.itemSpacing, vertical = Spacing.extraSmall)
                            ) { Text(text = category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false) }
                        }
                        Text(text = note.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
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
        localDateTime.date == now.date -> stringResourceNotComposableToday()
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

// Non-composable helper to avoid ambient read in pure function
private fun stringResourceNotComposableToday(): String = "Today"
