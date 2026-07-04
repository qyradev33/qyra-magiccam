package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.media.MediaActionSound
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ShootingMode(
    val id: String,
    val displayName: String,
    val isVideo: Boolean = false,
    val isCustomMod: Boolean = false,
    val filterName: String? = null,
    val description: String = ""
)

data class MediaItem(
    val id: Long,
    val uriString: String,
    val timestamp: Long,
    val mode: String,
    val zoom: String,
    val resolution: String,
    val watermark: String,
    val isVideo: Boolean = false,
    val durationSeconds: Int = 0
)

data class CameraMod(
    val id: String,
    val displayName: String,
    val description: String,
    val icon: String,
    val filterName: String,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false
)

data class CameraSettings(
    val resolution: String = "8K UHD", // 8K UHD, 4K UHD, 1080P, 720P
    val fps: String = "60 FPS", // 60 FPS, 30 FPS, 24 FPS
    val customWatermarkText: String = "Taken by QYRA",
    val isWatermarkEnabled: Boolean = true,
    val isGridEnabled: Boolean = false,
    val stabilizerLevel: String = "Super Steady", // Off, Standard, Super Steady, Gimbal Mode
    val isSceneEnhancerEnabled: Boolean = true,
    val isAudioHighFiEnabled: Boolean = true,
    val storageLocation: String = "Internal (Qyra Vault)"
)

data class CameraUiState(
    val shootingModes: List<ShootingMode> = emptyList(),
    val currentModeIndex: Int = 0,
    val zoomValue: Float = 1.0f,
    val exposureValue: Float = 0.0f,
    val isRecording: Boolean = false,
    val recordingSeconds: Int = 0,
    val isFlashOn: String = "OFF", // OFF, ON, AUTO, TORCH
    val activeLens: String = "1.0x Wide", // 0.5x UltraWide, 1.0x Wide, 5.0x Tele, 300x QMagic
    val activeAREmoji: String = "None", // None, Neon Crown, Glowing Visor, Alien Eyes, Cyber Cat
    val galleryItems: List<MediaItem> = emptyList(),
    val settings: CameraSettings = CameraSettings(),
    val mods: List<CameraMod> = emptyList(),
    val isFrontCamera: Boolean = false,
    val capturedNotificationImageUri: String? = null,
    val proISO: Int = 100,
    val proShutter: String = "1/125s",
    val proAperture: String = "F/1.8",
    val proFocus: Float = 0.8f // Manual focus slider
)

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val soundEffect = MediaActionSound()
    private var recordingTimerJob: kotlinx.coroutines.Job? = null

    // Base shooting modes
    private val defaultModes = listOf(
        ShootingMode("photo", "PHOTO", isVideo = false),
        ShootingMode("video", "VIDEO", isVideo = true),
        ShootingMode("portrait", "PORTRAIT", isVideo = false),
        ShootingMode("panorama", "PANORAMA", isVideo = false),
        ShootingMode("timelapse", "TIME-LAPSE", isVideo = true),
        ShootingMode("slowmotion", "SLO-MO", isVideo = true),
        ShootingMode("aremoji", "AR EMOJI", isVideo = false),
        ShootingMode("qmagiczoom", "Q-ZOOM", isVideo = false),
        ShootingMode("promovie", "PRO MOVIE", isVideo = true),
        ShootingMode("director", "DIRECTOR", isVideo = false),
        ShootingMode("landscape", "LANDSCAPE", isVideo = false),
        ShootingMode("macro", "MACRO", isVideo = false),
        ShootingMode("hdr", "HDR", isVideo = false)
    )

    // Mod store items
    private val modStoreItems = listOf(
        CameraMod("retro_vhs", "Retro VHS 1998", "Adds classic tape distortions, retro scanning overlays, and raw analog noise textures.", "videocam", "vhs"),
        CameraMod("cyberpunk", "Cyberpunk Neon", "Overlays high-intensity magenta and glowing cyan edge matrix highlights.", "electric_bolt", "cyberpunk"),
        CameraMod("thermal", "Thermal Heat Vision", "Translates luminance details into electric thermal infrared heatmaps.", "thermostat", "thermal"),
        CameraMod("matrix", "Matrix Digital Rain", "Drapes a beautiful waterfall of cascading neon-green binary glyphs across the scene.", "code", "matrix"),
        CameraMod("astromagic", "AstroMagic Night", "Simulates hyper-exposure stellar stacks to unveil faint galactic nebulas.", "star", "astro")
    )

    init {
        _uiState.update {
            it.copy(
                shootingModes = defaultModes,
                mods = modStoreItems
            )
        }
        // Play click sounds to load the sound player
        try {
            soundEffect.load(MediaActionSound.SHUTTER_CLICK)
            soundEffect.load(MediaActionSound.START_VIDEO_RECORDING)
            soundEffect.load(MediaActionSound.STOP_VIDEO_RECORDING)
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Failed to load camera sound effects: ${e.message}")
        }
    }

    val currentMode: ShootingMode
        get() {
            val state = _uiState.value
            return state.shootingModes.getOrNull(state.currentModeIndex) ?: defaultModes[0]
        }

    fun selectModeIndex(index: Int) {
        if (index in 0 until _uiState.value.shootingModes.size) {
            _uiState.update {
                val nextMode = it.shootingModes[index]
                var nextZoom = it.zoomValue
                var nextLens = it.activeLens
                if (nextMode.id == "qmagiczoom") {
                    nextZoom = 150.0f
                    nextLens = "300x QMagic"
                } else if (it.zoomValue > 10.0f && nextMode.id != "qmagiczoom") {
                    nextZoom = 1.0f
                    nextLens = "1.0x Wide"
                }
                it.copy(
                    currentModeIndex = index,
                    zoomValue = nextZoom,
                    activeLens = nextLens
                )
            }
        }
    }

    fun setZoomValue(value: Float) {
        val coerced = value.coerceIn(1.0f, 300.0f)
        _uiState.update {
            val lens = when {
                coerced >= 150.0f -> "300x QMagic"
                coerced >= 10.0f -> "5.0x Tele"
                coerced <= 0.7f -> "0.5x UltraWide"
                else -> "1.0x Wide"
            }
            it.copy(zoomValue = coerced, activeLens = lens)
        }
    }

    fun selectLens(lensName: String) {
        _uiState.update {
            val targetZoom = when (lensName) {
                "0.5x UltraWide" -> 0.5f
                "1.0x Wide" -> 1.0f
                "5.0x Tele" -> 5.0f
                "300x QMagic" -> 150.0f
                else -> 1.0f
            }
            it.copy(activeLens = lensName, zoomValue = targetZoom)
        }
    }

    fun setExposureValue(value: Float) {
        _uiState.update { it.copy(exposureValue = value.coerceIn(-2.0f, 2.0f)) }
    }

    fun setFlashState(state: String) {
        _uiState.update { it.copy(isFlashOn = state) }
    }

    fun selectAREmoji(emoji: String) {
        _uiState.update { it.copy(activeAREmoji = emoji) }
    }

    fun toggleCameraFacing() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun updateProISO(iso: Int) {
        _uiState.update { it.copy(proISO = iso) }
    }

    fun updateProShutter(shutter: String) {
        _uiState.update { it.copy(proShutter = shutter) }
    }

    fun updateProAperture(aperture: String) {
        _uiState.update { it.copy(proAperture = aperture) }
    }

    fun updateProFocus(focus: Float) {
        _uiState.update { it.copy(proFocus = focus) }
    }

    fun updateSettings(newSettings: CameraSettings) {
        _uiState.update { it.copy(settings = newSettings) }
    }

    // Capture execution
    fun capturePhoto(context: Context, previewBitmap: Bitmap? = null) {
        val state = _uiState.value
        val mode = currentMode
        if (state.isRecording) return

        viewModelScope.launch {
            try {
                soundEffect.play(MediaActionSound.SHUTTER_CLICK)
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Could not play shutter sound: ${e.message}")
            }

            // Create watermarked photo Bitmap
            val width = 1920
            val height = 1080
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            if (previewBitmap != null) {
                // Scale real preview bitmap to cover canvas
                val srcWidth = previewBitmap.width
                val srcHeight = previewBitmap.height
                val scale = Math.max(width.toFloat() / srcWidth, height.toFloat() / srcHeight)
                val newWidth = srcWidth * scale
                val newHeight = srcHeight * scale
                val left = (width - newWidth) / 2
                val top = (height - newHeight) / 2
                val dstRect = android.graphics.RectF(left, top, left + newWidth, top + newHeight)
                canvas.drawBitmap(previewBitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
            } else {
                // Generate a high-quality simulated photography scene if camera feed isn't ready
                drawSimulatedScene(canvas, width, height, mode, state)
            }

            // Draw "Taken by QYRA" Watermark at the bottom
            if (state.settings.isWatermarkEnabled) {
                drawWatermark(canvas, width, height, state)
            }

            // Save to internal storage file
            val file = File(context.filesDir, "IMG_${System.currentTimeMillis()}.jpg")
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error saving photo file: ${e.message}")
            } finally {
                out?.close()
            }

            // Create media record
            val newItem = MediaItem(
                id = System.currentTimeMillis(),
                uriString = Uri.fromFile(file).toString(),
                timestamp = System.currentTimeMillis(),
                mode = mode.displayName,
                zoom = String.format(Locale.US, "%.1fx", state.zoomValue),
                resolution = state.settings.resolution,
                watermark = if (state.settings.isWatermarkEnabled) state.settings.customWatermarkText else "",
                isVideo = false
            )

            _uiState.update {
                it.copy(
                    galleryItems = listOf(newItem) + it.galleryItems,
                    capturedNotificationImageUri = newItem.uriString
                )
            }

            delay(2000)
            _uiState.update {
                if (it.capturedNotificationImageUri == newItem.uriString) {
                    it.copy(capturedNotificationImageUri = null)
                } else {
                    it
                }
            }
        }
    }

    // Video Recording simulator
    fun toggleRecording(context: Context) {
        val state = _uiState.value
        val mode = currentMode
        if (!mode.isVideo) return

        if (state.isRecording) {
            // Stop recording
            try {
                soundEffect.play(MediaActionSound.STOP_VIDEO_RECORDING)
            } catch (e: Exception) {}
            recordingTimerJob?.cancel()

            val recordedDuration = state.recordingSeconds
            _uiState.update { it.copy(isRecording = false, recordingSeconds = 0) }

            // Create simulated video file (creates a placeholder watermarked image with video icon overlay)
            viewModelScope.launch {
                val width = 1920
                val height = 1080
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                // Render simulated frame
                drawSimulatedScene(canvas, width, height, mode, state)

                // Draw video status indicator
                val paint = Paint().apply {
                    color = AndroidColor.RED
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(80f, 80f, 15f, paint)
                paint.color = AndroidColor.WHITE
                paint.textSize = 36f
                canvas.drawText("REC OVERLAY [PRO-MOVI_8K_60FPS]", 115f, 92f, paint)

                if (state.settings.isWatermarkEnabled) {
                    drawWatermark(canvas, width, height, state)
                }

                val file = File(context.filesDir, "VID_${System.currentTimeMillis()}.jpg")
                var out: FileOutputStream? = null
                try {
                    out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Error saving simulated video thumb: ${e.message}")
                } finally {
                    out?.close()
                }

                val newItem = MediaItem(
                    id = System.currentTimeMillis(),
                    uriString = Uri.fromFile(file).toString(),
                    timestamp = System.currentTimeMillis(),
                    mode = mode.displayName,
                    zoom = String.format(Locale.US, "%.1fx", state.zoomValue),
                    resolution = state.settings.resolution,
                    watermark = if (state.settings.isWatermarkEnabled) state.settings.customWatermarkText else "",
                    isVideo = true,
                    durationSeconds = if (recordedDuration > 0) recordedDuration else 12
                )

                _uiState.update {
                    it.copy(
                        galleryItems = listOf(newItem) + it.galleryItems,
                        capturedNotificationImageUri = newItem.uriString
                    )
                }

                delay(2000)
                _uiState.update {
                    if (it.capturedNotificationImageUri == newItem.uriString) {
                        it.copy(capturedNotificationImageUri = null)
                    } else {
                        it
                    }
                }
            }
        } else {
            // Start recording
            try {
                soundEffect.play(MediaActionSound.START_VIDEO_RECORDING)
            } catch (e: Exception) {}
            _uiState.update { it.copy(isRecording = true, recordingSeconds = 0) }

            recordingTimerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _uiState.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
                }
            }
        }
    }

    fun deleteMediaItem(context: Context, item: MediaItem) {
        viewModelScope.launch {
            try {
                val uri = Uri.parse(item.uriString)
                val path = uri.path
                if (path != null) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Failed to delete file: ${e.message}")
            }
            _uiState.update { state ->
                state.copy(
                    galleryItems = state.galleryItems.filter { it.id != item.id },
                    capturedNotificationImageUri = if (state.capturedNotificationImageUri == item.uriString) null else state.capturedNotificationImageUri
                )
            }
        }
    }

    // Mod store downloader
    fun downloadMod(modId: String) {
        val modIndex = _uiState.value.mods.indexOfFirst { it.id == modId }
        if (modIndex == -1) return
        val mod = _uiState.value.mods[modIndex]
        if (mod.isDownloaded || mod.isDownloading) return

        viewModelScope.launch {
            // Update downloading state
            _uiState.update { state ->
                state.copy(
                    mods = state.mods.map {
                        if (it.id == modId) it.copy(isDownloading = true, downloadProgress = 0.05f) else it
                    }
                )
            }

            // Simulate progress over time
            for (p in 1..10) {
                delay(250)
                _uiState.update { state ->
                    state.copy(
                        mods = state.mods.map {
                            if (it.id == modId) it.copy(downloadProgress = p * 0.10f) else it
                        }
                    )
                }
            }

            // Complete download
            _uiState.update { state ->
                val updatedMods = state.mods.map {
                    if (it.id == modId) it.copy(isDownloaded = true, isDownloading = false, downloadProgress = 1.0f) else it
                }
                val downloadedMod = updatedMods.first { it.id == modId }
                val newShootingMode = ShootingMode(
                    id = downloadedMod.id,
                    displayName = downloadedMod.displayName.uppercase(),
                    isVideo = downloadedMod.id == "retro_vhs",
                    isCustomMod = true,
                    filterName = downloadedMod.filterName,
                    description = downloadedMod.description
                )
                state.copy(
                    mods = updatedMods,
                    shootingModes = state.shootingModes + newShootingMode
                )
            }
        }
    }

    // Drawing helpers for simulation
    private fun drawSimulatedScene(canvas: Canvas, width: Int, height: Int, mode: ShootingMode, state: CameraUiState) {
        val paint = Paint()

        // Background base gradient
        val bgGradient = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            when (mode.id) {
                "hdr" -> intArrayOf(0xFF101C38.toInt(), 0xFF5D2075.toInt(), 0xFFFF7B00.toInt())
                "portrait" -> intArrayOf(0xFF1C0A00.toInt(), 0xFF4E200C.toInt(), 0xFF140700.toInt())
                "landscape" -> intArrayOf(0xFF0F2042.toInt(), 0xFF3574A8.toInt(), 0xFF99E2FF.toInt())
                "cyberpunk" -> intArrayOf(0xFF140417.toInt(), 0xFF3D063A.toInt(), 0xFF0D031A.toInt())
                "thermal" -> intArrayOf(0xFF010022.toInt(), 0xFF330055.toInt(), 0xFFFF5500.toInt())
                "matrix" -> intArrayOf(0xFF000801.toInt(), 0xFF001F04.toInt(), 0xFF000501.toInt())
                "astromagic" -> intArrayOf(0xFF02020A.toInt(), 0xFF0D0F2E.toInt(), 0xFF2A153A.toInt())
                "qmagiczoom" -> intArrayOf(0xFF051B12.toInt(), 0xFF1A4522.toInt(), 0xFF081810.toInt())
                else -> intArrayOf(0xFF1C1E24.toInt(), 0xFF2F323A.toInt(), 0xFF141518.toInt())
            },
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = bgGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // Drawing custom visual patterns depending on mode
        paint.isAntiAlias = true
        when (mode.id) {
            "portrait" -> {
                // Focus ring in center
                paint.color = AndroidColor.argb(50, 255, 255, 255)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 4f
                canvas.drawCircle(width / 2f, height / 2f, 250f, paint)

                // Blur Simulation overlay texturing
                paint.style = Paint.Style.FILL
                paint.color = AndroidColor.WHITE
                paint.textSize = 48f
                canvas.drawText("LIQUID GLASS PORTRAIT DEPTH: F/1.2", 100f, 150f, paint)

                // Stylized golden particle dots
                paint.color = AndroidColor.argb(160, 255, 196, 0)
                canvas.drawCircle(width * 0.3f, height * 0.4f, 20f, paint)
                canvas.drawCircle(width * 0.7f, height * 0.6f, 35f, paint)
                paint.color = AndroidColor.argb(80, 255, 196, 0)
                canvas.drawCircle(width * 0.25f, height * 0.7f, 40f, paint)
                canvas.drawCircle(width * 0.8f, height * 0.3f, 15f, paint)
            }
            "hdr" -> {
                // Super vivid solar sphere and rays
                paint.style = Paint.Style.FILL
                paint.color = AndroidColor.argb(120, 255, 110, 0)
                canvas.drawCircle(width * 0.5f, height * 0.35f, 220f, paint)
                paint.color = AndroidColor.argb(230, 255, 235, 120)
                canvas.drawCircle(width * 0.5f, height * 0.35f, 140f, paint)

                paint.color = AndroidColor.WHITE
                paint.textSize = 45f
                canvas.drawText("HDR+ MULTI-EXPOSURE SMART FUSION ACTIVE", 100f, 150f, paint)
            }
            "landscape" -> {
                // Majestic mountain silhouettes
                paint.style = Paint.Style.FILL
                paint.color = AndroidColor.parseColor("#1C3A5E")
                val path1 = android.graphics.Path()
                path1.moveTo(0f, height.toFloat())
                path1.lineTo(width * 0.3f, height * 0.5f)
                path1.lineTo(width * 0.6f, height * 0.85f)
                path1.lineTo(width * 0.8f, height * 0.6f)
                path1.lineTo(width.toFloat(), height.toFloat())
                path1.close()
                canvas.drawPath(path1, paint)

                // Overlay AI info
                paint.color = AndroidColor.GREEN
                paint.textSize = 40f
                canvas.drawText("SCENE: HIGH LANDSCAPE HORIZON // OPTIMIZED FOR SHARPNESS", 100f, 150f, paint)
            }
            "qmagiczoom" -> {
                // Zoom grids and focal lines
                paint.color = AndroidColor.argb(100, 0, 229, 255)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRect(200f, 150f, width - 200f, height - 150f, paint)
                canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint)
                canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, paint)

                // Zoom factor
                paint.style = Paint.Style.FILL
                paint.color = AndroidColor.parseColor("#00E5FF")
                paint.textSize = 72f
                paint.isFakeBoldText = true
                canvas.drawText("Q-ZOOM: ${String.format(Locale.US, "%.1fx", state.zoomValue)}", 100f, 220f, paint)

                paint.textSize = 36f
                paint.isFakeBoldText = false
                canvas.drawText("STABILIZATION GIMBAL LOCK: 100% STEADY", 100f, 280f, paint)
            }
            "cyberpunk" -> {
                // Glowing vector wireframe grid
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                paint.color = AndroidColor.parseColor("#FF007F")
                for (i in 0..10) {
                    val x = i * (width / 10f)
                    canvas.drawLine(x, height * 0.6f, x + (x - width/2)*1.5f, height.toFloat(), paint)
                }
                for (i in 0..6) {
                    val y = height * 0.6f + (i * i * 8f)
                    canvas.drawLine(0f, y, width.toFloat(), y, paint)
                }

                paint.style = Paint.Style.FILL
                paint.color = AndroidColor.parseColor("#00E5FF")
                paint.textSize = 50f
                canvas.drawText("CYBERPUNK NEON GLITCH SHADER // INSTALLED MOD", 100f, 150f, paint)
            }
            "thermal" -> {
                // Draw warm infrared heat signatures
                paint.style = Paint.Style.FILL
                paint.color = AndroidColor.YELLOW
                canvas.drawCircle(width * 0.4f, height * 0.5f, 180f, paint)
                paint.color = AndroidColor.RED
                canvas.drawCircle(width * 0.4f, height * 0.5f, 100f, paint)
                paint.color = AndroidColor.WHITE
                canvas.drawCircle(width * 0.4f, height * 0.5f, 40f, paint)

                paint.color = AndroidColor.argb(180, 0, 0, 255)
                canvas.drawRect(0f, height * 0.7f, width.toFloat(), height.toFloat(), paint)

                paint.color = AndroidColor.parseColor("#FF9100")
                paint.textSize = 50f
                canvas.drawText("INFRARED THERMAL SPECTRUM ANALYSIS", 100f, 150f, paint)
            }
            "matrix" -> {
                // Falling digital matrix text simulation
                paint.color = AndroidColor.GREEN
                paint.textSize = 40f
                for (col in 0..20) {
                    val x = col * 90f + 50f
                    var y = (System.currentTimeMillis() % 1000) / 1000f * height
                    y = (y + col * 40f) % height
                    canvas.drawText("1", x, y, paint)
                    canvas.drawText("0", x, (y - 50) % height, paint)
                    canvas.drawText("1", x, (y - 100) % height, paint)
                    canvas.drawText("Q", x, (y - 150) % height, paint)
                }
                paint.textSize = 50f
                canvas.drawText("MATRIX DATA STACK ACTIVE", 100f, 150f, paint)
            }
            "aremoji" -> {
                // Interactive Face mask projection
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 6f
                paint.color = AndroidColor.parseColor("#00E5FF")
                // Draw mask shapes on center screen
                val maskPath = android.graphics.Path()
                maskPath.moveTo(width/2f - 120f, height/2f - 50f)
                maskPath.lineTo(width/2f - 50f, height/2f - 10f)
                maskPath.lineTo(width/2f - 120f, height/2f + 30f)
                maskPath.lineTo(width/2f - 200f, height/2f - 10f)
                maskPath.close()

                maskPath.moveTo(width/2f + 120f, height/2f - 50f)
                maskPath.lineTo(width/2f + 200f, height/2f - 10f)
                maskPath.lineTo(width/2f + 120f, height/2f + 30f)
                maskPath.lineTo(width/2f + 50f, height/2f - 10f)
                maskPath.close()
                canvas.drawPath(maskPath, paint)

                paint.color = AndroidColor.RED
                canvas.drawCircle(width/2f, height/2f + 150f, 35f, paint)

                paint.style = Paint.Style.FILL
                paint.color = AndroidColor.WHITE
                paint.textSize = 40f
                canvas.drawText("AR FACIAL ANCHOR MAPPED // ACTIVE MASK: " + state.activeAREmoji, 100f, 150f, paint)
            }
            "macro" -> {
                // Ring guidelines for super detailed macro shots
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                paint.color = AndroidColor.WHITE
                canvas.drawCircle(width / 2f, height / 2f, 120f, paint)
                canvas.drawCircle(width / 2f, height / 2f, 80f, paint)
                canvas.drawCircle(width / 2f, height / 2f, 40f, paint)

                paint.style = Paint.Style.FILL
                paint.textSize = 45f
                canvas.drawText("LIQUID MACRO: FOCUS DISTANCE 1.5CM", 100f, 150f, paint)
            }
            "astromagic" -> {
                // Galactic spiral arm dust
                paint.style = Paint.Style.FILL
                paint.color = AndroidColor.WHITE
                for (i in 1..200) {
                    val angle = i * 0.1f
                    val dist = i * 4f
                    val rx = width / 2f + Math.cos(angle.toDouble()).toFloat() * dist
                    val ry = height / 2f + Math.sin(angle.toDouble()).toFloat() * dist
                    canvas.drawCircle(rx, ry, (i % 4 + 2).toFloat(), paint)
                }

                paint.color = AndroidColor.CYAN
                canvas.drawCircle(width * 0.75f, height * 0.25f, 60f, paint)

                paint.color = AndroidColor.WHITE
                paint.textSize = 45f
                canvas.drawText("ASTRO EXPOSURE TIME: 30s [SIMULATED EXP STACK]", 100f, 150f, paint)
            }
            else -> {
                // Photo and Video mode landscapes
                paint.color = AndroidColor.WHITE
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                // Grid lines if enabled
                if (state.settings.isGridEnabled) {
                    canvas.drawLine(width / 3f, 0f, width / 3f, height.toFloat(), paint)
                    canvas.drawLine(2 * width / 3f, 0f, 2 * width / 3f, height.toFloat(), paint)
                    canvas.drawLine(0f, height / 3f, width.toFloat(), height / 3f, paint)
                    canvas.drawLine(0f, 2 * height / 3f, width.toFloat(), 2 * height / 3f, paint)
                }

                // Small center target reticle
                canvas.drawCircle(width / 2f, height / 2f, 10f, paint)

                paint.style = Paint.Style.FILL
                paint.textSize = 40f
                canvas.drawText("QYRA MAGIC ENGINE AUTO-SCENE // MULTI-AI SENSING", 100f, 150f, paint)
            }
        }
    }

    private fun drawWatermark(canvas: Canvas, width: Int, height: Int, state: CameraUiState) {
        val paint = Paint().apply {
            color = AndroidColor.WHITE
            textSize = 34f
            isAntiAlias = true
            isFakeBoldText = true
            letterSpacing = 0.2f
        }

        // Draw background glossy strip for watermark
        val watermarkBg = Paint().apply {
            color = AndroidColor.argb(120, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, height - 90f, width.toFloat(), height.toFloat(), watermarkBg)

        // Watermark Text at Bottom Left
        val text = state.settings.customWatermarkText
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US)
        val dateStr = sdf.format(Date())
        canvas.drawText(text, 80f, height - 35f, paint)

        // Camera Metadata at Bottom Right
        paint.color = AndroidColor.parseColor("#00E5FF")
        paint.textSize = 28f
        paint.isFakeBoldText = false
        val specString = "${state.settings.resolution} // ${state.settings.fps} // ZOOM ${String.format(Locale.US, "%.1fx", state.zoomValue)} // ISO ${state.proISO} // $dateStr"
        val textWidth = paint.measureText(specString)
        canvas.drawText(specString, width - textWidth - 80f, height - 37f, paint)
    }
}
