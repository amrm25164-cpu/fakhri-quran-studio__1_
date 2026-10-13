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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Ayah
import com.example.data.FontManager
import com.example.data.QuranRepository
import com.example.data.Surah
import com.example.ui.components.AyahBadge
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranLibraryScreen(
    quranRepository: QuranRepository,
    fontManager: FontManager,
    onCreateVideoFromSurah: (Surah) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Surahs, 1: Bookmarks

    val bookmarks by quranRepository.bookmarks.collectAsState()
    val searchResults = remember(searchQuery) {
        quranRepository.searchSurahs(searchQuery)
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
                    text = "مكتبة المصحف الشريف الموثقة",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicCream,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "114 سورة كاملة برسم المصحف العثماني وأرقام الآيات",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight
                )
            }
        }

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث برقم السورة أو اسمها (مثال: الفاتحة، 67)...", color = IslamicCream.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
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
                text = { Text("فهرس السور (114)", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("الآيات المحفوظة (${bookmarks.size})", fontWeight = FontWeight.Bold) }
            )
        }

        // Detail View or List
        if (selectedSurah != null) {
            SurahReaderView(
                surah = selectedSurah!!,
                quranRepository = quranRepository,
                fontManager = fontManager,
                onBackToList = { selectedSurah = null },
                onCreateVideo = { onCreateVideoFromSurah(selectedSurah!!) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults, key = { it.number }) { surah ->
                    SurahListItem(
                        surah = surah,
                        onClick = {
                            selectedSurah = surah
                            quranRepository.markSurahRecent(surah.number)
                        },
                        onCreateVideo = { onCreateVideoFromSurah(surah) }
                    )
                }
            }
        }
    }
}

@Composable
fun SurahListItem(
    surah: Surah,
    onClick: () -> Unit,
    onCreateVideo: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AyahBadge(ayahNumber = surah.number.toString())
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "سورة ${surah.arabicName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${surah.englishName} • ${surah.revelationType} • ${surah.versesCount} آيات",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream.copy(alpha = 0.5f),
                        fontSize = 11.5.sp
                    )
                }
            }

            IconButton(onClick = onCreateVideo) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "إنشاء فيديو",
                    tint = IslamicGoldPrimary
                )
            }
        }
    }
}

@Composable
fun SurahReaderView(
    surah: Surah,
    quranRepository: QuranRepository,
    fontManager: FontManager,
    onBackToList: () -> Unit,
    onCreateVideo: () -> Unit
) {
    val verses = remember(surah.number) {
        quranRepository.getVersesForSurah(surah.number)
    }
    val quranFont = fontManager.getFontFamily(fontManager.fonts.value.find { it.id == "bundled_amiri" })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackToList,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmeraldMedium)
            ) {
                Text(text = "← عودة للفهرس", color = IslamicCream)
            }

            Button(
                onClick = onCreateVideo,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
            ) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = IslamicEmeraldDark)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "إنشاء فيديو لهذه السورة", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(verses, key = { it.numberInSurah }) { ayah ->
                val isBookmarked = quranRepository.isBookmarked(surah.number, ayah.numberInSurah)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AyahBadge(ayahNumber = ayah.numberInSurah.toString())
                            IconButton(onClick = { quranRepository.toggleBookmark(surah.number, ayah.numberInSurah) }) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "حفظ الآية",
                                    tint = if (isBookmarked) IslamicGoldPrimary else IslamicCream.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = ayah.arabicText,
                            fontFamily = quranFont,
                            fontSize = 24.sp,
                            color = IslamicCream,
                            textAlign = TextAlign.Right,
                            lineHeight = 42.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = ayah.translation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IslamicGoldLight,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "التفسير الميسر: ${ayah.tafsir}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicCream.copy(alpha = 0.65f),
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
