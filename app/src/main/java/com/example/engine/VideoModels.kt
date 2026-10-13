package com.example.engine

import android.graphics.Color as AndroidColor

enum class AspectRatio(val label: String, val widthRatio: Int, val heightRatio: Int) {
    PORTRAIT_9_16("9:16 (ريلز / تيك توك / شورتس)", 9, 16),
    LANDSCAPE_16_9("16:9 (يوتيوب عريض)", 16, 9),
    SQUARE_1_1("1:1 (مربع إنستغرام)", 1, 1),
    PORTRAIT_4_5("4:5 (منشورات عمودية)", 4, 5);

    fun getDimensions(resolution: Resolution): Pair<Int, Int> {
        val base = if (resolution == Resolution.RES_1080P) 1080 else 720
        return when (this) {
            PORTRAIT_9_16 -> if (resolution == Resolution.RES_1080P) Pair(1080, 1920) else Pair(720, 1280)
            LANDSCAPE_16_9 -> if (resolution == Resolution.RES_1080P) Pair(1920, 1080) else Pair(1280, 720)
            SQUARE_1_1 -> Pair(base, base)
            PORTRAIT_4_5 -> if (resolution == Resolution.RES_1080P) Pair(1080, 1350) else Pair(720, 900)
        }
    }
}

enum class Resolution(val label: String) {
    RES_720P("720p HD (سريع ومناسب للهواتف)"),
    RES_1080P("1080p FHD (دقة فائقة)")
}

enum class Fps(val value: Int) {
    FPS_30(30),
    FPS_60(60)
}

enum class TextAnimation(val label: String) {
    NONE("ثابت"),
    FADE_IN("تلاشي تدريجي (Fade In)"),
    GENTLE_SLIDE("انزلاق هادئ (Gentle Slide)"),
    SMOOTH_REVEAL("ظهور انسيابي (Smooth Reveal)")
}

enum class BackgroundType {
    PRESET_MOSQUE,
    PRESET_STARS,
    DARK_GRADIENT,
    EMERALD_GOLD_GRADIENT,
    PRESET_KAABA,
    PRESET_MADINAH,
    PRESET_RAIN,
    CUSTOM_IMAGE
}

data class VerseTimestamp(
    val verseNumber: Int,
    val startMs: Int,
    val endMs: Int
)

data class LayerProperties(
    val id: String,
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f,
    val offsetYRatio: Float = 0.5f, // Normalized 0.0 (top) to 1.0 (bottom)
    val offsetXRatio: Float = 0.5f, // Normalized 0.0 (left) to 1.0 (right)
    val scale: Float = 1.0f,
    val widthRatio: Float = 0.88f,
    val heightRatio: Float = 0.35f
)

data class TypographyStyle(
    val fontId: String = "bundled_amiri",
    val fontSizeSp: Float = 28f,
    val minFontSizeSp: Float = 12f,
    val maxFontSizeSp: Float = 72f,
    val textColorArgb: Int = AndroidColor.WHITE,
    val textShadowColorArgb: Int = AndroidColor.argb(180, 0, 0, 0),
    val textShadowRadius: Float = 8f,
    val strokeColorArgb: Int = AndroidColor.TRANSPARENT,
    val strokeWidth: Float = 0f,
    val lineSpacingMultiplier: Float = 1.3f,
    val isBold: Boolean = false,
    val maxWidthRatio: Float = 0.88f,
    val maxHeightRatio: Float = 0.55f,
    val isFitToBox: Boolean = false,
    val maxLines: Int = 0, // 0 = no limit
    val paddingHorizontalDp: Float = 16f,
    val paddingVerticalDp: Float = 8f
)

data class AspectRatioTypographyConfig(
    val quranTextStyle: TypographyStyle,
    val translationTextStyle: TypographyStyle,
    val surahHeaderStyle: TypographyStyle,
    val ayahBadgeStyle: TypographyStyle = TypographyStyle(
        fontId = "bundled_cairo",
        fontSizeSp = 22f,
        textColorArgb = AndroidColor.parseColor("#D4AF37")
    ),
    val reciterBadgeStyle: TypographyStyle = TypographyStyle(
        fontId = "bundled_cairo",
        fontSizeSp = 13f,
        textColorArgb = AndroidColor.parseColor("#E0D8C3")
    ),
    val layers: List<LayerProperties>
)

data class TypographyPreset(
    val id: String,
    val name: String,
    val description: String,
    val quranTextStyle: TypographyStyle,
    val translationTextStyle: TypographyStyle,
    val surahHeaderStyle: TypographyStyle,
    val isCustom: Boolean = false
)

data class TextFitStatus(
    val fitsInsideCanvas: Boolean = true,
    val isOverlapping: Boolean = false,
    val effectiveFontSizeSp: Float = 28f,
    val scaleFactor: Float = 1.0f,
    val warningMessage: String? = null,
    val exceedsTop: Boolean = false,
    val exceedsBottom: Boolean = false,
    val exceedsSides: Boolean = false
)

data class QualityCheckItem(
    val id: String,
    val title: String,
    val description: String,
    val isPassed: Boolean,
    val isCritical: Boolean = false
)

data class ProjectConfig(
    val id: String = "proj_${System.currentTimeMillis()}",
    val title: String = "مشروع فيديو قرآني جديد",
    val surahNumber: Int = 1,
    val startAyah: Int = 1,
    val endAyah: Int = 7,
    val reciterId: String = "afasy",
    val customAudioPath: String? = null,
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT_9_16,
    val resolution: Resolution = Resolution.RES_720P,
    val fps: Fps = Fps.FPS_30,
    val backgroundType: BackgroundType = BackgroundType.PRESET_MOSQUE,
    val customBackgroundImagePath: String? = null,
    val backgroundBrightness: Float = 0.75f,
    val backgroundBlur: Float = 0f,
    val animation: TextAnimation = TextAnimation.FADE_IN,
    val showTranslation: Boolean = true,
    val showTafsir: Boolean = false,
    val showIslamicFrame: Boolean = true,
    val showSurahHeader: Boolean = true,
    val showReciterBadge: Boolean = true,
    val useArabicNumerals: Boolean = true,
    val verseDurationSeconds: Int = 6, // Duration per Ayah
    val isPreviewReviewed: Boolean = false, // Must be true before final export confirmation
    val verseTimestamps: List<VerseTimestamp> = emptyList(),
    val showSafeAreas: Boolean = false,
    val quranTextStyle: TypographyStyle = TypographyStyle(
        fontId = "bundled_amiri",
        fontSizeSp = 28f,
        minFontSizeSp = 14f,
        maxFontSizeSp = 64f,
        textColorArgb = AndroidColor.parseColor("#FFFDF7"),
        textShadowColorArgb = AndroidColor.argb(220, 10, 20, 18),
        textShadowRadius = 10f,
        maxWidthRatio = 0.88f
    ),
    val translationTextStyle: TypographyStyle = TypographyStyle(
        fontId = "bundled_cairo",
        fontSizeSp = 15f,
        minFontSizeSp = 10f,
        maxFontSizeSp = 32f,
        textColorArgb = AndroidColor.parseColor("#E0D8C3"),
        textShadowColorArgb = AndroidColor.argb(160, 0, 0, 0),
        textShadowRadius = 6f,
        maxWidthRatio = 0.85f
    ),
    val surahHeaderStyle: TypographyStyle = TypographyStyle(
        fontId = "bundled_cairo",
        fontSizeSp = 20f,
        minFontSizeSp = 12f,
        maxFontSizeSp = 40f,
        textColorArgb = AndroidColor.parseColor("#D4AF37"), // Gold
        textShadowColorArgb = AndroidColor.argb(180, 0, 0, 0),
        textShadowRadius = 8f
    ),
    val ayahBadgeStyle: TypographyStyle = TypographyStyle(
        fontId = "bundled_cairo",
        fontSizeSp = 22f,
        textColorArgb = AndroidColor.parseColor("#D4AF37")
    ),
    val reciterBadgeStyle: TypographyStyle = TypographyStyle(
        fontId = "bundled_cairo",
        fontSizeSp = 13f,
        textColorArgb = AndroidColor.parseColor("#E0D8C3")
    ),
    val layers: List<LayerProperties> = listOf(
        LayerProperties("bg", "الخلفية", offsetYRatio = 0.5f, offsetXRatio = 0.5f),
        LayerProperties("frame", "الإطار والزخارف الإسلامية", offsetYRatio = 0.5f, offsetXRatio = 0.5f),
        LayerProperties("header", "عنوان السورة ورقم الجزء", offsetYRatio = 0.12f, offsetXRatio = 0.5f),
        LayerProperties("quran_text", "نص الآية الكريمة", offsetYRatio = 0.48f, offsetXRatio = 0.5f),
        LayerProperties("ayah_badge", "شارة رقم الآية ﴿﴾", offsetYRatio = 0.48f, offsetXRatio = 0.5f),
        LayerProperties("translation", "الترجمة والتفسير", offsetYRatio = 0.73f, offsetXRatio = 0.5f),
        LayerProperties("reciter", "اسم القارئ وشعار الاستوديو", offsetYRatio = 0.92f, offsetXRatio = 0.5f)
    ),
    val aspectRatioConfigs: Map<AspectRatio, AspectRatioTypographyConfig> = emptyMap(),
    val customPresets: List<TypographyPreset> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Retrieves the typography config for the specified aspect ratio,
     * falling back to the project's base typography or a sensible default tailored to that aspect ratio.
     */
    fun getEffectiveConfigForAspectRatio(ar: AspectRatio): AspectRatioTypographyConfig {
        aspectRatioConfigs[ar]?.let { return it }

        if (ar == this.aspectRatio) {
            return AspectRatioTypographyConfig(
                quranTextStyle = this.quranTextStyle,
                translationTextStyle = this.translationTextStyle,
                surahHeaderStyle = this.surahHeaderStyle,
                ayahBadgeStyle = this.ayahBadgeStyle,
                reciterBadgeStyle = this.reciterBadgeStyle,
                layers = this.layers
            )
        }

        // Compute tailored initial layout if not explicitly saved yet
        return when (ar) {
            AspectRatio.PORTRAIT_9_16 -> AspectRatioTypographyConfig(
                quranTextStyle = quranTextStyle.copy(fontSizeSp = 28f, maxWidthRatio = 0.88f),
                translationTextStyle = translationTextStyle.copy(fontSizeSp = 15f, maxWidthRatio = 0.85f),
                surahHeaderStyle = surahHeaderStyle.copy(fontSizeSp = 20f),
                ayahBadgeStyle = ayahBadgeStyle,
                reciterBadgeStyle = reciterBadgeStyle,
                layers = layers.map {
                    when (it.id) {
                        "header" -> it.copy(offsetYRatio = 0.11f)
                        "quran_text" -> it.copy(offsetYRatio = 0.48f)
                        "translation" -> it.copy(offsetYRatio = 0.73f)
                        "reciter" -> it.copy(offsetYRatio = 0.92f)
                        else -> it
                    }
                }
            )
            AspectRatio.LANDSCAPE_16_9 -> AspectRatioTypographyConfig(
                // In landscape 16:9, height is compact (1080) and width is wide (1920)
                // Font size should be comfortable (24sp) and maxWidth wider (0.75 ratio of 1920 = 1440px)
                quranTextStyle = quranTextStyle.copy(fontSizeSp = 24f, maxWidthRatio = 0.75f),
                translationTextStyle = translationTextStyle.copy(fontSizeSp = 14f, maxWidthRatio = 0.70f),
                surahHeaderStyle = surahHeaderStyle.copy(fontSizeSp = 18f),
                ayahBadgeStyle = ayahBadgeStyle.copy(fontSizeSp = 18f),
                reciterBadgeStyle = reciterBadgeStyle.copy(fontSizeSp = 12f),
                layers = layers.map {
                    when (it.id) {
                        "header" -> it.copy(offsetYRatio = 0.13f)
                        "quran_text" -> it.copy(offsetYRatio = 0.46f)
                        "translation" -> it.copy(offsetYRatio = 0.74f)
                        "reciter" -> it.copy(offsetYRatio = 0.91f)
                        else -> it
                    }
                }
            )
            AspectRatio.SQUARE_1_1 -> AspectRatioTypographyConfig(
                quranTextStyle = quranTextStyle.copy(fontSizeSp = 25f, maxWidthRatio = 0.84f),
                translationTextStyle = translationTextStyle.copy(fontSizeSp = 14f, maxWidthRatio = 0.80f),
                surahHeaderStyle = surahHeaderStyle.copy(fontSizeSp = 19f),
                ayahBadgeStyle = ayahBadgeStyle.copy(fontSizeSp = 19f),
                reciterBadgeStyle = reciterBadgeStyle.copy(fontSizeSp = 12f),
                layers = layers.map {
                    when (it.id) {
                        "header" -> it.copy(offsetYRatio = 0.12f)
                        "quran_text" -> it.copy(offsetYRatio = 0.47f)
                        "translation" -> it.copy(offsetYRatio = 0.74f)
                        "reciter" -> it.copy(offsetYRatio = 0.91f)
                        else -> it
                    }
                }
            )
            AspectRatio.PORTRAIT_4_5 -> AspectRatioTypographyConfig(
                quranTextStyle = quranTextStyle.copy(fontSizeSp = 26f, maxWidthRatio = 0.86f),
                translationTextStyle = translationTextStyle.copy(fontSizeSp = 15f, maxWidthRatio = 0.82f),
                surahHeaderStyle = surahHeaderStyle.copy(fontSizeSp = 19f),
                ayahBadgeStyle = ayahBadgeStyle.copy(fontSizeSp = 20f),
                reciterBadgeStyle = reciterBadgeStyle.copy(fontSizeSp = 12f),
                layers = layers.map {
                    when (it.id) {
                        "header" -> it.copy(offsetYRatio = 0.12f)
                        "quran_text" -> it.copy(offsetYRatio = 0.48f)
                        "translation" -> it.copy(offsetYRatio = 0.73f)
                        "reciter" -> it.copy(offsetYRatio = 0.92f)
                        else -> it
                    }
                }
            )
        }
    }

    /**
     * Switches the aspect ratio while preserving current settings into the dictionary
     * and loading the saved settings for the target aspect ratio if they exist.
     */
    fun withAspectRatio(newAspectRatio: AspectRatio): ProjectConfig {
        if (this.aspectRatio == newAspectRatio) return this

        // 1. Save current active settings into map for current aspect ratio
        val currentConfig = AspectRatioTypographyConfig(
            quranTextStyle = this.quranTextStyle,
            translationTextStyle = this.translationTextStyle,
            surahHeaderStyle = this.surahHeaderStyle,
            ayahBadgeStyle = this.ayahBadgeStyle,
            reciterBadgeStyle = this.reciterBadgeStyle,
            layers = this.layers
        )
        val updatedMap = this.aspectRatioConfigs.toMutableMap()
        updatedMap[this.aspectRatio] = currentConfig

        // 2. Retrieve or generate settings for new aspect ratio
        val newConfig = updatedMap[newAspectRatio] ?: getEffectiveConfigForAspectRatio(newAspectRatio)
        updatedMap[newAspectRatio] = newConfig

        return this.copy(
            aspectRatio = newAspectRatio,
            aspectRatioConfigs = updatedMap,
            quranTextStyle = newConfig.quranTextStyle,
            translationTextStyle = newConfig.translationTextStyle,
            surahHeaderStyle = newConfig.surahHeaderStyle,
            ayahBadgeStyle = newConfig.ayahBadgeStyle,
            reciterBadgeStyle = newConfig.reciterBadgeStyle,
            layers = newConfig.layers
        )
    }

    /**
     * Copies typography settings from one aspect ratio to another.
     */
    fun copyTypographySettings(from: AspectRatio, to: AspectRatio): ProjectConfig {
        val sourceConfig = if (from == this.aspectRatio) {
            AspectRatioTypographyConfig(
                quranTextStyle = this.quranTextStyle,
                translationTextStyle = this.translationTextStyle,
                surahHeaderStyle = this.surahHeaderStyle,
                ayahBadgeStyle = this.ayahBadgeStyle,
                reciterBadgeStyle = this.reciterBadgeStyle,
                layers = this.layers
            )
        } else {
            getEffectiveConfigForAspectRatio(from)
        }

        val updatedMap = this.aspectRatioConfigs.toMutableMap()
        updatedMap[to] = sourceConfig

        return if (this.aspectRatio == to) {
            this.copy(
                aspectRatioConfigs = updatedMap,
                quranTextStyle = sourceConfig.quranTextStyle,
                translationTextStyle = sourceConfig.translationTextStyle,
                surahHeaderStyle = sourceConfig.surahHeaderStyle,
                ayahBadgeStyle = sourceConfig.ayahBadgeStyle,
                reciterBadgeStyle = sourceConfig.reciterBadgeStyle,
                layers = sourceConfig.layers
            )
        } else {
            this.copy(aspectRatioConfigs = updatedMap)
        }
    }

    /**
     * Applies a typography preset to the current aspect ratio.
     */
    fun applyPreset(preset: TypographyPreset): ProjectConfig {
        return this.copy(
            quranTextStyle = preset.quranTextStyle,
            translationTextStyle = preset.translationTextStyle,
            surahHeaderStyle = preset.surahHeaderStyle
        )
    }
}

object DefaultTypographyPresets {
    val presets = listOf(
        TypographyPreset(
            id = "preset_large",
            name = "خط قرآني كبير (Large Focus)",
            description = "تركيز كامل على إبراز جمال النص القرآني بخط كبير وواضح",
            quranTextStyle = TypographyStyle(fontSizeSp = 36f, lineSpacingMultiplier = 1.4f, maxWidthRatio = 0.88f),
            translationTextStyle = TypographyStyle(fontSizeSp = 14f, maxWidthRatio = 0.85f),
            surahHeaderStyle = TypographyStyle(fontSizeSp = 22f)
        ),
        TypographyPreset(
            id = "preset_balanced",
            name = "متوازن قياسي (Medium Balanced)",
            description = "تناسق مثالي بين الآية الكريمة، الترجمة، واسم السورة لجميع المنصات",
            quranTextStyle = TypographyStyle(fontSizeSp = 28f, lineSpacingMultiplier = 1.3f, maxWidthRatio = 0.88f),
            translationTextStyle = TypographyStyle(fontSizeSp = 15f, maxWidthRatio = 0.85f),
            surahHeaderStyle = TypographyStyle(fontSizeSp = 20f)
        ),
        TypographyPreset(
            id = "preset_compact",
            name = "مدمج للآيات الطويلة (Long Verse Layout)",
            description = "حجم خط متزن وملاءمة تلقائية مع زيادة عرض النص للآيات الممتدة",
            quranTextStyle = TypographyStyle(fontSizeSp = 22f, lineSpacingMultiplier = 1.25f, maxWidthRatio = 0.92f, isFitToBox = true),
            translationTextStyle = TypographyStyle(fontSizeSp = 13f, maxWidthRatio = 0.90f),
            surahHeaderStyle = TypographyStyle(fontSizeSp = 18f)
        ),
        TypographyPreset(
            id = "preset_short_verse",
            name = "تأثير الآيات القصيرة (Short Verse Impact)",
            description = "خط عريض وفخم جداً للآيات والكلمات القرآنية القصيرة",
            quranTextStyle = TypographyStyle(fontSizeSp = 40f, lineSpacingMultiplier = 1.45f, maxWidthRatio = 0.84f),
            translationTextStyle = TypographyStyle(fontSizeSp = 16f, maxWidthRatio = 0.80f),
            surahHeaderStyle = TypographyStyle(fontSizeSp = 22f)
        ),
        TypographyPreset(
            id = "preset_translation_focus",
            name = "تركيز على الترجمة والتفسير (Translation-Focused)",
            description = "مساحة بارزة ومقروءة للترجمة بالإنجليزية وتفسير الآية أسفلها",
            quranTextStyle = TypographyStyle(fontSizeSp = 24f, lineSpacingMultiplier = 1.3f, maxWidthRatio = 0.88f),
            translationTextStyle = TypographyStyle(fontSizeSp = 18f, maxWidthRatio = 0.90f),
            surahHeaderStyle = TypographyStyle(fontSizeSp = 18f)
        )
    )
}

data class RenderProgress(
    val isRendering: Boolean = false,
    val progressPercent: Int = 0,
    val currentFrame: Int = 0,
    val totalFrames: Int = 0,
    val statusText: String = "",
    val outputFilePath: String? = null,
    val error: String? = null
)
