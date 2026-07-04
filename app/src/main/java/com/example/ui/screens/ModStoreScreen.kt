package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CameraMod
import com.example.CameraViewModel
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.GlassBgLight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassyButton
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModStoreScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val mods = uiState.mods

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MODS & SHADERS STORE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("modstore_back_button")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introductory glass header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x2BFFFFFF), Color(0x0EFFFFFF))
                        )
                    )
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "EXPAND YOUR VIEWPORT",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Download cutting-edge matrix overlays, infrared thermal models, and classic 1990s VHS textures. Installed mods instantly integrate into the horizontal viewfinder mode selector carousel below.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Text(
                "AVAILABLE CAMERA MODPACKS",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )

            // Mod items list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mods) { mod ->
                    ModStoreCard(
                        mod = mod,
                        onDownload = { viewModel.downloadMod(mod.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModStoreCard(
    mod: CameraMod,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vectorIcon = when (mod.icon) {
        "videocam" -> Icons.Default.Videocam
        "electric_bolt" -> Icons.Default.ElectricBolt
        "thermostat" -> Icons.Default.Thermostat
        "code" -> Icons.Default.Code
        "star" -> Icons.Default.Star
        else -> Icons.Default.Download
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x1F000000))
            .border(
                BorderStroke(
                    if (mod.isDownloading) 1.5.dp else 1.dp,
                    if (mod.isDownloading) NeonCyan else Color(0x26FFFFFF)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon backing
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (mod.isDownloaded) NeonCyan.copy(alpha = 0.2f) else Color(0x1AFFFFFF)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (mod.isDownloaded) NeonCyan else Color(0x33FFFFFF)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorIcon,
                        contentDescription = mod.displayName,
                        tint = if (mod.isDownloaded) NeonCyan else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mod.displayName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (mod.isDownloaded) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Installed",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "SHADER PACK // FREE",
                        color = if (mod.isDownloaded) NeonCyan else NeonOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Download/Install Button
                if (!mod.isDownloaded && !mod.isDownloading) {
                    GlassyButton(
                        onClick = onDownload,
                        modifier = Modifier.testTag("download_button_${mod.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Download",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else if (mod.isDownloaded) {
                    Text(
                        "INSTALLED",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Text(
                text = mod.description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Progress Bar if Downloading
            if (mod.isDownloading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Downloading shader libraries...",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                        Text(
                            "${(mod.downloadProgress * 100).toInt()}%",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { mod.downloadProgress },
                        color = NeonCyan,
                        trackColor = Color(0x1AFFFFFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}
