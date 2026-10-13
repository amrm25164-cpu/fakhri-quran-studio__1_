package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FontImportResult
import com.example.data.FontManager
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import kotlinx.coroutines.launch

@Composable
fun FontUploadDialog(
    fontManager: FontManager,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<FontImportResult?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isUploading = true
            scope.launch {
                val res = fontManager.importFontsFromUris(uris)
                resultMessage = res
                isUploading = false
                if (res.successCount > 0) {
                    onSuccess()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = IslamicGoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "رفع خطوط جديدة (غير محدود)",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicCream
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "يدعم الاستوديو رفع أي خط مخصص من جهازك بصيغ: TTF, OTF, وأرشيفات ZIP المضغوطة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IslamicCream.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Big Clickable Dropzone Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IslamicEmeraldDark.copy(alpha = 0.8f))
                        .border(1.5.dp, IslamicGoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable(enabled = !isUploading) {
                            filePickerLauncher.launch(arrayOf("*/*"))
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = IslamicGoldPrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "جاري فحص وتثبيت الخطوط بأمان...",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicGoldLight
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = IslamicGoldPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "انقر هنا لاختيار ملفات الخطوط من الهاتف",
                                style = MaterialTheme.typography.titleMedium,
                                color = IslamicCream,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "يدعم ملفات متعددة معاً أو ملف مضغوط ZIP",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicCream.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Supported Formats Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(IslamicEmeraldLight.copy(alpha = 0.3f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = IslamicGoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الصيغ المدعومة: TTF, OTF, ZIP. بالنسبة لصيغ الويب (WOFF/WOFF2)، يجب تحويلها أولاً إلى TTF للعمل على محرك فيديو أندرويد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicCream.copy(alpha = 0.85f),
                        fontSize = 11.5.sp
                    )
                }

                // Show Results / Warnings
                resultMessage?.let { res ->
                    Spacer(modifier = Modifier.height(12.dp))
                    if (res.successCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF81C784))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تم تثبيت ${res.successCount} خط بنجاح وإضافتها لمكتبتك!",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    res.warnings.forEach { warn ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = warn,
                                color = IslamicCream.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
            ) {
                Text(text = "تم", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "إلغاء", color = IslamicCream)
            }
        },
        containerColor = Color(0xFF102622),
        shape = RoundedCornerShape(20.dp)
    )
}
