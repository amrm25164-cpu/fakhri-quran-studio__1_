package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FontManager
import com.example.data.ProjectRepository
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import com.example.engine.AspectRatio
import com.example.engine.AudioPlayerManager
import com.example.engine.DefaultTypographyPresets
import com.example.engine.LayerProperties
import com.example.engine.ProjectConfig
import com.example.engine.QualityControlManager
import com.example.engine.Resolution
import com.example.engine.TextFitStatus
import com.example.engine.TypographyPreset
import com.example.engine.TypographyStyle
import com.example.engine.VideoRenderer
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.LayerPositionControls
import com.example.ui.components.ManualFontSizeStepper
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PreviewStudioScreen(
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
    fontManager: FontManager,
    projectRepository: ProjectRepository,
    videoRenderer: VideoRenderer,
    audioPlayerManager: AudioPlayerManager,
    qualityControlManager: QualityControlManager,
    initialProject: ProjectConfig? = null,
    onProceedToExport: (ProjectConfig) -> Unit = {}
) {
    var project by remember {
        mutableStateOf(initialProject ?: projectRepository.projects.value.firstOrNull() ?: ProjectConfig())
    }

    val fonts by fontManager.fonts.collectAsState()
    val audioState by audioPlayerManager.state.collectAsState()

    val ayahs = remember(project.surahNumber, project.startAyah, project.endAyah) {
        quranRepository.getVersesForSurah(project.surahNumber, project.startAyah, project.endAyah)
    }

    val totalDurationSec = (ayahs.size * project.verseDurationSeconds).coerceAtLeast(1)
    val totalDurationMs = totalDurationSec * 1000

    var isPlaying by remember { mutableStateOf(false) }
    var currentPlaybackMs by remember { mutableIntStateOf(0) }
    var previewSpeed by remember { mutableFloatStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }
    var previewQuality by remember { mutableStateOf(Resolution.RES_720P) }
    var showInStudioEditor by remember { mutableStateOf(true) }

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isConfirmedReady by remember { mutableStateOf(project.isPreviewReviewed) }

    // Layer selection tab for typography editor: 0 = Quran Text, 1 = Surah Header, 2 = Translation, 3 = Reciter
    var selectedLayerTab by remember { mutableIntStateOf(0) }

    // Dialog state for copying typography settings to another aspect ratio
    var showCopySettingsDialog by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    val currentAyahIdx = if (ayahs.isNotEmpty()) {
        val msPerAyah = (totalDurationMs / ayahs.size).coerceAtLeast(1)
        (currentPlaybackMs / msPerAyah).coerceIn(0, ayahs.size - 1)
    } else 0

    val currentAyah = ayahs.getOrNull(currentAyahIdx)

    val (currentCanvasW, currentCanvasH) = project.aspectRatio.getDimensions(previewQuality)

    // Check text fit status in real-time
    val textFitStatus = remember(project, currentAyah, currentCanvasW, currentCanvasH) {
        videoRenderer.checkTextFit(project, currentAyah, currentCanvasW, currentCanvasH)
    }

    // Render preview frame whenever currentAyahIdx or project properties change
    LaunchedEffect(
        project,
        currentAyahIdx,
        previewQuality
    ) {
        val (w, h) = project.aspectRatio.getDimensions(previewQuality)
        val bmp = videoRenderer.renderFrame(
            project = project,
            currentAyahIndex = currentAyahIdx,
            frameAlpha = 1.0f,
            width = w,
            height = h
        )
        previewBitmap = bmp
    }

    // Playback ticker simulation
    LaunchedEffect(isPlaying, previewSpeed) {
        while (isActive && isPlaying) {
            delay(50)
            val step = (50 * previewSpeed).toInt()
            val next = currentPlaybackMs + step
            if (next >= totalDurationMs) {
                currentPlaybackMs = 0
                isPlaying = false
                audioPlayerManager.pause()
            } else {
                currentPlaybackMs = next
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayerManager.stop()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Workspace Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "استوديو المعاينة وضبط التنسيق الفوري",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تحكم بحجم الخط والموضع وملاءمة المقاسات لكل نسبة عرض بشكل مستقل",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight
                    )
                }

                IconButton(
                    onClick = { showInStudioEditor = !showInStudioEditor },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (showInStudioEditor) IslamicGoldPrimary else SurfaceContainerDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "أدوات التنسيق",
                        tint = if (showInStudioEditor) IslamicEmeraldDark else IslamicGoldPrimary
                    )
                }
            }
        }

        // Live Video Player Screen
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, IslamicGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Frame Canvas Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = "شاشة المعاينة الكاملة",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CircularProgressIndicator(color = IslamicGoldPrimary)
                        }

                        // Top Overlay: Aspect Ratio Badge & Verse Indicator
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${project.aspectRatio.label.substringBefore(" ")} • ${currentCanvasW}x${currentCanvasH}",
                                    color = IslamicGoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "الآية ${currentAyah?.numberInSurah ?: project.startAyah} من ${project.endAyah}",
                                    color = IslamicCream,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Bottom Overlay: Safe Area Toggle Indicator
                        if (project.showSafeAreas) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00363A).copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "شبكة الأمان مفعّلة (Safe Area)",
                                    color = Color(0xFF4DD0E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scrubbing Timeline Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimeMs(currentPlaybackMs),
                            color = IslamicCream,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = currentPlaybackMs.toFloat(),
                            onValueChange = { currentPlaybackMs = it.toInt() },
                            valueRange = 0f..totalDurationMs.toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = IslamicGoldPrimary,
                                activeTrackColor = IslamicGoldPrimary
                            )
                        )
                        Text(
                            text = formatTimeMs(totalDurationMs),
                            color = IslamicCream,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Transport Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Ayah
                        IconButton(
                            onClick = {
                                if (currentAyahIdx > 0) {
                                    val msPerAyah = (totalDurationMs / ayahs.size).coerceAtLeast(1)
                                    currentPlaybackMs = (currentAyahIdx - 1) * msPerAyah
                                }
                            },
                            enabled = currentAyahIdx > 0
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "الآية السابقة",
                                tint = if (currentAyahIdx > 0) IslamicCream else IslamicCream.copy(alpha = 0.3f)
                            )
                        }

                        // Play/Pause Main Button
                        IconButton(
                            onClick = {
                                isPlaying = !isPlaying
                                val reciter = reciterRepository.reciters.find { it.id == project.reciterId }
                                if (isPlaying && reciter != null && !audioState.isPlaying) {
                                    val audioUrl = reciterRepository.getAudioUrlForSurah(reciter, project.surahNumber)
                                    audioPlayerManager.play(audioUrl)
                                } else if (!isPlaying) {
                                    audioPlayerManager.pause()
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(IslamicGoldPrimary)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                                tint = IslamicEmeraldDark,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Next Ayah
                        IconButton(
                            onClick = {
                                if (currentAyahIdx < ayahs.size - 1) {
                                    val msPerAyah = (totalDurationMs / ayahs.size).coerceAtLeast(1)
                                    currentPlaybackMs = (currentAyahIdx + 1) * msPerAyah
                                }
                            },
                            enabled = currentAyahIdx < ayahs.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الآية التالية",
                                tint = if (currentAyahIdx < ayahs.size - 1) IslamicCream else IslamicCream.copy(alpha = 0.3f)
                            )
                        }

                        // Mute Toggle
                        IconButton(onClick = {
                            isMuted = !isMuted
                            audioPlayerManager.setVolume(if (isMuted) 0f else 1f)
                        }) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "الصوت",
                                tint = IslamicCream
                            )
                        }
                    }
                }
            }
        }

        // Live Overflow & Text-fit Alert Banner
        item {
            if (!textFitStatus.fitsInsideCanvas || textFitStatus.warningMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF332008)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = textFitStatus.warningMessage ?: "تنبيه: أبعاد النص تقترب من حواف شاشة الفيديو",
                                color = Color(0xFFFFE082),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Fix Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Reduce Font Size by 2sp
                            Button(
                                onClick = {
                                    val newSize = (project.quranTextStyle.fontSizeSp - 2f).coerceAtLeast(project.quranTextStyle.minFontSizeSp)
                                    project = project.copy(
                                        quranTextStyle = project.quranTextStyle.copy(fontSizeSp = newSize)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تصغير الخط (-2)", fontSize = 10.sp, color = IslamicCream)
                            }

                            // Enable Fit-to-Box Mode
                            Button(
                                onClick = {
                                    project = project.copy(
                                        quranTextStyle = project.quranTextStyle.copy(isFitToBox = true)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text("تفعيل الملاءمة (Fit-to-box)", fontSize = 10.sp, color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                            }

                            // Center Text
                            Button(
                                onClick = {
                                    val updatedLayers = project.layers.map {
                                        if (it.id == "quran_text") it.copy(offsetYRatio = 0.48f, offsetXRatio = 0.5f) else it
                                    }
                                    project = project.copy(layers = updatedLayers)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Text("إعادة التوسيط", fontSize = 10.sp, color = IslamicCream)
                            }
                        }
                    }
                }
            } else {
                // Success Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(IslamicEmeraldMedium.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "النص القرآني والترجمة متناسقان تماماً داخل حدود الفيديو (${project.aspectRatio.label.substringBefore(" ")})",
                        color = Color(0xFFA5D6A7),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Dedicated Responsive Typography & Manual Controls Panel
        item {
            AnimatedVisibility(visible = showInStudioEditor) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatSize, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "التحكم اليدوي المتقدم في الخطوط والأبعاد",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IslamicCream,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Copy Settings to other Aspect Ratio Button
                            TextButton(onClick = { showCopySettingsDialog = true }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسخ التنسيق لمقاس آخر", color = IslamicGoldPrimary, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1. Aspect Ratio Independent Selector
                        Text(
                            text = "أبعاد الفيديو (إعدادات مستقلة ومحفوظة لكل مقاس):",
                            color = IslamicGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AspectRatio.values().forEach { ar ->
                                val isSelected = project.aspectRatio == ar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) IslamicGoldPrimary else SurfaceContainerHighDark)
                                        .clickable {
                                            // Switches aspect ratio while preserving independent settings!
                                            project = project.withAspectRatio(ar)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${ar.widthRatio}:${ar.heightRatio}",
                                            color = if (isSelected) IslamicEmeraldDark else IslamicCream,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = when (ar) {
                                                AspectRatio.PORTRAIT_9_16 -> "ريلز"
                                                AspectRatio.LANDSCAPE_16_9 -> "يوتيوب"
                                                AspectRatio.SQUARE_1_1 -> "مربع"
                                                AspectRatio.PORTRAIT_4_5 -> "منشور"
                                            },
                                            color = if (isSelected) IslamicEmeraldDark.copy(alpha = 0.8f) else IslamicCream.copy(alpha = 0.6f),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Presets Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "القوالب الجاهزة للخطوط (Presets):",
                                color = IslamicGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { showSavePresetDialog = true }) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حفظ كقالب مخصص", color = IslamicGoldPrimary, fontSize = 11.sp)
                            }
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            val allPresets = DefaultTypographyPresets.presets + project.customPresets
                            items(allPresets) { preset ->
                                FilterChip(
                                    selected = false,
                                    onClick = { project = project.applyPreset(preset) },
                                    label = { Text(preset.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = SurfaceContainerHighDark,
                                        labelColor = IslamicCream
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Layer Selection Tabs
                        Text(
                            text = "اختر الطبقة لضبط خطها وموضعها يدوياً:",
                            color = IslamicGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val layerTabs = listOf(
                            Pair("نص الآية", "quran_text"),
                            Pair("عنوان السورة", "header"),
                            Pair("الترجمة", "translation"),
                            Pair("القارئ", "reciter")
                        )

                        TabRow(
                            selectedTabIndex = selectedLayerTab,
                            containerColor = SurfaceContainerHighDark,
                            contentColor = IslamicGoldPrimary,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedLayerTab]),
                                    color = IslamicGoldPrimary,
                                    height = 3.dp
                                )
                            }
                        ) {
                            layerTabs.forEachIndexed { index, (label, _) ->
                                Tab(
                                    selected = selectedLayerTab == index,
                                    onClick = { selectedLayerTab = index },
                                    text = {
                                        Text(
                                            text = label,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (selectedLayerTab == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedLayerTab == index) IslamicGoldLight else IslamicCream.copy(alpha = 0.7f)
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Manual Controls for Selected Layer
                        when (selectedLayerTab) {
                            0 -> QuranTextLayerControls(
                                project = project,
                                fonts = fonts,
                                onUpdateProject = { project = it }
                            )
                            1 -> SurahHeaderLayerControls(
                                project = project,
                                fonts = fonts,
                                onUpdateProject = { project = it }
                            )
                            2 -> TranslationLayerControls(
                                project = project,
                                fonts = fonts,
                                onUpdateProject = { project = it }
                            )
                            3 -> ReciterBadgeLayerControls(
                                project = project,
                                fonts = fonts,
                                onUpdateProject = { project = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5. Global Display Guides & Safe Areas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerHighDark)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "إظهار شبكة منطقة الأمان (Safe Area Guides)",
                                    color = IslamicCream,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تحديد الهوامش الآمنة التي لا تغطيها أزرار وتطبيقات الفيديو",
                                    color = IslamicGoldLight,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = project.showSafeAreas,
                                onCheckedChange = { project = project.copy(showSafeAreas = it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = IslamicGoldPrimary,
                                    checkedTrackColor = IslamicEmeraldLight
                                )
                            )
                        }
                    }
                }
            }
        }

        // Final Pre-Export Confirmation & Checklist
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102823)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = IslamicGoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إقرار جودة المعاينة قبل التصدير النهائي",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicCream,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "وفقاً للسياسة الصارمة للاستوديو: يتم تطبيق نفس إعدادات الخطوط والمقاسات والأبعاد بالكامل في الفيديو المصدّر بصيغة MP4.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceContainerDark)
                            .clickable {
                                isConfirmedReady = !isConfirmedReady
                                project = project.copy(isPreviewReviewed = isConfirmedReady)
                                projectRepository.saveProject(project)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConfirmedReady) Icons.Default.CheckCircle else Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (isConfirmedReady) Color(0xFF81C784) else IslamicGoldLight
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "أقر بأنني عاينت الفيديو بالكامل، والنص القرآني والتسجيل والخط جاهزة 100%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = IslamicCream,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            project = project.copy(isPreviewReviewed = true)
                            projectRepository.saveProject(project)
                            onProceedToExport(project)
                        },
                        enabled = isConfirmedReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IslamicGoldPrimary,
                            disabledContainerColor = SurfaceContainerHighDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = IslamicEmeraldDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تأكيد والانتقال لمركز التصدير النهائي (MP4)",
                            color = IslamicEmeraldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Dialog for copying typography settings to another aspect ratio
    if (showCopySettingsDialog) {
        AlertDialog(
            onDismissRequest = { showCopySettingsDialog = false },
            title = {
                Text(
                    text = "نسخ تنسيق (${project.aspectRatio.label.substringBefore(" ")}) إلى مقاس آخر",
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "اختر المقاس المستهدف لنسخ الخطوط والأحجام ومواضع الطبقات إليه دون التأثير على المقاسات الأخرى:",
                        color = IslamicCream,
                        fontSize = 12.sp
                    )
                    AspectRatio.values().filter { it != project.aspectRatio }.forEach { targetAr ->
                        OutlinedButton(
                            onClick = {
                                project = project.copyTypographySettings(from = project.aspectRatio, to = targetAr)
                                showCopySettingsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicGoldLight)
                        ) {
                            Text("نسخ إلى ${targetAr.label}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCopySettingsDialog = false }) {
                    Text("إلغاء", color = IslamicCream)
                }
            },
            containerColor = SurfaceContainerDark
        )
    }

    // Dialog for saving custom preset
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = {
                Text(
                    text = "حفظ التنسيق الحالي كقالب خط مخصص",
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "اكتب اسماً للقالب لحفظ حجم الخط وتباعد الأسطر والخطوط:",
                        color = IslamicCream,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        placeholder = { Text("مثال: قالبي للريلز الفخم") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGoldPrimary,
                            unfocusedBorderColor = IslamicEmeraldLight,
                            focusedTextColor = IslamicCream,
                            unfocusedTextColor = IslamicCream
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            val preset = TypographyPreset(
                                id = "custom_${System.currentTimeMillis()}",
                                name = newPresetName.trim(),
                                description = "قالب مخصص تم إنشاؤه بواسطة المستخدم",
                                quranTextStyle = project.quranTextStyle,
                                translationTextStyle = project.translationTextStyle,
                                surahHeaderStyle = project.surahHeaderStyle,
                                isCustom = true
                            )
                            project = project.copy(customPresets = project.customPresets + preset)
                            showSavePresetDialog = false
                            newPresetName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                ) {
                    Text("حفظ القالب", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("إلغاء", color = IslamicCream)
                }
            },
            containerColor = SurfaceContainerDark
        )
    }
}

/**
 * Dedicated controls for Quran Verse text layer.
 */
@Composable
fun QuranTextLayerControls(
    project: ProjectConfig,
    fonts: List<com.example.data.FontItem>,
    onUpdateProject: (ProjectConfig) -> Unit
) {
    val style = project.quranTextStyle
    val layer = project.layers.find { it.id == "quran_text" } ?: LayerProperties("quran_text", "نص الآية")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Font Selection Chips
        Text(text = "نوع الخط المعتمد:", color = IslamicGoldLight, fontSize = 11.5.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(fonts) { f ->
                val isSelected = style.fontId == f.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onUpdateProject(project.copy(quranTextStyle = style.copy(fontId = f.id)))
                    },
                    label = { Text(f.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = IslamicEmeraldDark
                    )
                )
            }
        }

        // Dedicated Font Size Stepper, Numeric Input & Slider
        ManualFontSizeStepper(
            label = "حجم خط الآيات القرآنية",
            currentSize = style.fontSizeSp,
            minSize = style.minFontSizeSp,
            maxSize = style.maxFontSizeSp,
            onSizeChange = { newSize ->
                onUpdateProject(project.copy(quranTextStyle = style.copy(fontSizeSp = newSize)))
            },
            onReset = {
                val defaultSize = when (project.aspectRatio) {
                    AspectRatio.PORTRAIT_9_16 -> 28f
                    AspectRatio.LANDSCAPE_16_9 -> 24f
                    AspectRatio.SQUARE_1_1 -> 25f
                    AspectRatio.PORTRAIT_4_5 -> 26f
                }
                onUpdateProject(project.copy(quranTextStyle = style.copy(fontSizeSp = defaultSize)))
            }
        )

        // Fit-To-Box mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceContainerHighDark)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "وضع الملاءمة التلقائية (Fit-To-Box)",
                    color = IslamicCream,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (style.isFitToBox) "يقلل الخط تلقائياً عند تجاوز الصندوق المخصص" else "حجم يدوي ثابت مع تنبيه عند التجاوز",
                    color = IslamicGoldLight,
                    fontSize = 9.5.sp
                )
            }
            Switch(
                checked = style.isFitToBox,
                onCheckedChange = { onUpdateProject(project.copy(quranTextStyle = style.copy(isFitToBox = it))) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = IslamicGoldPrimary,
                    checkedTrackColor = IslamicEmeraldLight
                )
            )
        }

        // Max Width Ratio Slider (Padding control)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "عرض صندوق النص: ${(style.maxWidthRatio * 100).toInt()}%", color = IslamicCream, fontSize = 11.5.sp)
        }
        Slider(
            value = style.maxWidthRatio,
            onValueChange = { onUpdateProject(project.copy(quranTextStyle = style.copy(maxWidthRatio = it))) },
            valueRange = 0.60f..0.96f,
            colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
        )

        // Line Spacing Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "تباعد الأسطر: ${String.format("%.2f", style.lineSpacingMultiplier)}x", color = IslamicCream, fontSize = 11.5.sp)
        }
        Slider(
            value = style.lineSpacingMultiplier,
            onValueChange = { onUpdateProject(project.copy(quranTextStyle = style.copy(lineSpacingMultiplier = it))) },
            valueRange = 1.0f..2.2f,
            colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
        )

        // Layer Position & Alignment Controls
        LayerPositionControls(
            layer = layer,
            onUpdateLayer = { updatedLayer ->
                val newLayers = project.layers.map { if (it.id == "quran_text") updatedLayer else it }
                onUpdateProject(project.copy(layers = newLayers))
            }
        )
    }
}

/**
 * Dedicated controls for Surah Header layer.
 */
@Composable
fun SurahHeaderLayerControls(
    project: ProjectConfig,
    fonts: List<com.example.data.FontItem>,
    onUpdateProject: (ProjectConfig) -> Unit
) {
    val style = project.surahHeaderStyle
    val layer = project.layers.find { it.id == "header" } ?: LayerProperties("header", "عنوان السورة")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ManualFontSizeStepper(
            label = "حجم خط عنوان السورة",
            currentSize = style.fontSizeSp,
            minSize = style.minFontSizeSp,
            maxSize = style.maxFontSizeSp,
            onSizeChange = { newSize ->
                onUpdateProject(project.copy(surahHeaderStyle = style.copy(fontSizeSp = newSize)))
            },
            onReset = {
                onUpdateProject(project.copy(surahHeaderStyle = style.copy(fontSizeSp = 20f)))
            }
        )

        LayerPositionControls(
            layer = layer,
            onUpdateLayer = { updatedLayer ->
                val newLayers = project.layers.map { if (it.id == "header") updatedLayer else it }
                onUpdateProject(project.copy(layers = newLayers))
            }
        )
    }
}

/**
 * Dedicated controls for Translation & Tafsir layer.
 */
@Composable
fun TranslationLayerControls(
    project: ProjectConfig,
    fonts: List<com.example.data.FontItem>,
    onUpdateProject: (ProjectConfig) -> Unit
) {
    val style = project.translationTextStyle
    val layer = project.layers.find { it.id == "translation" } ?: LayerProperties("translation", "الترجمة")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ManualFontSizeStepper(
            label = "حجم خط الترجمة والتفسير",
            currentSize = style.fontSizeSp,
            minSize = style.minFontSizeSp,
            maxSize = style.maxFontSizeSp,
            onSizeChange = { newSize ->
                onUpdateProject(project.copy(translationTextStyle = style.copy(fontSizeSp = newSize)))
            },
            onReset = {
                onUpdateProject(project.copy(translationTextStyle = style.copy(fontSizeSp = 15f)))
            }
        )

        LayerPositionControls(
            layer = layer,
            onUpdateLayer = { updatedLayer ->
                val newLayers = project.layers.map { if (it.id == "translation") updatedLayer else it }
                onUpdateProject(project.copy(layers = newLayers))
            }
        )
    }
}

/**
 * Dedicated controls for Reciter Badge layer.
 */
@Composable
fun ReciterBadgeLayerControls(
    project: ProjectConfig,
    fonts: List<com.example.data.FontItem>,
    onUpdateProject: (ProjectConfig) -> Unit
) {
    val style = project.reciterBadgeStyle
    val layer = project.layers.find { it.id == "reciter" } ?: LayerProperties("reciter", "اسم القارئ")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ManualFontSizeStepper(
            label = "حجم خط شارة القارئ",
            currentSize = style.fontSizeSp,
            minSize = style.minFontSizeSp,
            maxSize = style.maxFontSizeSp,
            onSizeChange = { newSize ->
                onUpdateProject(project.copy(reciterBadgeStyle = style.copy(fontSizeSp = newSize)))
            },
            onReset = {
                onUpdateProject(project.copy(reciterBadgeStyle = style.copy(fontSizeSp = 13f)))
            }
        )

        LayerPositionControls(
            layer = layer,
            onUpdateLayer = { updatedLayer ->
                val newLayers = project.layers.map { if (it.id == "reciter") updatedLayer else it }
                onUpdateProject(project.copy(layers = newLayers))
            }
        )
    }
}

private fun formatTimeMs(ms: Int): String {
    val totalSec = ms / 1000
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format("%02d:%02d", minutes, seconds)
}
