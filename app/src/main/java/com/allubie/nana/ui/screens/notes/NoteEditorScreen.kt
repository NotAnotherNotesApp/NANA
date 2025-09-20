package com.allubie.nana.ui.screens.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
// FlowRow removed to force single-line categories
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Context
import android.net.Uri
import com.allubie.nana.ui.theme.Spacing
import com.allubie.nana.ui.components.richtext.*
import com.allubie.nana.ui.components.SimpleChecklistEditor
import com.allubie.nana.ui.components.ChecklistItem
import com.allubie.nana.ui.components.ImagePicker
import com.allubie.nana.ui.components.CleanTextEditor
import com.allubie.nana.utils.FileUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String? = null,
    initialTitle: String = "",
    initialContent: String = "",
    initialRichContent: String = "",
    initialNoteType: String = "text",
    // New: ensure HTML content can be hydrated on edit
    initialHtmlContent: String = "",
    // New: pass-through existing category from caller when editing
    initialCategory: String? = null,
    onSave: (title: String, content: String, richContent: String, htmlContent: String, noteType: String, category: String?) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var noteType by remember { mutableStateOf(if (initialNoteType == "image") "text" else initialNoteType) }
    var category by remember { mutableStateOf(initialCategory) }
    val categories = remember { listOf("Personal", "Study", "Work", "Ideas", "Important") }
    
    // For checklist items (simple interface)
    var checklistItems by remember { mutableStateOf(emptyList<ChecklistItem>()) }
    
    // For rich text blocks (complex interface)
    var blocks by remember { 
        mutableStateOf(
            try {
                if (initialRichContent.isNotBlank()) {
                    RichTextConverter.jsonToBlocks(initialRichContent)
                } else if (initialContent.isNotBlank()) {
                    RichTextConverter.plainTextToBlocks(initialContent)
                } else {
                    listOf(RichTextBlock(
                        id = "block_0",
                        type = BlockType.TEXT,
                        text = ""
                    ))
                }
            } catch (e: Exception) {
                listOf(RichTextBlock(
                    id = "block_0",
                    type = BlockType.TEXT,
                    text = ""
                ))
            }
        )
    }
    // New rich HTML state derived via HtmlRichEditor
    var htmlContent by remember { mutableStateOf(initialHtmlContent) }
    var plainContentDerived by remember { mutableStateOf(initialContent) }
    
    // Image notes temporarily disabled
    // var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Reference to the context
    val context = LocalContext.current    // Initialize checklist items from existing data
    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(noteType) {
        if (initialized) return@LaunchedEffect
        initialized = true
        when (noteType) {
            "text" /* , "image" */ -> {
                // For text and image notes, preserve existing structure if available
                if (initialRichContent.isNotBlank()) {
                    // Try to load from rich content first to preserve structure
                    val richBlocks = RichTextConverter.jsonToBlocks(initialRichContent)
                    if (richBlocks.size == 1 && richBlocks[0].type == BlockType.TEXT) {
                        // Single text block - load as text
                        checklistItems = listOf(ChecklistItem(text = richBlocks[0].text))
                    } else {
                        // Multiple blocks - convert to checklist to preserve data
                        checklistItems = richBlocks.map { block ->
                            ChecklistItem(
                                id = block.id,
                                text = block.text,
                                isCompleted = block.isCompleted
                            )
                        }
                    }
                } else if (initialContent.isNotBlank()) {
                    // Check if content has multiple lines or checklist format
                    if (initialContent.contains('\n') || 
                        initialContent.contains('☑') || 
                        initialContent.contains('☐')) {
                        // Multi-line or checklist content - parse as blocks
                        val richBlocks = RichTextConverter.plainTextToBlocks(initialContent)
                        checklistItems = richBlocks.map { block ->
                            ChecklistItem(
                                id = block.id,
                                text = block.text,
                                isCompleted = block.isCompleted
                            )
                        }
                    } else {
                        // Single line content
                        checklistItems = listOf(ChecklistItem(text = initialContent))
                    }
                } else {
                    checklistItems = listOf(ChecklistItem())
                }
            }
            "list", "checklist" -> {
                // For list notes, use rich content parsing
                if (initialRichContent.isNotBlank()) {
                    val richBlocks = RichTextConverter.jsonToBlocks(initialRichContent)
                    checklistItems = richBlocks.map { block ->
                        ChecklistItem(
                            id = block.id,
                            text = block.text,
                            isCompleted = block.isCompleted
                        )
                    }
                } else if (initialContent.isNotBlank()) {
                    val richBlocks = RichTextConverter.plainTextToBlocks(initialContent)
                    checklistItems = richBlocks.map { block ->
                        ChecklistItem(
                            id = block.id,
                            text = block.text,
                            isCompleted = block.isCompleted
                        )
                    }
                } else {
                    checklistItems = listOf(ChecklistItem())
                }
            }
            else -> {
                // Default handling
                checklistItems = listOf(ChecklistItem())
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (noteId == null) "New Note" else "Edit Note",
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                // Derive save payload depending on note type to avoid empty saves
                                when (noteType) {
                                    "list", "checklist" -> {
                                        // Convert checklist items to RichTextBlocks
                                        val checklistBlocks = checklistItems.mapIndexed { idx, it ->
                                            com.allubie.nana.ui.components.richtext.RichTextBlock(
                                                id = it.id,
                                                type = com.allubie.nana.ui.components.richtext.BlockType.CHECKLIST,
                                                text = it.text,
                                                isCompleted = it.isCompleted,
                                                listIndex = idx
                                            )
                                        }
                                        val json = com.allubie.nana.ui.components.richtext.RichTextConverter.blocksToJson(checklistBlocks)
                                        val plain = com.allubie.nana.ui.components.richtext.RichTextConverter.blocksToPlainText(checklistBlocks)
                                        val finalPlain = if (plain.isNotBlank()) plain else initialContent
                                        // No HTML editor for checklist; keep HTML empty
                                        onSave(title, finalPlain, json, /*html*/"", noteType, category)
                                    }
                                    // "image" -> { /* disabled */ }
                                    else -> {
                                        // Text/Image notes: rely on HTML editor outputs, fallback to blocks/plain
                                        val blocksPlain = com.allubie.nana.ui.components.richtext.RichTextConverter.blocksToPlainText(blocks)
                                        val blocksJson = com.allubie.nana.ui.components.richtext.RichTextConverter.blocksToJson(blocks)
                                        val finalPlain = listOf(plainContentDerived, blocksPlain, initialContent).firstOrNull { it.isNotBlank() } ?: ""
                                        val finalHtml = if (htmlContent.isNotBlank()) htmlContent else initialHtmlContent
                                        onSave(title, finalPlain, blocksJson, finalHtml, noteType, category)
                                    }
                                }
                                onBack()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) { Text("Save") }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
            // Always use simple interface to match reference exactly
            when (noteType) {
                "list", "checklist" -> {
                    // Checklist interface for list notes
                    SimpleChecklistEditor(
                        title = title,
                        onTitleChange = { title = it },
                        items = checklistItems,
                        onItemsChange = { newItems -> 
                            checklistItems = newItems
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.screenPadding)
                    )
                }
                "text" -> {
                    // Clean text editor without any boxes or borders
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        CategorySection(categories, category) { category = it }
                        // Title Field
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            )
                        )
                        
                        // Html Rich Editor for text notes
                        HtmlRichEditor(
                            initialHtml = initialHtmlContent,
                            onHtmlChange = { html, plain ->
                                htmlContent = html
                                plainContentDerived = plain
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
                else -> {
                    // Default to simple text interface
                    SimpleChecklistEditor(
                        title = title,
                        onTitleChange = { title = it },
                        items = checklistItems,
                        onItemsChange = { newItems -> 
                            checklistItems = newItems
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.screenPadding),
                        placeholder = "Add note content..."
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySection(categories: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEach { cat ->
                    val selectedState = cat == selected
                    FilterChip(
                        selected = selectedState,
                        onClick = { onSelect(if (selectedState) null else cat) },
                        label = {
                            Text(
                                cat,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedState,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        leadingIcon = null
                    )
                }
            }
        }
    }
}
