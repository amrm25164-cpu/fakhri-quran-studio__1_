package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.FontManager
import com.example.data.ProjectRepository
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import com.example.engine.AspectRatio
import com.example.engine.ProjectConfig
import com.example.engine.Resolution
import com.example.engine.VideoRenderer
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import java.io.File

@Composable
fun ThumbnailEditorScreen(
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
    fontManager: FontManager,
    projectRepository: ProjectRepository,
    videoRenderer: VideoRenderer
) {
    val context = LocalContext.current
    var project by remember {
        mutableStateOf(
            ProjectConfig(
                aspectRatio = AspectRatio.LANDSCAPE_16_9,
                title = "سورة الفاتحة تلاوة خاشعة",
                surahNumber = 1,
                startAyah = 1,
                endAyah = 2
            )
        )
    }

    var thumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var exportStatusText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(project) {
        val (w, h) = project.aspectRatio.getDimensions(Resolution.RES_720P)
        thumbnailBitmap = videoRenderer.renderFrame(project, 0, 1.0f, w, h)
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
                        text = "مصمم الصور المصغرة (Thumbnail Designer)",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تصميم أغلفة يوتيوب وفيسبوك وإنستغرام بدقة فائقة وبنفس الخطوط المخصصة",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight
                    )
                }

                Button(
                    onClick = {
                        val file = videoRenderer.exportThumbnail(project, Bitmap.CompressFormat.JPEG, 95)
                        exportedFile = file
                        exportStatusText = "تم تصدير وحفظ الصورة المصغرة بنجاح!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = IslamicEmeraldDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تصدير JPEG", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Thumbnail Preview Canvas
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbnailBitmap != null) {
                        Image(
                            bitmap = thumbnailBitmap!!.asImageBitmap(),
                            contentDescription = "معاينة الغلاف",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = IslamicGoldPrimary)
                    }
                }
            }
        }

        // Export notification banner
        exportStatusText?.let { msg ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF143029)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = msg, color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (exportedFile != null) {
                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", exportedFile!!)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة الصورة المصغرة"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = IslamicEmeraldDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "مشاركة", color = IslamicEmeraldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "أبعاد الغلاف:",
                        color = IslamicCream,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { project = project.copy(aspectRatio = AspectRatio.LANDSCAPE_16_9) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (project.aspectRatio == AspectRatio.LANDSCAPE_16_9) IslamicGoldPrimary else SurfaceContainerDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "16:9 يوتيوب",
                                color = if (project.aspectRatio == AspectRatio.LANDSCAPE_16_9) IslamicEmeraldDark else IslamicCream,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { project = project.copy(aspectRatio = AspectRatio.SQUARE_1_1) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (project.aspectRatio == AspectRatio.SQUARE_1_1) IslamicGoldPrimary else SurfaceContainerDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "1:1 مربع",
                                color = if (project.aspectRatio == AspectRatio.SQUARE_1_1) IslamicEmeraldDark else IslamicCream,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { project = project.copy(aspectRatio = AspectRatio.PORTRAIT_9_16) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (project.aspectRatio == AspectRatio.PORTRAIT_9_16) IslamicGoldPrimary else SurfaceContainerDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "9:16 ريلز",
                                color = if (project.aspectRatio == AspectRatio.PORTRAIT_9_16) IslamicEmeraldDark else IslamicCream,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "سطوع الخلفية (Brightness): ${(project.backgroundBrightness * 100).toInt()}%",
                        color = IslamicCream,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = project.backgroundBrightness,
                        onValueChange = { project = project.copy(backgroundBrightness = it) },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = IslamicGoldPrimary, activeTrackColor = IslamicGoldPrimary)
                    )
                }
            }
        }
    }
}
