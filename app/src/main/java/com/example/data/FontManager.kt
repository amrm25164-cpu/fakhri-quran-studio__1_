package com.example.data

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.NotoNaskhFontFamily
import com.example.ui.theme.ReemKufiFontFamily
import com.example.ui.theme.ScheherazadeFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipInputStream

data class FontItem(
    val id: String,
    val displayName: String,
    val fileName: String,
    val format: String, // TTF, OTF, BUNDLED
    val fileSizeFormatted: String,
    val isCustom: Boolean,
    val isFavorite: Boolean = false,
    val filePath: String? = null,
    val resourceId: Int? = null,
    val uploadTimestamp: Long = System.currentTimeMillis()
)

class FontManager(private val context: Context) {

    private val fontsDir = File(context.filesDir, "custom_fonts").apply { mkdirs() }
    private val prefs = context.getSharedPreferences("fakhri_fonts_prefs", Context.MODE_PRIVATE)

    private val _fonts = MutableStateFlow<List<FontItem>>(emptyList())
    val fonts: StateFlow<List<FontItem>> = _fonts.asStateFlow()

    // Cache of loaded Android Typeface
    private val typefaceCache = mutableMapOf<String, Typeface>()

    init {
        loadAllFonts()
    }

    private fun getBundledFonts(): List<FontItem> = listOf(
        FontItem(
            id = "bundled_amiri",
            displayName = "خط أميري (نسخ قرآني)",
            fileName = "amiri.ttf",
            format = "TTF",
            fileSizeFormatted = "مدمج",
            isCustom = false,
            isFavorite = true,
            resourceId = R.font.amiri
        ),
        FontItem(
            id = "bundled_scheherazade",
            displayName = "خط شهرزاد الجديد (عثماني)",
            fileName = "scheherazade_new.ttf",
            format = "TTF",
            fileSizeFormatted = "مدمج",
            isCustom = false,
            isFavorite = true,
            resourceId = R.font.scheherazade_new
        ),
        FontItem(
            id = "bundled_cairo",
            displayName = "خط كايرو (عصري للعناوين)",
            fileName = "cairo.ttf",
            format = "TTF",
            fileSizeFormatted = "مدمج",
            isCustom = false,
            isFavorite = true,
            resourceId = R.font.cairo
        ),
        FontItem(
            id = "bundled_reem_kufi",
            displayName = "خط ريم الكوفي (زخرفي)",
            fileName = "reem_kufi.ttf",
            format = "TTF",
            fileSizeFormatted = "مدمج",
            isCustom = false,
            isFavorite = false,
            resourceId = R.font.reem_kufi
        ),
        FontItem(
            id = "bundled_noto_naskh",
            displayName = "خط نوتو نسخ العربي",
            fileName = "noto_naskh_arabic.ttf",
            format = "TTF",
            fileSizeFormatted = "مدمج",
            isCustom = false,
            isFavorite = false,
            resourceId = R.font.noto_naskh_arabic
        )
    )

    fun loadAllFonts() {
        val bundled = getBundledFonts()
        val customList = mutableListOf<FontItem>()
        val savedJson = prefs.getString("custom_fonts_meta", "[]") ?: "[]"

        try {
            val jsonArray = JSONArray(savedJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val filePath = obj.optString("filePath")
                val file = File(filePath)
                if (file.exists()) {
                    customList.add(
                        FontItem(
                            id = obj.getString("id"),
                            displayName = obj.getString("displayName"),
                            fileName = obj.getString("fileName"),
                            format = obj.getString("format"),
                            fileSizeFormatted = obj.getString("fileSizeFormatted"),
                            isCustom = true,
                            isFavorite = obj.optBoolean("isFavorite", false),
                            filePath = filePath,
                            uploadTimestamp = obj.optLong("uploadTimestamp", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("FontManager", "Failed to parse saved fonts", e)
        }

        // Apply favorite status from prefs for bundled fonts
        val updatedBundled = bundled.map { item ->
            val fav = prefs.getBoolean("fav_${item.id}", item.isFavorite)
            item.copy(isFavorite = fav)
        }

        _fonts.value = updatedBundled + customList
    }

    private fun persistCustomFonts(customFonts: List<FontItem>) {
        val array = JSONArray()
        customFonts.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("displayName", item.displayName)
                put("fileName", item.fileName)
                put("format", item.format)
                put("fileSizeFormatted", item.fileSizeFormatted)
                put("filePath", item.filePath)
                put("isFavorite", item.isFavorite)
                put("uploadTimestamp", item.uploadTimestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_fonts_meta", array.toString()).apply()
    }

    /**
     * Resolves an Android Typeface for rendering on Canvas or Compose.
     */
    fun getTypeface(fontItem: FontItem?): Typeface {
        if (fontItem == null) return Typeface.DEFAULT

        typefaceCache[fontItem.id]?.let { return it }

        val tf = try {
            if (fontItem.isCustom && fontItem.filePath != null) {
                Typeface.createFromFile(File(fontItem.filePath))
            } else {
                when (fontItem.id) {
                    "bundled_amiri" -> context.resources.getFont(R.font.amiri)
                    "bundled_scheherazade" -> context.resources.getFont(R.font.scheherazade_new)
                    "bundled_cairo" -> context.resources.getFont(R.font.cairo)
                    "bundled_reem_kufi" -> context.resources.getFont(R.font.reem_kufi)
                    "bundled_noto_naskh" -> context.resources.getFont(R.font.noto_naskh_arabic)
                    else -> Typeface.DEFAULT
                }
            }
        } catch (e: Exception) {
            Log.w("FontManager", "Failed to load typeface for ${fontItem.displayName}, falling back to DEFAULT", e)
            Typeface.DEFAULT
        }

        typefaceCache[fontItem.id] = tf
        return tf
    }

    /**
     * Resolves a Compose FontFamily for the font item.
     */
    fun getFontFamily(fontItem: FontItem?): FontFamily {
        if (fontItem == null) return FontFamily.Default
        return when (fontItem.id) {
            "bundled_amiri" -> AmiriFontFamily
            "bundled_scheherazade" -> ScheherazadeFontFamily
            "bundled_cairo" -> CairoFontFamily
            "bundled_reem_kufi" -> ReemKufiFontFamily
            "bundled_noto_naskh" -> NotoNaskhFontFamily
            else -> {
                val androidTf = getTypeface(fontItem)
                androidx.compose.ui.text.font.FontFamily(
                    androidx.compose.ui.text.font.Typeface(androidTf)
                )
            }
        }
    }

    /**
     * Uploads and installs fonts from a list of Uris.
     * Supports TTF, OTF, and ZIP archives containing TTF/OTF.
     */
    suspend fun importFontsFromUris(uris: List<Uri>): FontImportResult = withContext(Dispatchers.IO) {
        val imported = mutableListOf<FontItem>()
        val warnings = mutableListOf<String>()

        for (uri in uris) {
            try {
                val rawFileName = getFileNameFromUri(uri)
                val lowerName = rawFileName.lowercase()

                if (lowerName.endsWith(".zip")) {
                    // Extract ZIP safely
                    val zipResult = extractFontsFromZip(uri)
                    imported.addAll(zipResult.importedFonts)
                    warnings.addAll(zipResult.warnings)
                } else if (lowerName.endsWith(".woff") || lowerName.endsWith(".woff2")) {
                    warnings.add("الملف $rawFileName بصيغة ويب (WOFF/WOFF2). يرجى تحويله إلى صيغة TTF أو OTF لاستخدامه على الهاتف ومحرك الفيديو.")
                } else if (lowerName.endsWith(".ttf") || lowerName.endsWith(".otf")) {
                    val item = saveFontFileFromStream(
                        inputStream = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalArgumentException("تعذر فتح الملف"),
                        originalFileName = rawFileName
                    )
                    if (item != null) {
                        imported.add(item)
                    } else {
                        warnings.add("الملف $rawFileName ليس خطاً صالحاً (TTF/OTF) أو مكرر.")
                    }
                } else {
                    warnings.add("صيغة الملف $rawFileName غير مدعومة. الصيغ المدعومة هي: TTF, OTF, وأرشيفات ZIP.")
                }
            } catch (e: Exception) {
                warnings.add("خطأ أثناء استيراد الخط: ${e.localizedMessage}")
            }
        }

        if (imported.isNotEmpty()) {
            val currentCustom = _fonts.value.filter { it.isCustom }.toMutableList()
            currentCustom.addAll(imported)
            persistCustomFonts(currentCustom)
            loadAllFonts()
        }

        FontImportResult(
            successCount = imported.size,
            warnings = warnings,
            importedFonts = imported
        )
    }

    private fun extractFontsFromZip(zipUri: Uri): FontImportResult {
        val imported = mutableListOf<FontItem>()
        val warnings = mutableListOf<String>()

        context.contentResolver.openInputStream(zipUri)?.use { input ->
            ZipInputStream(input).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    // Prevent zip slip path traversal
                    if (!entry.isDirectory && !entryName.contains("..")) {
                        val simpleName = File(entryName).name
                        val lower = simpleName.lowercase()
                        if (lower.endsWith(".ttf") || lower.endsWith(".otf")) {
                            val tempFile = File.createTempFile("zip_font", ".tmp", context.cacheDir)
                            FileOutputStream(tempFile).use { out ->
                                zis.copyTo(out)
                            }
                            FileInputStream(tempFile).use { fis ->
                                val item = saveFontFileFromStream(fis, simpleName)
                                if (item != null) {
                                    imported.add(item)
                                }
                            }
                            tempFile.delete()
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        if (imported.isEmpty()) {
            warnings.add("لم يتم العثور على ملفات خطوط TTF أو OTF صالحة داخل ملف ZIP.")
        }

        return FontImportResult(imported.size, warnings, imported)
    }

    private fun saveFontFileFromStream(inputStream: InputStream, originalFileName: String): FontItem? {
        val buffer = inputStream.readBytes()
        if (buffer.size < 12) return null

        // Validate TTF/OTF Magic header:
        // TTF: 0x00010000 or 0x74727565 ('true') or 0x74797031 ('typ1')
        // OTF: 0x4F54544F ('OTTO')
        val b0 = buffer[0].toInt() and 0xFF
        val b1 = buffer[1].toInt() and 0xFF
        val b2 = buffer[2].toInt() and 0xFF
        val b3 = buffer[3].toInt() and 0xFF
        val magic = (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3

        val isTtf = magic == 0x00010000 || magic == 0x74727565 || magic == 0x74797031
        val isOtf = magic == 0x4F54544F

        if (!isTtf && !isOtf) {
            Log.w("FontManager", "Unsupported font magic bytes: 0x${Integer.toHexString(magic)}")
            return null
        }

        val format = if (isOtf) "OTF" else "TTF"

        // Check duplicate by MD5 hash
        val md5 = MessageDigest.getInstance("MD5").digest(buffer).joinToString("") { "%02x".format(it) }
        val existing = _fonts.value.find { it.id.endsWith(md5) }
        if (existing != null) {
            Log.i("FontManager", "Font already exists: $originalFileName")
            return null
        }

        val fontId = "custom_${System.currentTimeMillis()}_$md5"
        val cleanName = originalFileName.substringBeforeLast(".")
        val ext = if (isOtf) "otf" else "ttf"
        val targetFile = File(fontsDir, "$fontId.$ext")

        FileOutputStream(targetFile).use { it.write(buffer) }

        // Test if Android Typeface can actually load it
        try {
            val testTf = Typeface.createFromFile(targetFile)
            if (testTf == null) throw IllegalStateException("Typeface null")
        } catch (e: Exception) {
            targetFile.delete()
            Log.e("FontManager", "Android Typeface rejected font file", e)
            return null
        }

        val sizeFormatted = formatFileSize(buffer.size.toLong())

        return FontItem(
            id = fontId,
            displayName = cleanName.replace("_", " ").replace("-", " "),
            fileName = originalFileName,
            format = format,
            fileSizeFormatted = sizeFormatted,
            isCustom = true,
            isFavorite = false,
            filePath = targetFile.absolutePath,
            uploadTimestamp = System.currentTimeMillis()
        )
    }

    fun toggleFavorite(fontId: String) {
        val current = _fonts.value.map {
            if (it.id == fontId) {
                val newFav = !it.isFavorite
                if (!it.isCustom) {
                    prefs.edit().putBoolean("fav_$fontId", newFav).apply()
                }
                it.copy(isFavorite = newFav)
            } else it
        }
        _fonts.value = current
        val customOnly = current.filter { it.isCustom }
        persistCustomFonts(customOnly)
    }

    fun renameFont(fontId: String, newName: String) {
        val current = _fonts.value.map {
            if (it.id == fontId) it.copy(displayName = newName) else it
        }
        _fonts.value = current
        val customOnly = current.filter { it.isCustom }
        persistCustomFonts(customOnly)
    }

    fun deleteFont(fontId: String) {
        val font = _fonts.value.find { it.id == fontId }
        if (font != null && font.isCustom && font.filePath != null) {
            File(font.filePath).delete()
            typefaceCache.remove(fontId)
            val updated = _fonts.value.filter { it.id != fontId }
            _fonts.value = updated
            persistCustomFonts(updated.filter { it.isCustom })
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = "font_${System.currentTimeMillis()}.ttf"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val displayName = cursor.getString(nameIndex)
                if (!displayName.isNullOrBlank()) {
                    name = displayName
                }
            }
        }
        return name
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
        }
    }
}

data class FontImportResult(
    val successCount: Int,
    val warnings: List<String>,
    val importedFonts: List<FontItem>
)
