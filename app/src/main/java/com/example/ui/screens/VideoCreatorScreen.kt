package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.FontManager
import com.example.data.ProjectRepository
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import com.example.engine.AspectRatio
import com.example.engine.AudioPlayerManager
import com.example.engine.BackgroundType
import com.example.engine.ProjectConfig
import com.example.engine.Resolution
import com.example.engine.TextAnimation
import com.example.engine.TypographyStyle
import com.example.engine.VideoRenderer
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.ManualFontSizeStepper
import com.example.ui.components.FontUploadDialog
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCreatorScreen(
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
    fontManager: FontManager,
    projectRepository: ProjectRepository,
    videoRenderer: VideoRenderer,
    audioPlayerManager: AudioPlayerManager,
    initialProject: ProjectConfig? = null,
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var project by remember { mutableStateOf(initialProject ?: ProjectConfig()) }
    val fonts by fontManager.fonts.collectAsState()
    val audioState by audioPlayerManager.state.collectAsState()
    val renderProgress by videoRenderer.progress.collectAsState()

    var currentPreviewAyahIdx by remember { mutableIntStateOf(0) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSurahDropdownExpanded by remember { mutableStateOf(false) }
    var isReciterDropdownExpanded by remember { mutableStateOf(false) }
    var showFontUploadDialog by remember { mutableStateOf(false) }
    var showRenderModal by remember { mutableStateOf(false) }
    var renderJob by remember { mutableStateOf<Job?>(null) }

    val currentSurah = quranRepository.allSurahs.find { it.number == project.surahNumber }
        ?: quranRepository.allSurahs[0]
    val currentReciter = reciterRepository.reciters.find { it.id == project.reciterId }
        ?: reciterRepository.reciters[0]

    // Launcher for custom background image upload
    val bgImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val input = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "custom_bg_${System.currentTimeMillis()}.jpg")
                FileOutputStream(targetFile).use { out -> input?.copyTo(out) }
                project = project.copy(
                    backgroundType = BackgroundType.CUSTOM_IMAGE,
                    customBackgroundImagePath = targetFile.absolutePath
                )
            } catch (e: Exception) {
                // error fallback
            }
        }
    }

    // Refresh live preview frame whenever relevant configuration changes
    LaunchedEffect(
        project.surahNumber,
        project.startAyah,
        project.endAyah,
        project.aspectRatio,
        project.backgroundType,
        project.customBackgroundImagePath,
        project.quranTextStyle,
        project.translationTextStyle,
        project.surahHeaderStyle,
        project.showIslamicFrame,
        project.showTranslation,
        project.showTafsir,
        project.showSurahHeader,
        project.showReciterBadge,
        currentPreviewAyahIdx
    ) {
        val (w, h) = project.aspectRatio.getDimensions(Resolution.RES_720P)
        val bmp = videoRenderer.renderFrame(
            project = project,
            currentAyahIndex = currentPreviewAyahIdx,
            frameAlpha = 1.0f,
            width = w,
            height = h
        )
        previewBitmap = bmp
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayerManager.stop()
        }
    }

    if (showFontUploadDialog) {
        FontUploadDialog(
            fontManager = fontManager,
            onDismiss = { showFontUploadDialog = false },
            onSuccess = { showFontUploadDialog = false }
        )
    }

    // Video Exporting Progress / Result Dialog
    if (showRenderModal) {
        AlertDialog(
            onDismissRequest = {
                if (!renderProgress.isRendering) showRenderModal = false
            },
            title = {
                Text(
                    text = if (renderProgress.isRendering) "جاري تصدير الفيديو بتقنية H.264..." else "اكتمل التصدير!",
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (renderProgress.isRendering) {
                        CircularProgressIndicator(color = IslamicGoldPrimary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { renderProgress.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = IslamicGoldPrimary,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = renderProgress.statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IslamicCream,
                            textAlign = TextAlign.Center
                        )
                    } else if (renderProgress.outputFilePath != null) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "تم إنتاج وحفظ ملف الفيديو بنجاح بدقة ${project.resolution.label}!",
                            color = IslamicCream,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = File(renderProgress.outputFilePath!!).name,
                            color = IslamicGoldLight,
                            fontSize = 12.sp
                        )
                    } else if (renderProgress.error != null) {
                        Text(text = renderProgress.error!!, color = Color(0xFFCF6679))
                    }
                }
            },
            confirmButton = {
                if (!renderProgress.isRendering && renderProgress.outputFilePath != null) {
                    Row {
                        Button(
                            onClick = {
                                val file = File(renderProgress.outputFilePath!!)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الفيديو القرآني"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = IslamicEmeraldDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "مشاركة", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { showRenderModal = false }) {
                            Text(text = "إغلاق", color = IslamicCream)
                        }
                    }
                } else if (renderProgress.isRendering) {
                    OutlinedButton(
                        onClick = {
                            renderJob?.cancel()
                            showRenderModal = false
                        }
                    ) {
                        Text(text = "إلغاء التصدير", color = Color(0xFFCF6679))
                    }
                }
            },
            containerColor = Color(0xFF132B26)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "صانع الفيديو القرآني التلقائي",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "توليد فوري مع المعاينة الحية والتصدير الحقيقي",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            projectRepository.saveProject(project)
                            onNavigate("preview")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldMedium),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = IslamicGoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "المعاينة الكاملة", color = IslamicCream, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            projectRepository.saveProject(project)
                            onNavigate("export")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = IslamicEmeraldDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "تصدير MP4", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Real-Time Interactive Canvas Preview
        item {
            LiveVideoPreviewCard(
                project = project,
                previewBitmap = previewBitmap,
                currentAyahIdx = currentPreviewAyahIdx,
                onPreviousAyah = { currentPreviewAyahIdx = (currentPreviewAyahIdx - 1).coerceAtLeast(0) },
                onNextAyah = {
                    val max = (project.endAyah - project.startAyah).coerceAtLeast(0)
                    currentPreviewAyahIdx = (currentPreviewAyahIdx + 1).coerceAtMost(max)
                }
            )
        }

        // Audio Player Controls & Waveform
        item {
            AudioBar(
                reciterName = currentReciter.arabicName,
                audioState = audioState,
                onTogglePlay = {
                    val audioUrl = reciterRepository.getAudioUrlForSurah(currentReciter, project.surahNumber)
                    audioPlayerManager.play(audioUrl)
                }
            )
        }

        // Aspect Ratio Selector
        item {
            SectionTitle(title = "أبعاد ومنصة الفيديو (Aspect Ratio)")
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AspectRatio.values().forEach { ar ->
                    val isSelected = project.aspectRatio == ar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) IslamicGoldPrimary else SurfaceContainerDark)
                            .border(1.dp, if (isSelected) IslamicGoldLight else IslamicEmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { project = project.withAspectRatio(ar) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ar.widthRatio.toString() + ":" + ar.heightRatio.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) IslamicEmeraldDark else IslamicCream,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Surah & Verse Range Selector
        item {
            SectionTitle(title = "اختيار السورة ونطاق الآيات")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Surah Picker
                    ExposedDropdownMenuBox(
                        expanded = isSurahDropdownExpanded,
                        onExpandedChange = { isSurahDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${currentSurah.number}. سورة ${currentSurah.arabicName} (${currentSurah.versesCount} آية)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("السورة الكريمة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSurahDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isSurahDropdownExpanded,
                            onDismissRequest = { isSurahDropdownExpanded = false }
                        ) {
                            quranRepository.allSurahs.forEach { surah ->
                                DropdownMenuItem(
                                    text = { Text("${surah.number}. سورة ${surah.arabicName} (${surah.versesCount} آية)") },
                                    onClick = {
                                        project = project.copy(
                                            surahNumber = surah.number,
                                            startAyah = 1,
                                            endAyah = surah.versesCount.coerceAtMost(7)
                                        )
                                        currentPreviewAyahIdx = 0
                                        isSurahDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Verse Range Sliders
                    Text(
                        text = "نطاق الآيات: من الآية ${project.startAyah} إلى الآية ${project.endAyah}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "من الآية: ${project.startAyah}", color = IslamicCream, fontSize = 12.sp)
                            Slider(
                                value = project.startAyah.toFloat(),
                                onValueChange = {
                                    val newStart = it.toInt()
                                    project = project.copy(
                                        startAyah = newStart,
                                        endAyah = project.endAyah.coerceAtLeast(newStart)
                                    )
                                    currentPreviewAyahIdx = 0
                                },
                                valueRange = 1f..currentSurah.versesCount.toFloat(),
                                colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "إلى الآية: ${project.endAyah}", color = IslamicCream, fontSize = 12.sp)
                            Slider(
                                value = project.endAyah.toFloat(),
                                onValueChange = {
                                    val newEnd = it.toInt()
                                    project = project.copy(
                                        endAyah = newEnd,
                                        startAyah = project.startAyah.coerceAtMost(newEnd)
                                    )
                                    currentPreviewAyahIdx = 0
                                },
                                valueRange = 1f..currentSurah.versesCount.toFloat(),
                                colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
                            )
                        }
                    }
                }
            }
        }

        // Reciter Selection
        item {
            SectionTitle(title = "اختيار القارئ المعتمد")
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = isReciterDropdownExpanded,
                onExpandedChange = { isReciterDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${currentReciter.arabicName} (${currentReciter.riwayah})",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("القارئ") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isReciterDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isReciterDropdownExpanded,
                    onDismissRequest = { isReciterDropdownExpanded = false }
                ) {
                    reciterRepository.reciters.forEach { reciter ->
                        DropdownMenuItem(
                            text = { Text("${reciter.arabicName} - ${reciter.riwayah}") },
                            onClick = {
                                project = project.copy(reciterId = reciter.id)
                                isReciterDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Custom Font Selection Section (CRITICAL REQUIREMENT)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(title = "خط الآيات القرآنية (Font Family)")
                TextButton(onClick = { showFontUploadDialog = true }) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "رفع خطك المخصص", color = IslamicGoldPrimary, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fonts) { font ->
                    val isSelected = project.quranTextStyle.fontId == font.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1B3D34) else SurfaceContainerDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(1.dp, if (isSelected) IslamicGoldPrimary else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable {
                                project = project.copy(
                                    quranTextStyle = project.quranTextStyle.copy(fontId = font.id)
                                )
                            }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = font.displayName,
                                color = if (isSelected) IslamicGoldLight else IslamicCream,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (font.isCustom) "خط مخصص (${font.format})" else "مدمج",
                                color = IslamicCream.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ManualFontSizeStepper(
                label = "حجم خط الآيات القرآنية (${project.aspectRatio.label.substringBefore(" ")})",
                currentSize = project.quranTextStyle.fontSizeSp,
                minSize = project.quranTextStyle.minFontSizeSp,
                maxSize = project.quranTextStyle.maxFontSizeSp,
                onSizeChange = { newSize ->
                    project = project.copy(
                        quranTextStyle = project.quranTextStyle.copy(fontSizeSp = newSize)
                    )
                },
                onReset = {
                    val defaultSize = when (project.aspectRatio) {
                        AspectRatio.PORTRAIT_9_16 -> 28f
                        AspectRatio.LANDSCAPE_16_9 -> 24f
                        AspectRatio.SQUARE_1_1 -> 25f
                        AspectRatio.PORTRAIT_4_5 -> 26f
                    }
                    project = project.copy(
                        quranTextStyle = project.quranTextStyle.copy(fontSizeSp = defaultSize)
                    )
                }
            )
        }

        // Background Selection Section
        item {
            SectionTitle(title = "الخلفية والمشهد البصري")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BackgroundOptionChip(
                    label = "المسجد النبوي",
                    isSelected = project.backgroundType == BackgroundType.PRESET_MOSQUE,
                    onClick = { project = project.copy(backgroundType = BackgroundType.PRESET_MOSQUE) },
                    modifier = Modifier.weight(1f)
                )
                BackgroundOptionChip(
                    label = "سماء النجوم",
                    isSelected = project.backgroundType == BackgroundType.PRESET_STARS,
                    onClick = { project = project.copy(backgroundType = BackgroundType.PRESET_STARS) },
                    modifier = Modifier.weight(1f)
                )
                BackgroundOptionChip(
                    label = "تدرج زمردي",
                    isSelected = project.backgroundType == BackgroundType.EMERALD_GOLD_GRADIENT,
                    onClick = { project = project.copy(backgroundType = BackgroundType.EMERALD_GOLD_GRADIENT) },
                    modifier = Modifier.weight(1f)
                )
                BackgroundOptionChip(
                    label = "رفع صورة",
                    isSelected = project.backgroundType == BackgroundType.CUSTOM_IMAGE,
                    onClick = { bgImagePicker.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Layer & Overlay Toggles
        item {
            SectionTitle(title = "الطبقات والخيارات الإضافية")
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    CheckboxRow(
                        label = "إظهار الترجمة الإنجليزية أسفل الآية",
                        checked = project.showTranslation,
                        onCheckedChange = { project = project.copy(showTranslation = it) }
                    )
                    CheckboxRow(
                        label = "إظهار التفسير الميسر",
                        checked = project.showTafsir,
                        onCheckedChange = { project = project.copy(showTafsir = it) }
                    )
                    CheckboxRow(
                        label = "إظهار الإطار والزخرفة الإسلامية الذهبية",
                        checked = project.showIslamicFrame,
                        onCheckedChange = { project = project.copy(showIslamicFrame = it) }
                    )
                    CheckboxRow(
                        label = "إظهار اسم القارئ في الأسفل",
                        checked = project.showReciterBadge,
                        onCheckedChange = { project = project.copy(showReciterBadge = it) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun LiveVideoPreviewCard(
    project: ProjectConfig,
    previewBitmap: Bitmap?,
    currentAyahIdx: Int,
    onPreviousAyah: () -> Unit,
    onNextAyah: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF071412)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Preview Frame Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "المعاينة الحية للفيديو",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator(color = IslamicGoldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation between verses for preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousAyah, enabled = currentAyahIdx > 0) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "الآية السابقة", tint = IslamicCream)
                }

                Text(
                    text = "معاينة الآية رقم ${project.startAyah + currentAyahIdx} من ${project.endAyah}",
                    color = IslamicGoldLight,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onNextAyah,
                    enabled = currentAyahIdx < (project.endAyah - project.startAyah)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "الآية التالية", tint = IslamicCream)
                }
            }
        }
    }
}

@Composable
fun AudioBar(
    reciterName: String,
    audioState: com.example.engine.AudioPlayerState,
    onTogglePlay: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IslamicGoldPrimary)
                ) {
                    Icon(
                        imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل / إيقاف الصوت",
                        tint = IslamicEmeraldDark
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "صوت التلاوة: $reciterName",
                        style = MaterialTheme.typography.titleSmall,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (audioState.isPlaying) "جاري الاستماع للتلاوة..." else "انقر للمعاينة الصوتية ومزامنة الآيات",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            AudioWaveformVisualizer(
                isPlaying = audioState.isPlaying,
                modifier = Modifier.width(70.dp)
            )
        }
    }
}

@Composable
fun BackgroundOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF1B3D34) else SurfaceContainerDark)
            .border(1.dp, if (isSelected) IslamicGoldPrimary else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) IslamicGoldLight else IslamicCream,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = IslamicGoldPrimary,
                checkmarkColor = IslamicEmeraldDark
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = IslamicCream, fontSize = 13.sp)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = IslamicGoldPrimary,
        fontWeight = FontWeight.Bold
    )
}
