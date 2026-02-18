package com.allubie.nana.ui.screens.notes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.*
import com.allubie.nana.ui.theme.NanaIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.allubie.nana.data.model.Label
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import com.allubie.nana.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: NoteEditorViewModel = viewModel(factory = NoteEditorViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableLabels by viewModel.availableLabels.collectAsState()
    val richTextState = rememberRichTextState()
    val context = LocalContext.current
    
    // State for label picker dialog
    var showLabelPicker by remember { mutableStateOf(false) }
    
    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImage(context, it) }
    }
    
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
        }
    }
    
    // Show loading indicator when loading note
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Sync rich text state with viewModel content
    LaunchedEffect(uiState.content) {
        if (uiState.content.isNotEmpty() && richTextState.annotatedString.text.isEmpty()) {
            richTextState.setHtml(uiState.content)
        }
    }
    
    // Track formatting state
    val currentFontWeight = richTextState.currentSpanStyle.fontWeight
    val isBold by remember { derivedStateOf { 
        val weight = richTextState.currentSpanStyle.fontWeight
        weight == FontWeight.Bold || weight == FontWeight.SemiBold || weight == FontWeight.W700 || weight == FontWeight.W600
    } }
    val isItalic by remember { derivedStateOf { richTextState.currentSpanStyle.fontStyle == FontStyle.Italic } }
    // TextDecoration can combine multiple values, so we need to check if it contains the decoration
    val isUnderline by remember { derivedStateOf { 
        richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true 
    } }
    val isStrikethrough by remember { derivedStateOf { 
        richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true 
    } }
    val isUnorderedList by remember { derivedStateOf { richTextState.isUnorderedList } }
    val isOrderedList by remember { derivedStateOf { richTextState.isOrderedList } }
    val isCode by remember { derivedStateOf { richTextState.isCodeSpan } }
    val isHighlight by remember { derivedStateOf { 
        richTextState.currentSpanStyle.background != Color.Unspecified && 
        richTextState.currentSpanStyle.background != Color.Transparent 
    } }
    // Track header styles - check both fontSize and fontWeight
    val isH1 by remember { derivedStateOf { 
        richTextState.currentSpanStyle.fontSize == 28.sp && 
        richTextState.currentSpanStyle.fontWeight == FontWeight.Bold 
    } }
    val isH2 by remember { derivedStateOf { 
        richTextState.currentSpanStyle.fontSize == 22.sp && 
        richTextState.currentSpanStyle.fontWeight == FontWeight.SemiBold 
    } }
    val isH3 by remember { derivedStateOf { 
        richTextState.currentSpanStyle.fontSize == 18.sp && 
        richTextState.currentSpanStyle.fontWeight == FontWeight.SemiBold 
    } }
    
    // Label picker dialog
    if (showLabelPicker) {
        LabelPickerDialog(
            currentLabels = uiState.labels,
            availableLabels = availableLabels,
            onAddLabel = { label ->
                viewModel.addLabel(label)
            },
            onDismiss = { showLabelPicker = false }
        )
    }
    
    // Use Box with manual layout to properly handle window insets
    // This avoids issues with nested Scaffolds and inner padding from MainActivity
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Last edited just now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.updateContent(richTextState.toHtml())
                            viewModel.saveNote()
                            onNavigateBack()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
            
            // Content area - takes remaining space
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title input
                TextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Title",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Build label lookup map
                val labelColorMap = remember(availableLabels) {
                    availableLabels.associateBy { it.name }
                }
                
                // Label chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.labels.forEach { labelName ->
                        val labelData = labelColorMap[labelName]
                        val labelColor = labelData?.let { Color(it.color) } ?: MaterialTheme.colorScheme.primary
                        Surface(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.removeLabel(labelName) },
                            color = labelColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.8f))
                                )
                                Text(
                                    text = labelName,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Surface(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLabelPicker = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Add Label",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Attached images
                if (uiState.images.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.images, key = { it.id }) { image ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(image.imagePath)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Attached image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { viewModel.removeImage(image) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(28.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove image",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Rich Text Editor
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 300.dp),
                    placeholder = {
                        Text(
                            text = "Start typing...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        lineHeight = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Normal
                    ),
                    colors = RichTextEditorDefaults.richTextEditorColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Bottom Formatting Toolbar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderButton(
                            text = "H1",
                            isActive = isH1,
                            onClick = { 
                                richTextState.toggleSpanStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold))
                            }
                        )
                        HeaderButton(
                            text = "H2",
                            isActive = isH2,
                            onClick = { 
                                richTextState.toggleSpanStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold))
                            }
                        )
                        HeaderButton(
                            text = "H3",
                            isActive = isH3,
                            onClick = { 
                                richTextState.toggleSpanStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold))
                            }
                        )
                        
                        ToolbarDivider()
                        
                        FormattingButton(
                            icon = Icons.Outlined.FormatBold,
                            description = "Bold",
                            isActive = isBold,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.FormatItalic,
                            description = "Italic",
                            isActive = isItalic,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.FormatUnderlined,
                            description = "Underline",
                            isActive = isUnderline,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.FormatStrikethrough,
                            description = "Strikethrough",
                            isActive = isStrikethrough,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) }
                        )
                        
                        ToolbarDivider()
                        
                        FormattingButton(
                            icon = Icons.Outlined.BorderColor,
                            description = "Highlight",
                            isActive = isHighlight,
                            onClick = { 
                                if (isHighlight) {
                                    richTextState.removeSpanStyle(SpanStyle(background = Color.Yellow.copy(alpha = 0.4f)))
                                } else {
                                    richTextState.addSpanStyle(SpanStyle(background = Color.Yellow.copy(alpha = 0.4f)))
                                }
                            }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.Code,
                            description = "Code",
                            isActive = isCode,
                            onClick = { richTextState.toggleCodeSpan() }
                        )
                        
                        ToolbarDivider()
                        
                        FormattingButton(
                            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                            description = "Bullet List",
                            isActive = isUnorderedList,
                            onClick = { richTextState.toggleUnorderedList() }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.FormatListNumbered,
                            description = "Numbered List",
                            isActive = isOrderedList,
                            onClick = { richTextState.toggleOrderedList() }
                        )
                        
                        ToolbarDivider()
                        
                        FormattingButton(
                            icon = Icons.Outlined.Image,
                            description = "Add Image",
                            isActive = false,
                            onClick = { imagePicker.launch("image/*") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(24.dp)
            .background(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(1.dp)
            )
    )
}

@Composable
private fun HeaderButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isActive) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormattingButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (isActive) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelPickerDialog(
    currentLabels: List<String>,
    availableLabels: List<Label>,
    onAddLabel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Label", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (availableLabels.isEmpty()) {
                    Text(
                        text = "No labels available. Create labels in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Select Labels",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        maxItemsInEachRow = Int.MAX_VALUE
                    ) {
                        availableLabels
                            .filter { !currentLabels.contains(it.name) }
                            .forEach { label ->
                                CompactLabelChip(
                                    label = label,
                                    onClick = { onAddLabel(label.name) }
                                )
                            }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun CompactLabelChip(
    label: Label,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(label.color).copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(label.color))
            )
            Text(
                text = label.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
