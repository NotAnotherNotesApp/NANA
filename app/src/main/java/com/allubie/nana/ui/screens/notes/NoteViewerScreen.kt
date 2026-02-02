package com.allubie.nana.ui.screens.notes

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.allubie.nana.ui.theme.NanaIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.allubie.nana.data.model.NoteImage
import com.allubie.nana.ui.theme.*
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import java.text.SimpleDateFormat
import java.util.*

private fun getLabelIcon(label: String): ImageVector {
    return when (label.lowercase()) {
        "study" -> Icons.Default.School
        "work" -> Icons.Default.Work
        "personal" -> Icons.Default.Person
        "ideas" -> Icons.Default.Lightbulb
        "important" -> Icons.Default.Star
        "meeting" -> Icons.Default.Groups
        "project" -> Icons.Default.Folder
        "research" -> Icons.Default.Search
        "todo" -> Icons.Default.CheckCircle
        "reference" -> Icons.Default.Book
        else -> Icons.Default.Tag
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} min ago"
        diff < 86400_000 -> {
            val hours = diff / 3600_000
            if (hours == 1L) "1 hour ago" else "$hours hours ago"
        }
        diff < 604800_000 -> {
            val days = diff / 86400_000
            if (days == 1L) "Yesterday" else "$days days ago"
        }
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewerScreen(
    noteId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: () -> Unit,
    viewModel: NoteEditorViewModel = viewModel(factory = NoteEditorViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedImage by remember { mutableStateOf<NoteImage?>(null) }
    
    // Share note function
    fun shareNote() {
        val shareText = buildString {
            if (uiState.title.isNotEmpty()) {
                appendLine(uiState.title)
                appendLine()
            }
            // Strip HTML tags for plain text sharing
            val plainContent = uiState.content
                .replace(Regex("<[^>]*>"), "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
            append(plainContent)
        }
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, uiState.title.ifEmpty { "Shared Note" })
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Note")
        context.startActivity(shareIntent)
    }
    
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }
    
    val richTextState = rememberRichTextState()
    
    LaunchedEffect(uiState.content) {
        if (uiState.content.isNotEmpty()) {
            try {
                richTextState.setHtml(uiState.content)
            } catch (e: Exception) {
                // Fallback for plain text content
            }
        }
    }
    
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { shareNote() }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share"
                        )
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            // Pin/Unpin option
                            DropdownMenuItem(
                                text = { 
                                    Text(if (uiState.isPinned) "Unpin" else "Pin") 
                                },
                                onClick = {
                                    viewModel.togglePin()
                                    showMoreMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = NanaIcons.Keep,
                                        contentDescription = null
                                    )
                                }
                            )
                            HorizontalDivider()
                            // Copy content option
                            DropdownMenuItem(
                                text = { Text("Copy text") },
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                                    val plainContent = uiState.content
                                        .replace(Regex("<[^>]*>"), "")
                                        .replace("&nbsp;", " ")
                                        .replace("&amp;", "&")
                                        .replace("&lt;", "<")
                                        .replace("&gt;", ">")
                                    val fullText = if (uiState.title.isNotEmpty()) {
                                        "${uiState.title}\n\n$plainContent"
                                    } else {
                                        plainContent
                                    }
                                    val clip = android.content.ClipData.newPlainText("Note", fullText)
                                    clipboard?.setPrimaryClip(clip)
                                    showMoreMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = null
                                    )
                                }
                            )
                            HorizontalDivider()
                            // Archive option
                            DropdownMenuItem(
                                text = { Text("Archive") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.archiveNote {
                                        onNavigateBack()
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Archive,
                                        contentDescription = null
                                    )
                                }
                            )
                            HorizontalDivider()
                            // Delete option
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Delete",
                                        color = MaterialTheme.colorScheme.error
                                    ) 
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteDialog = true
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToEditor,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Note",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 80.dp  // Space for FAB
            )
        ) {
            // Title section
            item {
                Text(
                    text = uiState.title.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 40.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Metadata row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Last edited
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Edited ${formatTimestamp(uiState.updatedAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                }
                
                // Labels row - show all labels with icons
                if (uiState.labels.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.labels.forEach { label ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getLabelIcon(label),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Content section - render rich text
            item {
                if (uiState.content.isNotEmpty()) {
                    RichText(
                        state = richTextState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f)
                        )
                    )
                }
            }
            
            // Images section
            if (uiState.images.isNotEmpty()) {
                items(uiState.images, key = { it.id }) { image ->
                    NoteImageCard(
                        image = image,
                        onExpandImage = { expandedImage = image },
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete this note?") },
            text = { 
                Text(
                    if (uiState.title.isNotEmpty()) 
                        "\"${uiState.title}\" will be moved to trash."
                    else 
                        "This note will be moved to trash."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteNote {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Fullscreen image viewer
    expandedImage?.let { image ->
        FullscreenImageViewer(
            image = image,
            onDismiss = { expandedImage = null }
        )
    }
}

@Composable
private fun NoteImageCard(
    image: NoteImage,
    onExpandImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpandImage() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Box {
            AsyncImage(
                model = image.imagePath,
                contentDescription = "Note image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
            )
            
            // Gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            ),
                            startY = 100f
                        )
                    )
            )
            
            // Zoom icon - now clickable
            Surface(
                onClick = onExpandImage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.4f)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Expand",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
            
            // Caption at bottom
            Text(
                text = "Figure ${image.position + 1}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun FullscreenImageViewer(
    image: NoteImage,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = image.imagePath,
            contentDescription = "Full screen image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
                .clickable(enabled = false) { }
        )
        
        // Close button
        Surface(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .statusBarsPadding(),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp)
            )
        }
        
        // Caption at bottom
        Text(
            text = "Figure ${image.position + 1}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        )
    }
}
