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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.FontManager
import com.example.data.ProjectRepository
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import java.io.File

@Composable
fun SettingsScreen(
    fontManager: FontManager,
    projectRepository: ProjectRepository
) {
    val context = LocalContext.current
    val fonts by fontManager.fonts.collectAsState()
    val projects by projectRepository.projects.collectAsState()
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun cleanTempFiles() {
        val cache = context.cacheDir
        cache.deleteRecursively()
        cache.mkdirs()
        statusMessage = "تم تنظيف الملفات المؤقتة وذاكرة التخزين بنجاح!"
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
            Column {
                Text(
                    text = "الإعدادات والنسخ الاحتياطي والدليل",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إدارة التخزين المحلي، سلامة الخطوط، والتوثيق الشرعي والتقني",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
            }
        }

        statusMessage?.let { msg ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF143029)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Storage & Cache Overview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = IslamicGoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "إدارة التخزين والمشاريع المحلية:", color = IslamicCream, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "• عدد الخطوط المثبتة: ${fonts.size} (${fonts.count { it.isCustom }} مخصصة)", color = IslamicCream, fontSize = 12.5.sp)
                    Text(text = "• عدد المشاريع المحفوظة: ${projects.size} مشاريع", color = IslamicCream, fontSize = 12.5.sp)
                    Text(text = "• نوع التخزين: محلي بالكامل على الهاتف لضمان الخصوصية والسرعة", color = IslamicCream, fontSize = 12.5.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { cleanTempFiles() },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldMedium),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = IslamicCream, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تنظيف الملفات المؤقتة والكاش", color = IslamicCream, fontSize = 12.sp)
                    }
                }
            }
        }

        // Guidelines & Font Integrity Info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = IslamicGoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "ميثاق الأمانة والتوثيق القرآني:", color = IslamicCream, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1. يتم عرض النص القرآني برسم المصحف العثماني المعتمد بدون أي حذف أو إضافة أو توليد آلي.\n" +
                               "2. الخطوط المخصصة المرفوعة من قبل المستخدم تخضع لفحص التوافق وتُستخدم كما هي دون تعديل للرسم.\n" +
                               "3. التسجيلات الصوتية تعود لكبار القراء المعتمدين والمصاحف المرتلة الموثقة.\n" +
                               "4. ميزة المخرج الذكي تقتصر على التنسيق البصري واقتراح العناوين والوسوم ولا تتدخل في المتن القرآني نهائياً.",
                        color = IslamicCream.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // App Information
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "استوديو فخري للقرآن ألترا",
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Fakhri Quran Studio ULTRA • الإصدار 2.0 الاحترافي",
                        color = IslamicCream.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مبني بأحدث تقنيات أندرويد وJetpack Compose مع محرك تشفير فيديو H.264 متقدم",
                        color = IslamicCream.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
