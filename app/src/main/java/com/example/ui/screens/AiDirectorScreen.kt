package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AiStudioAssistant
import com.example.engine.AspectRatio
import com.example.engine.BackgroundType
import com.example.engine.ProjectConfig
import com.example.engine.TextAnimation
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark

data class DirectorPreset(
    val id: String,
    val name: String,
    val description: String,
    val surahNumber: Int,
    val startAyah: Int,
    val endAyah: Int,
    val fontId: String,
    val fontName: String,
    val backgroundType: BackgroundType,
    val backgroundName: String,
    val animation: TextAnimation,
    val targetRatio: AspectRatio
)

@Composable
fun AiDirectorScreen(
    aiStudioAssistant: AiStudioAssistant,
    onApplyDirectorPreset: (ProjectConfig) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Director Presets, 1: Social Media, 2: Content Planner
    var appliedPresetName by remember { mutableStateOf<String?>(null) }

    val presets = remember {
        listOf(
            DirectorPreset(
                id = "dir_1",
                name = "ثيم سكينة الفجر والمسجد النبوي",
                description = "سورة الفاتحة كاملة بخط أميري نسخ وتدرج زمردي هادئ وشارات ذهبية.",
                surahNumber = 1,
                startAyah = 1,
                endAyah = 7,
                fontId = "bundled_amiri",
                fontName = "خط أميري (نسخ قرآني)",
                backgroundType = BackgroundType.PRESET_MOSQUE,
                backgroundName = "المسجد النبوي الشريف",
                animation = TextAnimation.FADE_IN,
                targetRatio = AspectRatio.PORTRAIT_9_16
            ),
            DirectorPreset(
                id = "dir_2",
                name = "ثيم جلال الليل والنجوم",
                description = "أوائل سورة الملك بخط شهرزاد العثماني وخلفية سماء النجوم الصافية.",
                surahNumber = 67,
                startAyah = 1,
                endAyah = 5,
                fontId = "bundled_scheherazade",
                fontName = "خط شهرزاد الجديد (عثماني)",
                backgroundType = BackgroundType.PRESET_STARS,
                backgroundName = "سماء النجوم والكواكب",
                animation = TextAnimation.GENTLE_SLIDE,
                targetRatio = AspectRatio.PORTRAIT_9_16
            ),
            DirectorPreset(
                id = "dir_3",
                name = "ثيم السكينة والتوحيد",
                description = "سورة الإخلاص والمعوذتين بخط ريم الكوفي الفخم وتدرج داكن.",
                surahNumber = 112,
                startAyah = 1,
                endAyah = 4,
                fontId = "bundled_reem_kufi",
                fontName = "خط ريم الكوفي الزخرفي",
                backgroundType = BackgroundType.EMERALD_GOLD_GRADIENT,
                backgroundName = "تدرج زمردي وذهب ملكي",
                animation = TextAnimation.SMOOTH_REVEAL,
                targetRatio = AspectRatio.SQUARE_1_1
            ),
            DirectorPreset(
                id = "dir_4",
                name = "ثيم عريض ليوتيوب (سورة يس)",
                description = "أوائل سورة يس بخط كايرو العريض وتصميم أفقي 16:9 مناسب لشاشات التلفاز.",
                surahNumber = 36,
                startAyah = 1,
                endAyah = 5,
                fontId = "bundled_cairo",
                fontName = "خط كايرو العصري",
                backgroundType = BackgroundType.PRESET_MOSQUE,
                backgroundName = "أروقة المسجد المبارك",
                animation = TextAnimation.FADE_IN,
                targetRatio = AspectRatio.LANDSCAPE_16_9
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(IslamicGoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IslamicEmeraldDark)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "المخرج الفني الذكي (AI Director)",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اقتراح التنسيقات البصرية والخطوط والوسوم وجدول النشر اليومي",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
            }
        }

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
                text = { Text("باقات المخرج الفني", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("مساعد منصات النشر", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("جدول المحتوى القرآني", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DirectorPresetsTab(
                    presets = presets,
                    appliedPresetName = appliedPresetName,
                    onApply = { preset ->
                        appliedPresetName = preset.name
                        val config = ProjectConfig(
                            title = preset.name,
                            surahNumber = preset.surahNumber,
                            startAyah = preset.startAyah,
                            endAyah = preset.endAyah,
                            aspectRatio = preset.targetRatio,
                            backgroundType = preset.backgroundType,
                            animation = preset.animation
                        )
                        onApplyDirectorPreset(config)
                    }
                )
                1 -> SocialMediaAssistantTab()
                2 -> ContentPlannerTab()
            }
        }
    }
}

@Composable
fun DirectorPresetsTab(
    presets: List<DirectorPreset>,
    appliedPresetName: String?,
    onApply: (DirectorPreset) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        appliedPresetName?.let { name ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF143029)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تم تطبيق \"$name\" بنجاح على مشروعك الحالي!",
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        items(presets, key = { it.id }) { preset ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "• الخط المقترح: ${preset.fontName}", color = IslamicCream, fontSize = 11.5.sp)
                            Text(text = "• المشهد: ${preset.backgroundName}", color = IslamicCream, fontSize = 11.5.sp)
                            Text(text = "• الأبعاد: ${preset.targetRatio.label}", color = IslamicCream, fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = { onApply(preset) },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "تطبيق الباقة", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialMediaAssistantTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "قوالب العناوين والوسوم المجهزة للانتشار:",
                style = MaterialTheme.typography.titleMedium,
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "🎥 عناوين مقترحة ليوتيوب شورتس وتيك توك:", color = IslamicGoldLight, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "1. ﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾ | تلاوة خاشعة تريح البال", color = IslamicCream, fontSize = 13.sp)
                    Text(text = "2. دقيقة مع القرآن الكريم تبدل حزنك سكينة وطمأنينة", color = IslamicCream, fontSize = 13.sp)
                    Text(text = "3. سورة الملك كاملة | منجاة عذاب القبر بصوت الشيخ مشاري العفاسي", color = IslamicCream, fontSize = 13.sp)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "🏷️ حزمة وسوم الهاشتاق المعتمدة:", color = IslamicGoldLight, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "#قرآن #تلاوة_خاشعة #سورة_الملك #quran #راحة_نفسية #قرآن_كريم #shorts #reels #explore",
                        color = IslamicCream,
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ContentPlannerTab() {
    val dailyPlan = listOf(
        Pair("يوم الجمعة", "سورة الكهف كاملة + الصلاة على النبي صلى الله عليه وسلم"),
        Pair("يوم السبت", "آيات السكينة والطمأنينة من سورة يونس وهود"),
        Pair("يوم الأحد", "سورة الواقعة كاملة (جلب الرزق والبركة)"),
        Pair("يوم الإثنين", "سورة يس تلاوة خاشعة هادئة"),
        Pair("يوم الثلاثاء", "سورة الرحمن عروس القرآن"),
        Pair("يوم الأربعاء", "سورة طه وقصص الأنبياء"),
        Pair("يوم الخميس", "سورة الملك المنجية من عذاب القبر")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "جدول النشر القرآني الأسبوعي الموصى به:",
                style = MaterialTheme.typography.titleMedium,
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(dailyPlan) { (day, task) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IslamicEmeraldMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = day, color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = task, color = IslamicCream, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
