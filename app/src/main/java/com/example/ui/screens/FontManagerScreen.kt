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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.FontItem
import com.example.data.FontManager
import com.example.ui.components.FontUploadDialog
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicEmeraldMedium
import com.example.ui.theme.IslamicGoldDark
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark

@Composable
fun FontManagerScreen(
    fontManager: FontManager,
    onBack: () -> Unit = {}
) {
    val fonts by fontManager.fonts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var showOnlyCustom by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }

    var fontToRename by remember { mutableStateOf<FontItem?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var fontToDelete by remember { mutableStateOf<FontItem?>(null) }
    var previewSizeSp by remember { mutableFloatStateOf(24f) }

    val filteredFonts = fonts.filter { font ->
        val matchesQuery = searchQuery.isBlank() ||
                font.displayName.contains(searchQuery, ignoreCase = true) ||
                font.fileName.contains(searchQuery, ignoreCase = true)
        val matchesFav = !showOnlyFavorites || font.isFavorite
        val matchesCustom = !showOnlyCustom || font.isCustom
        matchesQuery && matchesFav && matchesCustom
    }

    if (showUploadDialog) {
        FontUploadDialog(
            fontManager = fontManager,
            onDismiss = { showUploadDialog = false },
            onSuccess = { showUploadDialog = false }
        )
    }

    // Rename Dialog
    fontToRename?.let { font ->
        AlertDialog(
            onDismissRequest = { fontToRename = null },
            title = { Text(text = "تعديل اسم الخط", color = IslamicCream) },
            text = {
                Column {
                    Text(
                        text = "اسم الملف الأصلي: ${font.fileName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("اسم الخط المعروض في الاستوديو") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            fontManager.renameFont(font.id, renameInput.trim())
                        }
                        fontToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                ) {
                    Text(text = "حفظ", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { fontToRename = null }) {
                    Text(text = "إلغاء", color = IslamicCream)
                }
            },
            containerColor = Color(0xFF142B27)
        )
    }

    // Delete Confirmation Dialog
    fontToDelete?.let { font ->
        AlertDialog(
            onDismissRequest = { fontToDelete = null },
            title = { Text(text = "حذف الخط المخصص", color = IslamicCream) },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف الخط \"${font.displayName}\" (${font.fileName}) من جهازك نهائياً؟",
                    color = IslamicCream.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        fontManager.deleteFont(font.id)
                        fontToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679))
                ) {
                    Text(text = "تأكيد الحذف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { fontToDelete = null }) {
                    Text(text = "إلغاء", color = IslamicCream)
                }
            },
            containerColor = Color(0xFF1B2624)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Upload Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مدير الخطوط المخصصة",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicCream,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "دعم كامل وغير محدود لأي خط TTF / OTF / ZIP",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight
                    )
                }

                Button(
                    onClick = { showUploadDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = IslamicEmeraldDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "رفع خط جديد", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث عن أي خط بالاسم أو الملف...", color = IslamicCream.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Filter Chips & Size Slider
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !showOnlyFavorites && !showOnlyCustom,
                    onClick = {
                        showOnlyFavorites = false
                        showOnlyCustom = false
                    },
                    label = { Text("الكل (${fonts.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = IslamicEmeraldDark
                    )
                )

                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = { showOnlyFavorites = !showOnlyFavorites },
                    label = { Text("المفضلة (${fonts.count { it.isFavorite }})") },
                    leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )

                FilterChip(
                    selected = showOnlyCustom,
                    onClick = { showOnlyCustom = !showOnlyCustom },
                    label = { Text("المرفوعة مسبقاً (${fonts.count { it.isCustom }})") }
                )
            }
        }

        // Preview Size Slider
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerHighDark)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "حجم المعاينة: ${previewSizeSp.toInt()} sp",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicCream.copy(alpha = 0.8f)
                )
                Slider(
                    value = previewSizeSp,
                    onValueChange = { previewSizeSp = it },
                    valueRange = 16f..40f,
                    modifier = Modifier.width(180.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = IslamicGoldPrimary,
                        activeTrackColor = IslamicGoldPrimary
                    )
                )
            }
        }

        // Font Cards
        items(filteredFonts, key = { it.id }) { font ->
            FontPreviewCard(
                fontItem = font,
                fontManager = fontManager,
                previewSizeSp = previewSizeSp,
                onToggleFavorite = { fontManager.toggleFavorite(font.id) },
                onRename = {
                    fontToRename = font
                    renameInput = font.displayName
                },
                onDelete = { fontToDelete = font }
            )
        }
    }
}

@Composable
fun FontPreviewCard(
    fontItem: FontItem,
    fontManager: FontManager,
    previewSizeSp: Float,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val fontFamily = fontManager.getFontFamily(fontItem)

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslamicEmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Info & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (fontItem.isCustom) IslamicGoldPrimary.copy(alpha = 0.2f) else IslamicEmeraldMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FontDownload,
                            contentDescription = null,
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = fontItem.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicCream,
                                fontWeight = FontWeight.Bold
                            )
                            if (fontItem.isCustom) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(IslamicGoldPrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "مخصص",
                                        color = IslamicEmeraldDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${fontItem.fileName} • ${fontItem.format} • ${fontItem.fileSizeFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicCream.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (fontItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (fontItem.isFavorite) Color(0xFFE57373) else IslamicCream.copy(alpha = 0.6f)
                        )
                    }

                    if (fontItem.isCustom) {
                        IconButton(onClick = onRename) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الاسم",
                                tint = IslamicCream.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الخط",
                                tint = Color(0xFFE57373).copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real Quranic Text Preview rendered using the exact custom font!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF091715))
                    .border(1.dp, IslamicGoldDark.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontFamily = fontFamily,
                        fontSize = previewSizeSp.sp,
                        color = IslamicCream,
                        textAlign = TextAlign.Center,
                        lineHeight = (previewSizeSp * 1.5f).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "﴿١﴾ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿٢﴾ الرَّحْمَٰنِ الرَّحِيمِ ﴿٣﴾",
                        fontFamily = fontFamily,
                        fontSize = (previewSizeSp * 0.85f).sp,
                        color = IslamicGoldLight,
                        textAlign = TextAlign.Center,
                        lineHeight = (previewSizeSp * 1.4f).sp
                    )
                }
            }
        }
    }
}
