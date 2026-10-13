package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Reciter
import com.example.data.ReciterRepository
import com.example.engine.AudioPlayerManager
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark
import java.io.File
import java.io.FileOutputStream

@Composable
fun RecitersScreen(
    reciterRepository: ReciterRepository,
    audioPlayerManager: AudioPlayerManager,
    onSelectReciterForProject: (Reciter) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurahNumber by remember { mutableIntStateOf(1) } // Default Al-Fatihah
    val audioState by audioPlayerManager.state.collectAsState()

    var customReciters by remember { mutableStateOf<List<Reciter>>(emptyList()) }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val input = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "custom_reciter_${System.currentTimeMillis()}.mp3")
                FileOutputStream(targetFile).use { out -> input?.copyTo(out) }
                val newReciter = Reciter(
                    id = "custom_${System.currentTimeMillis()}",
                    arabicName = "تلاوة مخصصة مرفوعة",
                    englishName = "Custom Audio Recording",
                    riwayah = "تسجيل شخصي",
                    serverBaseUrl = "",
                    isCustom = true,
                    customAudioUri = targetFile.absolutePath
                )
                customReciters = customReciters + newReciter
            } catch (e: Exception) {
                // error
            }
        }
    }

    val allReciters = customReciters + reciterRepository.reciters
    val filtered = remember(searchQuery, allReciters) {
        if (searchQuery.isBlank()) allReciters else allReciters.filter {
            it.arabicName.contains(searchQuery) || it.englishName.contains(searchQuery, ignoreCase = true)
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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مكتبة كبار القراء والتسجيلات",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "16 قارئاً معتمداً مع إمكانية رفع تلاواتك الشخصية",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight
                    )
                }

                Button(
                    onClick = { audioFilePicker.launch("audio/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = IslamicEmeraldDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "رفع تلاوة MP3", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Active Playback Controls Strip
        item {
            ActivePlaybackBar(
                audioState = audioState,
                audioPlayerManager = audioPlayerManager
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث عن القارئ (مثال: العفاسي، السديس، المنشاوي)...", color = IslamicCream.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Reciters List
        items(filtered, key = { it.id }) { reciter ->
            ReciterCard(
                reciter = reciter,
                isSelectedPlaying = audioState.isPlaying && audioState.currentUrlOrUri?.contains(reciter.id) == true,
                onPlay = {
                    val url = reciterRepository.getAudioUrlForSurah(reciter, selectedSurahNumber)
                    audioPlayerManager.play(url)
                },
                onPause = { audioPlayerManager.pause() },
                onSelectForProject = { onSelectReciterForProject(reciter) }
            )
        }
    }
}

@Composable
fun ActivePlaybackBar(
    audioState: com.example.engine.AudioPlayerState,
    audioPlayerManager: AudioPlayerManager
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سرعة التشغيل: ${audioState.playbackSpeed}x",
                        color = IslamicCream,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                        val isSelected = audioState.playbackSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) IslamicGoldPrimary else SurfaceContainerHighDark)
                                .clickable { audioPlayerManager.setSpeed(speed) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (isSelected) IslamicEmeraldDark else IslamicCream,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Waveform & Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AudioWaveformVisualizer(
                    isPlaying = audioState.isPlaying,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = IslamicCream.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = audioState.volume,
                        onValueChange = { audioPlayerManager.setVolume(it) },
                        modifier = Modifier.width(90.dp),
                        colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun ReciterCard(
    reciter: Reciter,
    isSelectedPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSelectForProject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (isSelectedPlaying) onPause() else onPlay() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isSelectedPlaying) IslamicGoldPrimary else IslamicEmeraldMedium)
                ) {
                    Icon(
                        imageVector = if (isSelectedPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل",
                        tint = if (isSelectedPlaying) IslamicEmeraldDark else IslamicGoldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = reciter.arabicName,
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${reciter.englishName} • ${reciter.riwayah}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight,
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = "جودة البث: ${reciter.quality} • سيرفر موثق",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream.copy(alpha = 0.45f),
                        fontSize = 10.5.sp
                    )
                }
            }

            Button(
                onClick = onSelectForProject,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldMedium),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "اختيار للمشروع", color = IslamicCream, fontSize = 11.sp)
            }
        }
    }
}
