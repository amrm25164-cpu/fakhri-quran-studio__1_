package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatchJob
import com.example.data.BatchStatus
import com.example.data.ProjectRepository
import com.example.data.QuranRepository
import com.example.engine.ProjectConfig
import com.example.engine.VideoRenderer
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BatchQueueScreen(
    projectRepository: ProjectRepository,
    quranRepository: QuranRepository,
    videoRenderer: VideoRenderer
) {
    val batchQueue by projectRepository.batchQueue.collectAsState()
    val scope = rememberCoroutineScope()
    var isProcessingBatch by remember { mutableStateOf(false) }

    fun processNextQueueItem() {
        scope.launch {
            isProcessingBatch = true
            for (job in batchQueue) {
                if (job.status == BatchStatus.QUEUED) {
                    projectRepository.updateBatchJobStatus(job.id, BatchStatus.RENDERING, progress = 25)
                    delay(800) // sequential processing simulation
                    projectRepository.updateBatchJobStatus(job.id, BatchStatus.RENDERING, progress = 75)
                    delay(800)
                    projectRepository.updateBatchJobStatus(
                        job.id,
                        BatchStatus.COMPLETED,
                        progress = 100,
                        outputPath = "/storage/emulated/0/Movies/${job.id}.mp4"
                    )
                }
            }
            isProcessingBatch = false
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
                        text = "طابور الإنتاج والتصدير التلقائي",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تصدير دفعات متعددة من السور والآيات تلقائياً وبأعلى كفاءة",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight
                    )
                }

                Button(
                    onClick = { processNextQueueItem() },
                    enabled = !isProcessingBatch && batchQueue.any { it.status == BatchStatus.QUEUED },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessingBatch) {
                        CircularProgressIndicator(color = IslamicEmeraldDark, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = IslamicEmeraldDark)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "بدء التصدير المتتالي", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(batchQueue, key = { it.id }) { job ->
            BatchJobCard(
                job = job,
                onDelete = { projectRepository.removeBatchJob(job.id) }
            )
        }
    }
}

@Composable
fun BatchJobCard(
    job: BatchJob,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when (job.status) {
                                    BatchStatus.COMPLETED -> Color(0xFF81C784).copy(alpha = 0.2f)
                                    BatchStatus.RENDERING -> IslamicGoldPrimary.copy(alpha = 0.2f)
                                    else -> IslamicEmeraldMedium
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (job.status) {
                                BatchStatus.COMPLETED -> Icons.Default.CheckCircle
                                BatchStatus.RENDERING -> Icons.Default.PlayArrow
                                else -> Icons.Default.HourglassEmpty
                            },
                            contentDescription = null,
                            tint = when (job.status) {
                                BatchStatus.COMPLETED -> Color(0xFF81C784)
                                BatchStatus.RENDERING -> IslamicGoldPrimary
                                else -> IslamicCream.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = job.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicCream,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الحالة: ${job.status.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (job.status) {
                                BatchStatus.COMPLETED -> Color(0xFF81C784)
                                BatchStatus.RENDERING -> IslamicGoldLight
                                else -> IslamicCream.copy(alpha = 0.6f)
                            },
                            fontSize = 11.5.sp
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف المهمة",
                        tint = IslamicCream.copy(alpha = 0.5f)
                    )
                }
            }

            if (job.status == BatchStatus.RENDERING) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { job.progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = IslamicGoldPrimary
                )
            }
        }
    }
}
