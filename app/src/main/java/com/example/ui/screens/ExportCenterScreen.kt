package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.compose.foundation.clickable
import com.example.engine.ProjectConfig
import com.example.data.ProjectRepository
import com.example.engine.Fps
import com.example.engine.QualityControlManager
import com.example.engine.Resolution
import com.example.engine.VideoRenderer
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

@Composable
fun ExportCenterScreen(
    projectRepository: ProjectRepository,
    videoRenderer: VideoRenderer,
    qualityControlManager: QualityControlManager,
    currentProject: ProjectConfig? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var project by remember {
        mutableStateOf(currentProject ?: projectRepository.projects.value.firstOrNull() ?: ProjectConfig())
    }

    val renderProgress by videoRenderer.progress.collectAsState()
    var renderJob by remember { mutableStateOf<Job?>(null) }

    val qcChecks = remember(project) {
        qualityControlManager.runPreExportInspection(project)
    }

    val exportedFiles = remember { mutableStateListOf<File>() }

    fun refreshExportedFiles() {
        val dir = File(context.getExternalFilesDir(null), "exported_videos")
        exportedFiles.clear()
        if (dir.exists()) {
            dir.listFiles { f -> f.extension == "mp4" }?.sortedByDescending { it.lastModified() }?.let {
                exportedFiles.addAll(it)
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshExportedFiles()
    }

    val canExport = qcChecks.all { !it.isCritical || it.isPassed }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "مركز التصدير وفحص الجودة النهائي",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "فحص سلامة النص القرآني والخطوط والأبعاد وترميز MP4 (H.264/AAC)",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
            }
        }

        // Quality Control Checklist
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = IslamicGoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تقرير الفحص الآلي للجودة (Quality Control):",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicCream,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    qcChecks.forEach { check ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (check.isPassed) Icons.Default.CheckCircle else if (check.isCritical) Icons.Default.Error else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (check.isPassed) Color(0xFF81C784) else if (check.isCritical) Color(0xFFCF6679) else IslamicGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = check.title,
                                    color = if (check.isPassed) IslamicCream else Color(0xFFCF6679),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = check.description,
                                    color = IslamicCream.copy(alpha = 0.65f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export Configuration Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "خيارات جودة التصدير والترميز:",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Resolution toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Resolution.values().forEach { res ->
                            val isSelected = project.resolution == res
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IslamicGoldPrimary else SurfaceContainerHighDark)
                                    .clickable { project = project.copy(resolution = res) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = res.label.substringBefore(" "),
                                    color = if (isSelected) IslamicEmeraldDark else IslamicCream,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // FPS toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Fps.values().forEach { fps ->
                            val isSelected = project.fps == fps
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IslamicGoldPrimary else SurfaceContainerHighDark)
                                    .clickable { project = project.copy(fps = fps) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${fps.value} FPS إطار/ثانية",
                                    color = if (isSelected) IslamicEmeraldDark else IslamicCream,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Render Action or Progress Bar
        item {
            if (renderProgress.isRendering) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF143029)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = IslamicGoldPrimary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "جاري ترميز الفيديو (${renderProgress.progressPercent}%)...",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicCream,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { renderProgress.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = IslamicGoldPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = renderProgress.statusText, color = IslamicGoldLight, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { renderJob?.cancel() }
                        ) {
                            Text(text = "إلغاء التصدير", color = Color(0xFFCF6679))
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        renderJob = scope.launch {
                            val f = videoRenderer.renderAndExportVideo(project)
                            if (f != null) {
                                refreshExportedFiles()
                            }
                        }
                    },
                    enabled = canExport,
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = IslamicEmeraldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بدء التصدير الحقيقي لملف MP4",
                        color = IslamicEmeraldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Previously Exported Videos Gallery
        item {
            Text(
                text = "الفيديوهات المنتجة المحفوظة في جهازك (${exportedFiles.size}):",
                style = MaterialTheme.typography.titleMedium,
                color = IslamicCream,
                fontWeight = FontWeight.Bold
            )
        }

        if (exportedFiles.isEmpty()) {
            item {
                Text(
                    text = "لم يتم تصدير أي فيديو بعد في هذه الجلسة.",
                    color = IslamicCream.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        } else {
            items(exportedFiles, key = { it.absolutePath }) { file ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IslamicEmeraldMedium),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = IslamicGoldPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IslamicCream,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${String.format("%.1f", file.length() / (1024.0 * 1024.0))} MB • صيغة MP4 فيديو حقيقي",
                                    color = IslamicGoldLight,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الفيديو القرآني"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicGoldPrimary)
                            }

                            IconButton(onClick = {
                                file.delete()
                                refreshExportedFiles()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFCF6679).copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}
