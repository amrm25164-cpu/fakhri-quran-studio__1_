package com.example.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FontManager
import com.example.data.ProjectRepository
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import com.example.engine.AspectRatio
import com.example.engine.LayerProperties
import com.example.engine.ProjectConfig
import com.example.engine.Resolution
import com.example.engine.VideoRenderer
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

@Composable
fun TimelineEditorScreen(
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
    fontManager: FontManager,
    projectRepository: ProjectRepository,
    videoRenderer: VideoRenderer,
    onBack: () -> Unit = {}
) {
    var project by remember { mutableStateOf(projectRepository.projects.value.firstOrNull() ?: ProjectConfig()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Layers, 1: Typography, 2: Timing
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(project) {
        val (w, h) = project.aspectRatio.getDimensions(Resolution.RES_720P)
        previewBitmap = videoRenderer.renderFrame(project, 0, 1f, w, h)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "محرر الطبقات والتايم لاين الاحترافي",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تحكم متعدد الطبقات • الخطوط • الشفافية • الظلال",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
            }

            Button(
                onClick = { projectRepository.saveProject(project) },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "حفظ التعديلات", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Live Small Preview Monitor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (previewBitmap != null) {
                Image(
                    bitmap = previewBitmap!!.asImageBitmap(),
                    contentDescription = "معاينة الطبقات",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceContainerDark,
            contentColor = IslamicGoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = IslamicGoldPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("إدارة الطبقات (Layers)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("الخط والظل (Typography)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("التوقيت والانتقال (Timing)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> LayersTabContent(
                    layers = project.layers,
                    onUpdateLayers = { project = project.copy(layers = it) }
                )
                1 -> TypographyTabContent(
                    project = project,
                    onUpdateProject = { project = it }
                )
                2 -> TimingTabContent(
                    project = project,
                    onUpdateProject = { project = it }
                )
            }
        }
    }
}

@Composable
fun LayersTabContent(
    layers: List<LayerProperties>,
    onUpdateLayers: (List<LayerProperties>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(layers, key = { it.id }) { layer ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val updated = layers.map {
                                    if (it.id == layer.id) it.copy(isVisible = !it.isVisible) else it
                                }
                                onUpdateLayers(updated)
                            }
                        ) {
                            Icon(
                                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "إظهار / إخفاء",
                                tint = if (layer.isVisible) IslamicGoldPrimary else IslamicCream.copy(alpha = 0.4f)
                            )
                        }

                        IconButton(
                            onClick = {
                                val updated = layers.map {
                                    if (it.id == layer.id) it.copy(isLocked = !it.isLocked) else it
                                }
                                onUpdateLayers(updated)
                            }
                        ) {
                            Icon(
                                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "قفل الطبقة",
                                tint = if (layer.isLocked) Color(0xFFE57373) else IslamicCream.copy(alpha = 0.4f)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = layer.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = IslamicCream,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الشفافية: ${(layer.opacity * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicGoldLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Opacity Slider
                    Slider(
                        value = layer.opacity,
                        onValueChange = { newOp ->
                            val updated = layers.map {
                                if (it.id == layer.id) it.copy(opacity = newOp) else it
                            }
                            onUpdateLayers(updated)
                        },
                        modifier = Modifier.width(100.dp),
                        colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun TypographyTabContent(
    project: ProjectConfig,
    onUpdateProject: (ProjectConfig) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Aspect Ratio Selector
        item {
            Text(
                text = "المقاس النشط (إعدادات مستقلة ومحفوظة):",
                style = MaterialTheme.typography.titleSmall,
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold
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
                            .background(if (isSelected) IslamicGoldPrimary else SurfaceContainerDark)
                            .clickable { onUpdateProject(project.withAspectRatio(ar)) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${ar.widthRatio}:${ar.heightRatio}",
                            color = if (isSelected) IslamicEmeraldDark else IslamicCream,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        // Quran Verse Text Stepper
        item {
            ManualFontSizeStepper(
                label = "حجم خط الآيات القرآنية",
                currentSize = project.quranTextStyle.fontSizeSp,
                minSize = project.quranTextStyle.minFontSizeSp,
                maxSize = project.quranTextStyle.maxFontSizeSp,
                onSizeChange = { newSize ->
                    onUpdateProject(project.copy(quranTextStyle = project.quranTextStyle.copy(fontSizeSp = newSize)))
                },
                onReset = {
                    val defaultSize = when (project.aspectRatio) {
                        AspectRatio.PORTRAIT_9_16 -> 28f
                        AspectRatio.LANDSCAPE_16_9 -> 24f
                        AspectRatio.SQUARE_1_1 -> 25f
                        AspectRatio.PORTRAIT_4_5 -> 26f
                    }
                    onUpdateProject(project.copy(quranTextStyle = project.quranTextStyle.copy(fontSizeSp = defaultSize)))
                }
            )
        }

        // Surah Header Stepper
        item {
            ManualFontSizeStepper(
                label = "حجم خط عنوان السورة",
                currentSize = project.surahHeaderStyle.fontSizeSp,
                minSize = project.surahHeaderStyle.minFontSizeSp,
                maxSize = project.surahHeaderStyle.maxFontSizeSp,
                onSizeChange = { newSize ->
                    onUpdateProject(project.copy(surahHeaderStyle = project.surahHeaderStyle.copy(fontSizeSp = newSize)))
                },
                onReset = {
                    onUpdateProject(project.copy(surahHeaderStyle = project.surahHeaderStyle.copy(fontSizeSp = 20f)))
                }
            )
        }

        // Translation Stepper
        item {
            ManualFontSizeStepper(
                label = "حجم خط الترجمة والتفسير",
                currentSize = project.translationTextStyle.fontSizeSp,
                minSize = project.translationTextStyle.minFontSizeSp,
                maxSize = project.translationTextStyle.maxFontSizeSp,
                onSizeChange = { newSize ->
                    onUpdateProject(project.copy(translationTextStyle = project.translationTextStyle.copy(fontSizeSp = newSize)))
                },
                onReset = {
                    onUpdateProject(project.copy(translationTextStyle = project.translationTextStyle.copy(fontSizeSp = 15f)))
                }
            )
        }

        // Reciter Badge Stepper
        item {
            ManualFontSizeStepper(
                label = "حجم خط شارة القارئ",
                currentSize = project.reciterBadgeStyle.fontSizeSp,
                minSize = project.reciterBadgeStyle.minFontSizeSp,
                maxSize = project.reciterBadgeStyle.maxFontSizeSp,
                onSizeChange = { newSize ->
                    onUpdateProject(project.copy(reciterBadgeStyle = project.reciterBadgeStyle.copy(fontSizeSp = newSize)))
                },
                onReset = {
                    onUpdateProject(project.copy(reciterBadgeStyle = project.reciterBadgeStyle.copy(fontSizeSp = 13f)))
                }
            )
        }
    }
}

@Composable
fun TimingTabContent(
    project: ProjectConfig,
    onUpdateProject: (ProjectConfig) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "مدة عرض الآية الواحدة على الشاشة",
                style = MaterialTheme.typography.titleSmall,
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "مدة الآية: ${project.verseDurationSeconds} ثواني",
                        color = IslamicCream,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = project.verseDurationSeconds.toFloat(),
                        onValueChange = {
                            onUpdateProject(project.copy(verseDurationSeconds = it.toInt()))
                        },
                        valueRange = 3f..20f,
                        colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
                    )
                    Text(
                        text = "يتم احتساب مدة الفيديو الإجمالية بناءً على عدد الآيات المحددة مضروبة في هذا التوقيت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
