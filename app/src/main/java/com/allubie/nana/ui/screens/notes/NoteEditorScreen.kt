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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.allubie.nana.data.model.Label
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import androidx.compose.ui.res.stringResource
import com.allubie.nana.R
import com.allubie.nana.util.sanitizeHtmlForEditor
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.ClipEntry
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableLabels by viewModel.availableLabels.collectAsStateWithLifecycle()
    val richTextState = rememberRichTextState()
    val context = LocalContext.current
    
    // State for label picker dialog
    var showLabelPicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collectLatest {
            onNavigateBack()
        }
    }
    
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
    
    // Sync rich text state with viewModel content
    LaunchedEffect(uiState.content) {
        if (uiState.content.isNotEmpty() && richTextState.annotatedString.text.isEmpty()) {
            richTextState.setHtml(sanitizeHtmlForEditor(uiState.content))
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
                        text = stringResource(R.string.status_edited_just_now),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.updateContent(sanitizeHtmlForEditor(richTextState.toHtml()))
                            viewModel.saveNote()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_save),
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
                            text = stringResource(R.string.hint_title),
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
                                text = stringResource(R.string.dialog_add_label),
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
                        items(uiState.images, key = { if (it.id > 0) it.id else it.imagePath }) { image ->
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
                                    contentDescription = stringResource(R.string.cd_attached_image),
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
                                        contentDescription = stringResource(R.string.cd_remove_image),
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Rich Text Editor with sanitized clipboard for dark/light mode contrast
                val currentClipboard = LocalClipboard.current
                val sanitizedClipboard = remember(currentClipboard) {
                    object : Clipboard by currentClipboard {
                        override suspend fun getClipEntry(): ClipEntry? {
                            val entry = currentClipboard.getClipEntry() ?: return null
                            val clipData = entry.clipData
                            if (clipData.itemCount > 0) {
                                val item = clipData.getItemAt(0)
                                val htmlText = item.htmlText
                                if (htmlText != null) {
                                    val sanitizedHtml = sanitizeHtmlForEditor(htmlText)
                                    val label = clipData.description.label ?: "text"
                                    val sanitizedClipData = android.content.ClipData.newHtmlText(label, item.text ?: "", sanitizedHtml)
                                    return ClipEntry(sanitizedClipData)
                                }
                            }
                            return entry
                        }
                    }
                }

                CompositionLocalProvider(LocalClipboard provides sanitizedClipboard) {
                    RichTextEditor(
                        state = richTextState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 300.dp),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.hint_start_typing),
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
                }
                
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
                            text = stringResource(R.string.heading_h1),
                            isActive = isH1,
                            onClick = { 
                                richTextState.toggleSpanStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold))
                            }
                        )
                        HeaderButton(
                            text = stringResource(R.string.heading_h2),
                            isActive = isH2,
                            onClick = { 
                                richTextState.toggleSpanStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold))
                            }
                        )
                        HeaderButton(
                            text = stringResource(R.string.heading_h3),
                            isActive = isH3,
                            onClick = { 
                                richTextState.toggleSpanStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold))
                            }
                        )
                        
                        ToolbarDivider()
                        
                        FormattingButton(
                            icon = Icons.Outlined.FormatBold,
                            description = stringResource(R.string.cd_format_bold),
                            isActive = isBold,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.FormatItalic,
                            description = stringResource(R.string.cd_format_italic),
                            isActive = isItalic,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.FormatUnderlined,
                            description = stringResource(R.string.cd_format_underline),
                            isActive = isUnderline,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.FormatStrikethrough,
                            description = stringResource(R.string.cd_format_strikethrough),
                            isActive = isStrikethrough,
                            onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) }
                        )
                        
                        ToolbarDivider()
                        
                        FormattingButton(
                            icon = Icons.Outlined.BorderColor,
                            description = stringResource(R.string.cd_format_highlight),
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
                            description = stringResource(R.string.cd_format_code),
                            isActive = isCode,
                            onClick = { richTextState.toggleCodeSpan() }
                        )
                        
                        ToolbarDivider()
                        
                        FormattingButton(
                            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                            description = stringResource(R.string.cd_format_bullet_list),
                            isActive = isUnorderedList,
                            onClick = { richTextState.toggleUnorderedList() }
                        )
                        FormattingButton(
                            icon = Icons.Outlined.FormatListNumbered,
                            description = stringResource(R.string.cd_format_numbered_list),
                            isActive = isOrderedList,
                            onClick = { richTextState.toggleOrderedList() }
                        )
                        
                        ToolbarDivider()
                        
                        FormattingButton(
                            icon = Icons.Outlined.Image,
                            description = stringResource(R.string.cd_add_image),
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
        title = { Text(stringResource(R.string.dialog_add_label), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (availableLabels.isEmpty()) {
                    Text(
                        text = stringResource(R.string.msg_no_labels),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.label_select_labels),
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
                Text(stringResource(R.string.action_done))
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
