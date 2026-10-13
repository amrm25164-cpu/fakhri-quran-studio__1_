package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FontManager
import com.example.data.ProjectRepository
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import com.example.engine.ProjectConfig
import com.example.ui.components.IslamicBannerHeader
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark

@Composable
fun DashboardScreen(
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
    fontManager: FontManager,
    projectRepository: ProjectRepository,
    onNavigate: (String) -> Unit,
    onOpenProject: (ProjectConfig) -> Unit,
    onNewProject: () -> Unit
) {
    val fonts by fontManager.fonts.collectAsState()
    val projects by projectRepository.projects.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Banner
        item {
            HeroHeader(
                fontCount = fonts.size,
                projectCount = projects.size,
                onNewProject = onNewProject
            )
        }

        // Quick Feature Hub Grid
        item {
            Text(
                text = "الأقسام والاستوديوهات المتخصصة",
                style = MaterialTheme.typography.titleMedium,
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            StudioHubRow(onNavigate = onNavigate)
        }

        // Quick Stats Strip
        item {
            StatsStrip(
                fontCount = fonts.size,
                customFontCount = fonts.count { it.isCustom },
                reciterCount = reciterRepository.reciters.size,
                projectCount = projects.size
            )
        }

        // Recent Projects Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مشاريعك المحفوظة",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${projects.size} مشاريع",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
            }
        }

        if (projects.isEmpty()) {
            item {
                EmptyProjectsCard(onNewProject = onNewProject)
            }
        } else {
            items(projects, key = { it.id }) { project ->
                ProjectItemCard(
                    project = project,
                    quranRepository = quranRepository,
                    reciterRepository = reciterRepository,
                    onClick = { onOpenProject(project) },
                    onDuplicate = { projectRepository.duplicateProject(project) },
                    onDelete = { projectRepository.deleteProject(project.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HeroHeader(
    fontCount: Int,
    projectCount: Int,
    onNewProject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF143029),
                        Color(0xFF0C201C),
                        Color(0xFF071412)
                    )
                )
            )
            .border(1.5.dp, IslamicGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(IslamicGoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = IslamicEmeraldDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "استوديو فخري ألترا",
                            style = MaterialTheme.typography.titleLarge,
                            color = IslamicCream,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fakhri Quran Studio ULTRA",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicGoldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "المنصة المتكاملة لإنتاج فيديوهات القرآن الكريم الاحترافية بدقة 1080p مع حرية تامة لرفع أي خط مخصص، ومزامنة الصوت والترجمات.",
                style = MaterialTheme.typography.bodyMedium,
                color = IslamicCream.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNewProject,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = IslamicEmeraldDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بدء إنشاء فيديو قرآني جديد الآن",
                    color = IslamicEmeraldDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun StudioHubRow(onNavigate: (String) -> Unit) {
    val items = listOf(
        Triple("creator", "صانع الفيديو", Icons.Default.Movie),
        Triple("preview", "استوديو المعاينة", Icons.Default.PlayCircle),
        Triple("director", "المخرج الذكي", Icons.Default.AutoAwesome),
        Triple("fonts", "مدير الخطوط", Icons.Default.FontDownload),
        Triple("export", "مركز التصدير", Icons.Default.Download),
        Triple("timeline", "محرر الطبقات", Icons.Default.Layers),
        Triple("quran", "المصحف الشريف", Icons.Default.MenuBook),
        Triple("reciters", "كبار القراء", Icons.Default.GraphicEq),
        Triple("media", "الوسائط والخلفيات", Icons.Default.Image),
        Triple("templates", "مكتبة القوالب", Icons.Default.Movie),
        Triple("thumbnail", "مصمم الغلاف", Icons.Default.PhotoCamera),
        Triple("batch", "طابور الدفعات", Icons.Default.Queue),
        Triple("ai", "المساعد التقني", Icons.Default.AutoAwesome),
        Triple("settings", "الإعدادات والدليل", Icons.Default.Settings)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { (route, label, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .width(105.dp)
                    .clickable { onNavigate(route) }
                    .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(IslamicEmeraldMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StatsStrip(
    fontCount: Int,
    customFontCount: Int,
    reciterCount: Int,
    projectCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerHighDark)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatItem(number = "$fontCount", label = "خط متاح", sub = "$customFontCount مخصص")
        StatItem(number = "114", label = "سورة موثقة", sub = "عثماني")
        StatItem(number = "$reciterCount", label = "قارئ معتمد", sub = "صوتيات")
        StatItem(number = "$projectCount", label = "مشاريع", sub = "محفوظة")
    }
}

@Composable
fun StatItem(number: String, label: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, color = IslamicGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = IslamicCream, fontSize = 12.sp)
        Text(text = sub, color = IslamicCream.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectConfig,
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val surah = quranRepository.allSurahs.find { it.number == project.surahNumber }
    val reciter = reciterRepository.reciters.find { it.id == project.reciterId }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F2622)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = IslamicGoldPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${surah?.arabicName ?: "سورة"} (آية ${project.startAyah} - ${project.endAyah}) • ${reciter?.arabicName ?: "القارئ"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.aspectRatio.label} • ${project.resolution.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicCream.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun EmptyProjectsCard(onNewProject: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "لا توجد مشاريع سابقة بعد",
                style = MaterialTheme.typography.titleMedium,
                color = IslamicCream
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ابدأ بإنشاء أول فيديو قرآني عالي الجودة الآن",
                style = MaterialTheme.typography.bodySmall,
                color = IslamicCream.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNewProject,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
            ) {
                Text(text = "إنشاء فيديو جديد", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}
