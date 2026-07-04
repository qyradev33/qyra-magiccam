package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.delay
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.CameraUiState
import com.example.CameraViewModel
import com.example.ShootingMode
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassyIconButton
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonRed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToModStore: () -> Unit,
    onNavigateToGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Handle standard camera permissions securely
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    var flashTriggered by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlate)
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Real Viewfinder integrating physical camera layout
            CameraXViewfinder(
                isFrontCamera = uiState.isFrontCamera,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission request panel or scenic animated preview for testing/simulators
            PermissionScenicFallback(
                onGrantPermission = { cameraPermissionState.launchPermissionRequest() },
                selectedMode = viewModel.currentMode,
                state = uiState
            )
        }

        // Viewfinder Grid Overlay (rule of thirds)
        if (uiState.settings.isGridEnabled) {
            GridViewfinderOverlay()
        }

        // Apply visual shaders matching active mode / store filters
        ModeShaderFilterOverlay(mode = viewModel.currentMode, state = uiState)

        // Central Focus Brackets Overlay (Sophisticated Dark custom visual element)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(160.dp)
                .border(BorderStroke(0.5.dp, Color(0x1AFFFFFF)), shape = RoundedCornerShape(16.dp))
        ) {
            // Top-Left corner bracket
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(16.dp)
                    .border(BorderStroke(2.dp, NeonCyan), shape = RoundedCornerShape(topStart = 6.dp))
            )
            // Top-Right corner bracket
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .border(BorderStroke(2.dp, NeonCyan), shape = RoundedCornerShape(topEnd = 6.dp))
            )
            // Bottom-Left corner bracket
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(16.dp)
                    .border(BorderStroke(2.dp, NeonCyan), shape = RoundedCornerShape(bottomStart = 6.dp))
            )
            // Bottom-Right corner bracket
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .border(BorderStroke(2.dp, NeonCyan), shape = RoundedCornerShape(bottomEnd = 6.dp))
            )
        }

        // Top Status Header and Quick toggles
        TopStatusRow(
            state = uiState,
            onToggleFlash = {
                val nextFlash = when (uiState.isFlashOn) {
                    "OFF" -> "ON"
                    "ON" -> "AUTO"
                    "AUTO" -> "TORCH"
                    else -> "OFF"
                }
                viewModel.setFlashState(nextFlash)
            },
            onFlipCamera = { viewModel.toggleCameraFacing() },
            onNavigateToSettings = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        )

        // Floating Samsung-style Super Zoom PIP Stabilizer Viewport
        if (uiState.zoomValue >= 10f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 110.dp, end = 16.dp)
                    .size(90.dp, 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x99000000))
                    .border(BorderStroke(1.dp, NeonCyan), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Outer wider angle context preview
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Wide PIP",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize(0.6f)
                )
                // Tracking box showing where the extreme zoom is aimed
                Box(
                    modifier = Modifier
                        .size(18.dp, 12.dp)
                        .border(BorderStroke(1.dp, Color.Yellow))
                )
                Text(
                    "Q-LOCK",
                    color = Color.Yellow,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                )
            }
        }

        // Picture-in-picture floating dual view for DIRECTOR mode
        if (viewModel.currentMode.id == "director") {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 110.dp, start = 16.dp)
                    .size(90.dp, 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161B22))
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Selfie simulator camera bubble inside rear view
                Icon(
                    imageVector = Icons.Default.Cached,
                    contentDescription = "Selfie view",
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "DIRECTOR REC",
                    color = NeonRed,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }
        }

        // Center AR Emoji Filters Projection
        if (viewModel.currentMode.id == "aremoji" && uiState.activeAREmoji != "None") {
            AREmojiMaskProjection(maskName = uiState.activeAREmoji)
        }

        // Middle Viewport Technical Telemetry (ISO, Shutter, Zoom slider)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 230.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Active Zoom Factor HUD & Selector Pills
            ZoomSelectorPills(
                currentZoom = uiState.zoomValue,
                onZoomSelected = { viewModel.setZoomValue(it) }
            )

            // Tactile Analog Zoom Slider
            ZoomAnalogWheel(
                zoomValue = uiState.zoomValue,
                onZoomChanged = { viewModel.setZoomValue(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            // PRO CAMERA manual settings dash (exposing parameters for Pro Movie Mode)
            if (viewModel.currentMode.id == "promovie" || viewModel.currentMode.id == "photo") {
                ProControlsDashboard(
                    state = uiState,
                    onISOChanged = { viewModel.updateProISO(it) },
                    onShutterChanged = { viewModel.updateProShutter(it) },
                    onApertureChanged = { viewModel.updateProAperture(it) },
                    onFocusChanged = { viewModel.updateProFocus(it) }
                )
            }
        }

        // Bottom Dashboard Panel (shutter button, mode carousel, gallery)
        BottomGlassDashboard(
            state = uiState,
            viewModel = viewModel,
            onNavigateToModStore = onNavigateToModStore,
            onNavigateToGallery = onNavigateToGallery,
            onShutterClicked = {
                if (viewModel.currentMode.isVideo) {
                    viewModel.toggleRecording(context)
                } else {
                    flashTriggered = true
                    viewModel.capturePhoto(context)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        // Photo Save Flash screen ripple effect
        AnimatedVisibility(
            visible = flashTriggered,
            enter = fadeIn(animationSpec = tween(100)),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
            LaunchedEffect(Unit) {
                delay(120)
                flashTriggered = false
            }
        }

        // "Saved in Qyra Vault" pop-up toast
        uiState.capturedNotificationImageUri?.let { uri ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE6111622))
                    .border(BorderStroke(1.dp, NeonCyan), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Saved",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "8K SNAPSHOT SAVED IN QYRA VAULT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CameraXViewfinder(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = CameraPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val selector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                } catch (e: Exception) {
                    android.util.Log.e("CameraScreen", "CameraX binding failed: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = modifier
    )
}

@Composable
fun PermissionScenicFallback(
    onGrantPermission: () -> Unit,
    selectedMode: ShootingMode,
    state: CameraUiState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
    ) {
        // High quality simulated viewfinder background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF13223C), Color(0xFF03070E)),
                        radius = 1200f
                    )
                )
        )

        // Parallax glowing lines representing space dust or matrix
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0x0FFFFFFF))
                    .border(BorderStroke(1.dp, Color(0x33FFFFFF)), shape = CircleShape)
                    .clickable(onClick = onGrantPermission),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Request permission",
                    tint = NeonCyan,
                    modifier = Modifier
                        .size(44.dp)
                        .alpha(alphaAnim)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "REAL CAMERA CAPTURE READY",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap to grant camera access, or play with the simulated lens filters below.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }

        // Watermark badge watermark line in fallback preview
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(top = 180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "QYRA SENSOR ARRAY EMULATOR // ACTIVE",
                color = Color.White.copy(alpha = 0.25f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun GridViewfinderOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().border(BorderStroke(0.5.dp, Color(0x2BFFFFFF))))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().border(BorderStroke(0.5.dp, Color(0x2BFFFFFF))))
            Box(modifier = Modifier.weight(1f).fillMaxHeight().border(BorderStroke(0.5.dp, Color(0x2BFFFFFF))))
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().border(BorderStroke(0.5.dp, Color(0x2BFFFFFF))))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().border(BorderStroke(0.5.dp, Color(0x2BFFFFFF))))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().border(BorderStroke(0.5.dp, Color(0x2BFFFFFF))))
        }
    }
}

@Composable
fun ModeShaderFilterOverlay(mode: ShootingMode, state: CameraUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                when (mode.filterName) {
                    "vhs" -> Color(0x220000FF) // Blue retro CRT color tint
                    "cyberpunk" -> Color(0x1F290029) // High-intensity pink neon edge light
                    "thermal" -> Color(0x11FFCC00) // Heat signature ambient
                    "matrix" -> Color(0x1C001C00) // Digital green filter tint
                    "astro" -> Color(0x1A08001F) // Stars galaxy sky depth tint
                    else -> Color.Transparent
                }
            )
    ) {
        if (mode.filterName == "vhs") {
            // Dynamic timestamp overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 110.dp, start = 24.dp)
            ) {
                Text("PLAY \u25b6", color = Color.White, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                Text("1998-07-04", color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp))
                Text("VCR STEREO", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun TopStatusRow(
    state: CameraUiState,
    onToggleFlash: () -> Unit,
    onFlipCamera: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flashIcon = when (state.isFlashOn) {
        "ON" -> Icons.Default.FlashOn
        "AUTO" -> Icons.Default.FlashAuto
        else -> Icons.Default.FlashOff
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left settings icon
        GlassyIconButton(
            icon = Icons.Default.Settings,
            contentDescription = "Settings",
            onClick = onNavigateToSettings,
            size = 40.dp,
            modifier = Modifier.testTag("settings_button")
        )

        // Middle Telemetry Row (8K 60, HDR+ & details)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 8K 60 Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF))
                    .border(BorderStroke(0.5.dp, Color(0x33FFFFFF)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "8K 60",
                    color = NeonCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // HDR+ Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF))
                    .border(BorderStroke(0.5.dp, Color(0x33FFFFFF)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "HDR+",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Details Capsule
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x99000000))
                    .border(BorderStroke(0.5.dp, Color(0x33FFFFFF)), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (state.isRecording) NeonRed else Color.Green)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.settings.resolution} // ${state.settings.fps}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Right toggles
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Flash toggle
            GlassyIconButton(
                icon = flashIcon,
                contentDescription = "Flash Toggle",
                onClick = onToggleFlash,
                activated = state.isFlashOn != "OFF",
                glowColor = NeonOrange,
                size = 40.dp,
                modifier = Modifier.testTag("flash_toggle_button")
            )

            // Front/Back camera flip
            GlassyIconButton(
                icon = Icons.Default.Cached,
                contentDescription = "Flip Camera",
                onClick = onFlipCamera,
                size = 40.dp,
                modifier = Modifier.testTag("camera_flip_button")
            )
        }
    }
}

@Composable
fun ZoomSelectorPills(
    currentZoom: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x80000000))
            .border(BorderStroke(0.5.dp, Color(0x1AFFFFFF)), RoundedCornerShape(18.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val zoomPresets = listOf(0.5f, 1.0f, 5.0f, 30.0f, 150.0f, 300.0f)
        zoomPresets.forEach { preset ->
            val isSelected = currentZoom == preset
            val pillBg = if (isSelected) NeonCyan else Color.Transparent
            val pillTextColor = if (isSelected) Color.Black else Color.White

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(pillBg)
                    .clickable { onZoomSelected(preset) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (preset == 0.5f) "0.5x" else if (preset == 1f) "1x" else "${preset.toInt()}x",
                    color = pillTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ZoomAnalogWheel(
    zoomValue: Float,
    onZoomChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Tactile sliding wheel selector going from 1.0 to 300.0
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Slider(
            value = zoomValue,
            onValueChange = onZoomChanged,
            valueRange = 1.0f..300.0f,
            colors = androidx.compose.material3.SliderDefaults.colors(
                activeTrackColor = NeonCyan,
                inactiveTrackColor = Color(0x33FFFFFF),
                thumbColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("zoom_slider")
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1x WIDE", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("5x TELE", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("100x ULTRA", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("300x MAGIC", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProControlsDashboard(
    state: CameraUiState,
    onISOChanged: (Int) -> Unit,
    onShutterChanged: (String) -> Unit,
    onApertureChanged: (String) -> Unit,
    onFocusChanged: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x99000000))
            .border(BorderStroke(1.dp, Color(0x26FFFFFF)), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Meter status line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PRO CONTROL DASH",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "ISO ${state.proISO} // EV ${String.format(Locale.US, "%+.1f", state.exposureValue)} // ${state.proShutter} // ${state.proAperture}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick select rows for settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ISO preset slider
                Column {
                    Text("ISO SPEED", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(100, 400, 1600, 3200).forEach { iso ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (state.proISO == iso) NeonCyan.copy(alpha = 0.3f) else Color(0x11FFFFFF))
                                    .border(BorderStroke(0.5.dp, if (state.proISO == iso) NeonCyan else Color(0x11FFFFFF)))
                                    .clickable { onISOChanged(iso) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(iso.toString(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Shutter speed presets
                Column {
                    Text("SHUTTER SPEED", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("1/30s", "1/125s", "1/500s", "1/1000s").forEach { shut ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (state.proShutter == shut) NeonCyan.copy(alpha = 0.3f) else Color(0x11FFFFFF))
                                    .border(BorderStroke(0.5.dp, if (state.proShutter == shut) NeonCyan else Color(0x11FFFFFF)))
                                    .clickable { onShutterChanged(shut) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(shut, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Manual Focus ring slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MANUAL FOCUS depth", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                Spacer(modifier = Modifier.width(12.dp))
                androidx.compose.material3.Slider(
                    value = state.proFocus,
                    onValueChange = onFocusChanged,
                    valueRange = 0.0f..1.0f,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        activeTrackColor = NeonCyan,
                        thumbColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (state.proFocus >= 0.9f) "INFINITY (INF)" else String.format(Locale.US, "%.1fm", state.proFocus * 5),
                    color = NeonCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AREmojiMaskProjection(maskName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Simulated tracking circles floating on face bounds
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .border(
                        BorderStroke(
                            2.dp,
                            when (maskName) {
                                "Neon Crown" -> NeonOrange
                                "Glowing Visor" -> NeonCyan
                                "Alien Eyes" -> Color.Green
                                else -> NeonRed
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner glowing geometric mask layers
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(
                            BorderStroke(
                                1.dp,
                                when (maskName) {
                                    "Neon Crown" -> NeonOrange.copy(alpha = 0.4f)
                                    "Glowing Visor" -> NeonCyan.copy(alpha = 0.4f)
                                    "Alien Eyes" -> Color.Green.copy(alpha = 0.4f)
                                    else -> NeonRed.copy(alpha = 0.4f)
                                }
                            ),
                            shape = CircleShape
                        )
                )

                Text(
                    text = when (maskName) {
                        "Neon Crown" -> "\ud83d\udc51"
                        "Glowing Visor" -> "\ud83d\udd76"
                        "Alien Eyes" -> "\ud83d\udc7d"
                        else -> "\ud83d\udc31"
                    },
                    fontSize = 44.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "AR MASK: $maskName [ACTIVE]",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BottomGlassDashboard(
    state: CameraUiState,
    viewModel: CameraViewModel,
    onNavigateToModStore: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onShutterClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0x40FFFFFF), Color(0x10FFFFFF))
                )
            )
            .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(32.dp))
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Select Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .testTag("mode_carousel"),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ensure padding inside scroll area
                Spacer(modifier = Modifier.width(16.dp))

                state.shootingModes.forEachIndexed { index, mode ->
                    val isSelected = index == state.currentModeIndex
                    val color = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.6f)
                    val weight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    val letterSpacing = if (isSelected) 1.5.sp else 1.sp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.selectModeIndex(index) }
                            .testTag("mode_${mode.id}")
                    ) {
                        Text(
                            text = mode.displayName,
                            color = color,
                            fontSize = 12.sp,
                            fontWeight = weight,
                            letterSpacing = letterSpacing
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }

            // AR Face selector row if in AR EMOJI mode
            if (viewModel.currentMode.id == "aremoji") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val emojisList = listOf("None", "Neon Crown", "Glowing Visor", "Alien Eyes", "Cyber Cat")
                    emojisList.forEach { emoji ->
                        val active = state.activeAREmoji == emoji
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) NeonCyan.copy(alpha = 0.3f) else Color(0x1A000000))
                                .border(BorderStroke(0.5.dp, if (active) NeonCyan else Color(0x1AFFFFFF)))
                                .clickable { viewModel.selectAREmoji(emoji) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(emoji, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Primary control layout: Shutter row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Gallery Button (frosted capsule containing recent photo thumbnail)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x33000000))
                        .border(BorderStroke(1.dp, Color(0x4DFFFFFF)), shape = CircleShape)
                        .clickable(onClick = onNavigateToGallery)
                        .testTag("gallery_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val latestItem = state.galleryItems.firstOrNull()
                    if (latestItem != null) {
                        AsyncImage(
                            model = latestItem.uriString,
                            contentDescription = "Latest Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No images",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Middle Shutter Button
                ShutterButton(
                    isRecording = state.isRecording,
                    isVideo = viewModel.currentMode.isVideo,
                    recordingSeconds = state.recordingSeconds,
                    onClick = onShutterClicked
                )

                // Right Mod Store Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x33000000))
                        .border(BorderStroke(1.dp, Color(0x4DFFFFFF)), shape = CircleShape)
                        .clickable(onClick = onNavigateToModStore)
                        .testTag("modstore_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Mod Store",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Indicator Bar from Sophisticated Dark theme
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(128.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.33f)
                        .background(NeonCyan) // Amber accent color
                )
            }
        }
    }
}

@Composable
fun ShutterButton(
    isRecording: Boolean,
    isVideo: Boolean,
    recordingSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .border(BorderStroke(4.dp, Color.White), shape = CircleShape)
            .padding(6.dp)
            .clickable(onClick = onClick)
            .testTag("shutter_button"),
        contentAlignment = Alignment.Center
    ) {
        if (isVideo) {
            if (isRecording) {
                // Recording visual red square
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val min = recordingSeconds / 60
                    val sec = recordingSeconds % 60
                    Text(
                        text = String.format(Locale.US, "%d:%02d", min, sec),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Standard red recording dot
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NeonRed)
                )
            }
        } else {
            // White shutter dot for photo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
