package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MediaItem
import com.example.CameraViewModel
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val mediaItems = uiState.galleryItems
    val context = LocalContext.current

    var selectedItem by remember { mutableStateOf<MediaItem?>(null) }
    var isInfoPanelVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MAGIC VAULT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("gallery_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSlate,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkSlate,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (mediaItems.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0x0FFFFFFF))
                            .border(BorderStroke(1.dp, Color(0x1AFFFFFF)), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "No Media",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "VAULT IS EMPTY",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Photos and video recordings taken by Qyra MagicCam will be stored securely in this sandbox gallery.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            } else {
                // Media grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(mediaItems, key = { it.id }) { item ->
                        GalleryGridItem(
                            item = item,
                            onClick = { selectedItem = item }
                        )
                    }
                }
            }

            // Fullscreen Viewport Overlay
            AnimatedVisibility(
                visible = selectedItem != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedItem?.let { item ->
                    FullscreenViewer(
                        item = item,
                        isInfoVisible = isInfoPanelVisible,
                        onToggleInfo = { isInfoPanelVisible = !isInfoPanelVisible },
                        onClose = {
                            selectedItem = null
                            isInfoPanelVisible = false
                        },
                        onDelete = {
                            viewModel.deleteMediaItem(context, item)
                            selectedItem = null
                            isInfoPanelVisible = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryGridItem(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF10141D))
            .border(BorderStroke(0.5.dp, Color(0x1AFFFFFF)), RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick
            )
    ) {
        AsyncImage(
            model = item.uriString,
            contentDescription = "Gallery Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlays: Video indicator or Watermark badge
        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "Video",
                    tint = NeonRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val min = item.durationSeconds / 60
                val sec = item.durationSeconds % 60
                Text(
                    text = String.format(Locale.US, "%02d:%02d", min, sec),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Mini mode bubble
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x99000000))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = item.mode,
                color = if (item.mode == "Q-ZOOM") NeonCyan else Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FullscreenViewer(
    item: MediaItem,
    isInfoVisible: Boolean,
    onToggleInfo: () -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = SimpleDateFormat("MMMM dd, yyyy - h:mm a", Locale.getDefault())
    val dateString = formatter.format(Date(item.id))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onClose) // Click backdrop to go back
    ) {
        // Core high-quality photo
        AsyncImage(
            model = item.uriString,
            contentDescription = "Fullscreen Media",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )

        // "Taken by QYRA" Brand watermark stamped over image base (if enabled)
        if (item.watermark.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(vertical = 12.dp, horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Taken by QYRA",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "${item.mode} // ${item.zoom} // ${item.resolution}",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Top Glassy Toolbar (Translucent overlay)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x4D000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Info toggle Button
                    IconButton(
                        onClick = onToggleInfo,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isInfoVisible) NeonCyan.copy(alpha = 0.4f) else Color(0x4D000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Metadata Info",
                            tint = if (isInfoVisible) NeonCyan else Color.White
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x4D000000))
                            .testTag("delete_media_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Capture",
                            tint = NeonRed
                        )
                    }
                }
            }
        }

        // Dynamic Technical Metadata Detail card (Liquid Glass styled sliding panel)
        AnimatedVisibility(
            visible = isInfoVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xE6111622))
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {} // Prevent click-through closing
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "EXIF TECHNICAL DETAILS",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    MetadataRow(icon = Icons.Default.CameraAlt, label = "Capture Mode", value = item.mode)
                    MetadataRow(icon = Icons.Default.ZoomIn, label = "Focal Zoom Level", value = item.zoom)
                    MetadataRow(icon = Icons.Default.Info, label = "Save Resolution", value = item.resolution)
                    MetadataRow(icon = Icons.Default.CalendarToday, label = "Recorded Date", value = dateString)
                    MetadataRow(icon = Icons.Default.Share, label = "Author Badge", value = "Taken by QYRA")

                    if (item.isVideo) {
                        MetadataRow(
                            icon = Icons.Default.PlayCircleFilled,
                            label = "Recorded Length",
                            value = "${item.durationSeconds} Seconds @ 60fps"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x1A00E5FF))
                            .border(BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "This photo was generated using Qyra MagicCam's 8K sensor array simulation, running with real-time hardware-accelerated watermarks.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
