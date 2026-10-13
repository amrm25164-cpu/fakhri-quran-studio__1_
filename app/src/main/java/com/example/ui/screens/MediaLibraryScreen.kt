package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.BackgroundType
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import java.io.File
import java.io.FileOutputStream

data class MediaItem(
    val id: String,
    val name: String,
    val type: BackgroundType,
    val resId: Int? = null,
    val localFilePath: String? = null
)

@Composable
fun MediaLibraryScreen(
    currentBackgroundType: BackgroundType,
    onSelectBackground: (BackgroundType, String?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(currentBackgroundType) }

    val presetItems = remember {
        listOf(
            MediaItem("bg_mosque", "المسجد النبوي الشريف", BackgroundType.PRESET_MOSQUE, R.drawable.bg_mosque),
            MediaItem("bg_stars", "سماء النجوم والكواكب", BackgroundType.PRESET_STARS, R.drawable.bg_stars),
            MediaItem("bg_emerald", "تدرج زمردي وذهب ملكي", BackgroundType.EMERALD_GOLD_GRADIENT),
            MediaItem("bg_dark", "أسود ملكي هادئ (Minimalist)", BackgroundType.DARK_GRADIENT)
        )
    }

    val customMediaList = remember { mutableStateListOf<MediaItem>() }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val input = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "user_bg_${System.currentTimeMillis()}.jpg")
                FileOutputStream(targetFile).use { out -> input?.copyTo(out) }
                val item = MediaItem(
                    id = "custom_${System.currentTimeMillis()}",
                    name = "خلفية مرفوعة من الهاتف",
                    type = BackgroundType.CUSTOM_IMAGE,
                    localFilePath = targetFile.absolutePath
                )
                customMediaList.add(item)
                selectedType = BackgroundType.CUSTOM_IMAGE
                onSelectBackground(BackgroundType.CUSTOM_IMAGE, targetFile.absolutePath)
            } catch (e: Exception) {
                // error
            }
        }
    }

    val allItems = customMediaList + presetItems

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مكتبة الخلفيات والوسائط المرئية",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مشاهد إسلامية وطبيعية عالية الدقة مع إمكانية رفع صورك الخاصة",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
            }

            Button(
                onClick = { filePicker.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = IslamicEmeraldDark)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "رفع صورة", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(allItems, key = { it.id }) { item ->
                val isSelected = selectedType == item.type
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .border(
                            2.dp,
                            if (isSelected) IslamicGoldPrimary else IslamicEmeraldLight.copy(alpha = 0.3f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            selectedType = item.type
                            onSelectBackground(item.type, item.localFilePath)
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (item.resId != null) {
                            Image(
                                painter = painterResource(id = item.resId),
                                contentDescription = item.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (item.type == BackgroundType.EMERALD_GOLD_GRADIENT) Color(0xFF0D2823) else Color(0xFF090D0C)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(36.dp))
                            }
                        }

                        // Bottom label overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    color = IslamicCream,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = IslamicGoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
