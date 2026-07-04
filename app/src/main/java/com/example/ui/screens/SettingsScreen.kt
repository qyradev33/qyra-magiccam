package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CameraSettings
import com.example.CameraViewModel
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MAGICCAM SETTINGS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Title: Image & Video Quality
            SectionHeader(title = "IMAGE & VIDEO ENGINE")

            // Resolution dropdown selector
            DropdownSettingRow(
                icon = Icons.Default.PhotoSizeSelectLarge,
                title = "Capture Resolution",
                subtitle = "Select max photo output resolution (8K uses QYRA SuperScale)",
                currentValue = settings.resolution,
                options = listOf("8K UHD", "4K UHD", "1080P", "720P"),
                onSelected = { res ->
                    viewModel.updateSettings(settings.copy(resolution = res))
                },
                tag = "resolution_setting"
            )

            HorizontalDivider(color = Color(0x1AFFFFFF))

            // Framerate dropdown selector
            DropdownSettingRow(
                icon = Icons.Default.Speed,
                title = "Video Framerate",
                subtitle = "Target FPS for cinema and high-speed modes",
                currentValue = settings.fps,
                options = listOf("60 FPS", "30 FPS", "24 FPS"),
                onSelected = { fps ->
                    viewModel.updateSettings(settings.copy(fps = fps))
                },
                tag = "fps_setting"
            )

            HorizontalDivider(color = Color(0x1AFFFFFF))

            // Stabilization dropdown selector
            DropdownSettingRow(
                icon = Icons.Default.Camera,
                title = "Q-Gimbal Stabilization",
                subtitle = "Adaptive electronic stabilizer algorithm",
                currentValue = settings.stabilizerLevel,
                options = listOf("Off", "Standard", "Super Steady", "Gimbal Mode"),
                onSelected = { lvl ->
                    viewModel.updateSettings(settings.copy(stabilizerLevel = lvl))
                },
                tag = "stabilizer_setting"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Section Title: Smart & Assistant Tools
            SectionHeader(title = "INTELLIGENT OVERLAYS")

            // Watermark toggle
            SwitchSettingRow(
                icon = Icons.Default.Water,
                title = "Taken by QYRA Watermark",
                subtitle = "Imprints brand badge & tech metadata on captures",
                checked = settings.isWatermarkEnabled,
                onCheckedChange = { checked ->
                    viewModel.updateSettings(settings.copy(isWatermarkEnabled = checked))
                },
                tag = "watermark_toggle"
            )

            if (settings.isWatermarkEnabled) {
                // Watermark custom string
                OutlinedTextField(
                    value = settings.customWatermarkText,
                    onValueChange = { txt ->
                        viewModel.updateSettings(settings.copy(customWatermarkText = txt))
                    },
                    label = { Text("Watermark Badge Text") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0x4DFFFFFF),
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("watermark_text_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            HorizontalDivider(color = Color(0x1AFFFFFF))

            // Rule of thirds grid lines
            SwitchSettingRow(
                icon = Icons.Default.Grid3x3,
                title = "Framing Grid Lines",
                subtitle = "Displays 3x3 division overlay for perfect layouts",
                checked = settings.isGridEnabled,
                onCheckedChange = { checked ->
                    viewModel.updateSettings(settings.copy(isGridEnabled = checked))
                },
                tag = "grid_toggle"
            )

            HorizontalDivider(color = Color(0x1AFFFFFF))

            // AI Scene Enhancer
            SwitchSettingRow(
                icon = Icons.Default.HighQuality,
                title = "AI Scene Enhancer",
                subtitle = "Auto-tunes dynamic range and noise structures via AI",
                checked = settings.isSceneEnhancerEnabled,
                onCheckedChange = { checked ->
                    viewModel.updateSettings(settings.copy(isSceneEnhancerEnabled = checked))
                },
                tag = "ai_enhancer_toggle"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Section Title: Hardware & Storage
            SectionHeader(title = "SYSTEM & INTEGRATION")

            // Audio High-Fi
            SwitchSettingRow(
                icon = Icons.Default.AudioFile,
                title = "Studio High-Fi Audio Capture",
                subtitle = "Records dual-channel 384kbps spatial audio tracks",
                checked = settings.isAudioHighFiEnabled,
                onCheckedChange = { checked ->
                    viewModel.updateSettings(settings.copy(isAudioHighFiEnabled = checked))
                },
                tag = "audio_toggle"
            )

            HorizontalDivider(color = Color(0x1AFFFFFF))

            // Storage Selector
            DropdownSettingRow(
                icon = Icons.Default.SdCard,
                title = "Vault Save Location",
                subtitle = "Target sandbox storage directory for saved media",
                currentValue = settings.storageLocation,
                options = listOf("Internal (Qyra Vault)", "SD Card Storage", "Camera Roll External"),
                onSelected = { loc ->
                    viewModel.updateSettings(settings.copy(storageLocation = loc))
                },
                tag = "storage_setting"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Legal Credit footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Qyra MagicCam Engine v4.0.0 // Powered by QYRA Lab",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = NeonCyan,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    )
}

@Composable
fun SwitchSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = NeonCyan,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
fun DropdownSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    currentValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    tag: String
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = NeonCyan,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        Box {
            Text(
                text = currentValue,
                color = NeonCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .background(Color(0x0FFFFFFF))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(DarkSlateCard)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.White) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
