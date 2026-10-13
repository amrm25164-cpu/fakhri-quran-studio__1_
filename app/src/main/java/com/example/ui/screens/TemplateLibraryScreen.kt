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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class StudioTemplate(
    val id: String,
    val name: String,
    val category: String,
    val surahNumber: Int,
    val startAyah: Int,
    val endAyah: Int,
    val reciterId: String,
    val aspectRatio: AspectRatio,
    val backgroundType: BackgroundType,
    val fontId: String,
    val description: String
)

@Composable
fun TemplateLibraryScreen(
    onUseTemplate: (ProjectConfig) -> Unit = {}
) {
    val templates = remember {
        listOf(
            StudioTemplate(
                id = "tpl_reels_gold",
                name = "قالب ريلز ذهبي فخم (سورة الفاتحة)",
                category = "ريلز وتيك توك (9:16)",
                surahNumber = 1,
                startAyah = 1,
                endAyah = 7,
                reciterId = "afasy",
                aspectRatio = AspectRatio.PORTRAIT_9_16,
                backgroundType = BackgroundType.PRESET_MOSQUE,
                fontId = "bundled_amiri",
                description = "إطار زخرفي ملكي مع خط أميري عثماني وتدرج إضاءة ناعم."
            ),
            StudioTemplate(
                id = "tpl_youtube_mulk",
                name = "قالب يوتيوب عريض خاشع (سورة الملك)",
                category = "يوتيوب عريض (16:9)",
                surahNumber = 67,
                startAyah = 1,
                endAyah = 5,
                reciterId = "sudais",
                aspectRatio = AspectRatio.LANDSCAPE_16_9,
                backgroundType = BackgroundType.PRESET_STARS,
                fontId = "bundled_scheherazade",
                description = "أبعاد 16:9 مع ترجمة إنجليزية بارزة وسماء النجوم."
            ),
            StudioTemplate(
                id = "tpl_square_ikhlas",
                name = "قالب إنستغرام مربع (سورة الإخلاص)",
                category = "منشورات مربعة (1:1)",
                surahNumber = 112,
                startAyah = 1,
                endAyah = 4,
                reciterId = "minshawi_murattal",
                aspectRatio = AspectRatio.SQUARE_1_1,
                backgroundType = BackgroundType.EMERALD_GOLD_GRADIENT,
                fontId = "bundled_reem_kufi",
                description = "تصميم كوفي مربع مناسب لخلاصات إنستغرام وتويتر."
            ),
            StudioTemplate(
                id = "tpl_duaa_falaq",
                name = "قالب المعوذات والتحصين اليومي",
                category = "أذكار وتحصين (9:16)",
                surahNumber = 113,
                startAyah = 1,
                endAyah = 5,
                reciterId = "fares_abbad",
                aspectRatio = AspectRatio.PORTRAIT_9_16,
                backgroundType = BackgroundType.DARK_GRADIENT,
                fontId = "bundled_cairo",
                description = "أسلوب داكن نقي يركز على الكلمات والخشوع الكامل."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "مكتبة القوالب الجاهزة (Template Library)",
                style = MaterialTheme.typography.titleLarge,
                color = IslamicCream,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "قوالب احترافية منسقة مسبقاً بالأبعاد والخطوط والزخارف المناسبة",
                style = MaterialTheme.typography.bodySmall,
                color = IslamicGoldLight
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(templates, key = { it.id }) { tpl ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                        .background(IslamicEmeraldMedium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = tpl.name, color = IslamicCream, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(text = tpl.category, color = IslamicGoldLight, fontSize = 11.5.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = tpl.description,
                            color = IslamicCream.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val config = ProjectConfig(
                                    title = tpl.name,
                                    surahNumber = tpl.surahNumber,
                                    startAyah = tpl.startAyah,
                                    endAyah = tpl.endAyah,
                                    reciterId = tpl.reciterId,
                                    aspectRatio = tpl.aspectRatio,
                                    backgroundType = tpl.backgroundType
                                )
                                onUseTemplate(config)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "استخدام هذا القالب لإنشاء فيديو جديد", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
