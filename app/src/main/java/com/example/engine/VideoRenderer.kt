package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import com.example.R
import com.example.data.Ayah
import com.example.data.FontItem
import com.example.data.FontManager
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.coroutines.coroutineContext
import kotlin.math.min

class VideoRenderer(
    private val context: Context,
    private val fontManager: FontManager,
    private val quranRepository: QuranRepository,
    private val reciterRepository: ReciterRepository
) {

    private val _progress = MutableStateFlow(RenderProgress())
    val progress: StateFlow<RenderProgress> = _progress.asStateFlow()

    private var cachedBgBitmap: Bitmap? = null
    private var lastBgType: BackgroundType? = null
    private var lastBgPath: String? = null

    /**
     * Calculates the canvas scale factor.
     * Normalized against a 1080p reference canvas (min dimension = 1080) with standard base density of 3.0f.
     *
     * In 1080p:
     * - 9:16 (1080x1920) -> min is 1080 -> scale = 3.0f
     * - 16:9 (1920x1080) -> min is 1080 -> scale = 3.0f (NEVER blows up to 5.3f!)
     * - 1:1 (1080x1080)  -> min is 1080 -> scale = 3.0f
     * - 4:5 (1080x1350)  -> min is 1080 -> scale = 3.0f
     *
     * In 720p:
     * - 9:16 (720x1280)  -> min is 720  -> scale = 2.0f (strictly 2/3 of 1080p)
     * - 16:9 (1280x720)  -> min is 720  -> scale = 2.0f
     * - 1:1 (720x720)   -> min is 720  -> scale = 2.0f
     * - 4:5 (720x900)   -> min is 720  -> scale = 2.0f
     */
    fun calculateCanvasScale(width: Int, height: Int): Float {
        val minDim = min(width, height).toFloat()
        return (minDim / 1080f) * 3.0f
    }

    /**
     * Renders a single frame Bitmap for preview or video encoding.
     */
    fun renderFrame(
        project: ProjectConfig,
        currentAyahIndex: Int,
        frameAlpha: Float = 1.0f,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val ayahs = quranRepository.getVersesForSurah(
            project.surahNumber,
            project.startAyah,
            project.endAyah
        )
        val safeAyahIndex = currentAyahIndex.coerceIn(0, (ayahs.size - 1).coerceAtLeast(0))
        val currentAyah = ayahs.getOrNull(safeAyahIndex)
        val surah = quranRepository.allSurahs.find { it.number == project.surahNumber }
            ?: quranRepository.allSurahs[0]
        val reciter = reciterRepository.reciters.find { it.id == project.reciterId }
            ?: reciterRepository.reciters[0]

        // Load aspect-ratio-specific typography and layer positions
        val effectiveConfig = project.getEffectiveConfigForAspectRatio(project.aspectRatio)
        val quranStyle = effectiveConfig.quranTextStyle
        val translationStyle = effectiveConfig.translationTextStyle
        val headerStyle = effectiveConfig.surahHeaderStyle
        val reciterStyle = effectiveConfig.reciterBadgeStyle
        val layers = effectiveConfig.layers

        val canvasScale = calculateCanvasScale(width, height)

        // 1. Draw Background
        val bgLayer = layers.find { it.id == "bg" }
        if (bgLayer?.isVisible != false) {
            drawBackground(canvas, project, width, height, bgLayer?.opacity ?: 1.0f)
        }

        // 2. Draw Islamic Frame
        val frameLayer = layers.find { it.id == "frame" }
        if (project.showIslamicFrame && frameLayer?.isVisible != false) {
            drawIslamicFrame(canvas, width, height, (frameLayer?.opacity ?: 1.0f) * frameAlpha)
        }

        // 3. Draw Surah Header
        val headerLayer = layers.find { it.id == "header" }
        if (project.showSurahHeader && headerLayer?.isVisible != false) {
            val surahTitle = "سُورَةُ ${surah.arabicName}"
            val subtitle = "${surah.revelationType} • آياتها ${surah.versesCount}"
            drawSurahHeader(
                canvas = canvas,
                title = surahTitle,
                subtitle = subtitle,
                style = headerStyle,
                layer = headerLayer ?: LayerProperties("header", "عنوان السورة"),
                width = width,
                height = height,
                canvasScale = canvasScale,
                alpha = (headerLayer?.opacity ?: 1.0f) * frameAlpha
            )
        }

        // 4. Draw Quran Verse Text
        val textLayer = layers.find { it.id == "quran_text" }
        if (currentAyah != null && textLayer?.isVisible != false) {
            val ayahText = currentAyah.arabicText
            val ayahNumber = if (project.useArabicNumerals) {
                toArabicIndicNumerals(currentAyah.numberInSurah)
            } else {
                currentAyah.numberInSurah.toString()
            }

            drawQuranVerse(
                canvas = canvas,
                text = ayahText,
                ayahNumberBadge = ayahNumber,
                style = quranStyle,
                layer = textLayer ?: LayerProperties("quran_text", "نص الآية الكريمة"),
                width = width,
                height = height,
                canvasScale = canvasScale,
                alpha = (textLayer?.opacity ?: 1.0f) * frameAlpha
            )
        }

        // 5. Draw Translation & Tafsir
        val transLayer = layers.find { it.id == "translation" }
        if (currentAyah != null && (project.showTranslation || project.showTafsir) && transLayer?.isVisible != false) {
            val textToShow = if (project.showTafsir) {
                "${currentAyah.translation}\n\nالتفسير: ${currentAyah.tafsir}"
            } else {
                currentAyah.translation
            }
            drawTranslation(
                canvas = canvas,
                translationText = textToShow,
                style = translationStyle,
                layer = transLayer ?: LayerProperties("translation", "الترجمة والتفسير"),
                width = width,
                height = height,
                canvasScale = canvasScale,
                alpha = (transLayer?.opacity ?: 1.0f) * frameAlpha
            )
        }

        // 6. Draw Reciter Badge
        val reciterLayer = layers.find { it.id == "reciter" }
        if (project.showReciterBadge && reciterLayer?.isVisible != false) {
            drawReciterBadge(
                canvas = canvas,
                reciterText = "القارئ: ${reciter.arabicName}",
                style = reciterStyle,
                layer = reciterLayer ?: LayerProperties("reciter", "اسم القارئ"),
                width = width,
                height = height,
                canvasScale = canvasScale,
                alpha = (reciterLayer?.opacity ?: 1.0f) * frameAlpha
            )
        }

        // 7. Draw Safe Area Guides if enabled
        if (project.showSafeAreas) {
            drawSafeAreaGuides(canvas, project.aspectRatio, width, height)
        }

        return bitmap
    }

    private fun drawBackground(
        canvas: Canvas,
        project: ProjectConfig,
        width: Int,
        height: Int,
        layerOpacity: Float
    ) {
        val bgBitmap = loadBackgroundBitmap(project, width, height)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = (255 * layerOpacity * project.backgroundBrightness.coerceIn(0.1f, 1.0f)).toInt()
        }

        if (bgBitmap != null) {
            val srcRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
            val dstRect = Rect(0, 0, width, height)
            canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)
        } else {
            // High luxury dark emerald-gold gradient fallback
            val gradient = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(
                    AndroidColor.parseColor("#061210"),
                    AndroidColor.parseColor("#0B231F"),
                    AndroidColor.parseColor("#040E0C")
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            val gradPaint = Paint().apply {
                shader = gradient
                alpha = (255 * layerOpacity).toInt()
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradPaint)
        }

        // Add soft dark vignette overlay for readability
        val vignette = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                AndroidColor.argb(160, 0, 0, 0),
                AndroidColor.argb(80, 0, 0, 0),
                AndroidColor.argb(190, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        val overlayPaint = Paint().apply { shader = vignette }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
    }

    private fun loadBackgroundBitmap(project: ProjectConfig, targetWidth: Int, targetHeight: Int): Bitmap? {
        if (project.backgroundType == lastBgType &&
            project.customBackgroundImagePath == lastBgPath &&
            cachedBgBitmap != null &&
            !cachedBgBitmap!!.isRecycled
        ) {
            return cachedBgBitmap
        }

        val resId = when (project.backgroundType) {
            BackgroundType.PRESET_MOSQUE -> R.drawable.bg_mosque
            BackgroundType.PRESET_STARS -> R.drawable.bg_stars
            BackgroundType.CUSTOM_IMAGE -> null
            else -> R.drawable.bg_mosque
        }

        val bmp: Bitmap? = if (resId != null) {
            try {
                BitmapFactory.decodeResource(context.resources, resId)
            } catch (e: Exception) {
                null
            }
        } else if (project.customBackgroundImagePath != null) {
            try {
                BitmapFactory.decodeFile(project.customBackgroundImagePath)
            } catch (e: Exception) {
                null
            }
        } else null

        cachedBgBitmap = bmp
        lastBgType = project.backgroundType
        lastBgPath = project.customBackgroundImagePath
        return bmp
    }

    private fun drawIslamicFrame(canvas: Canvas, width: Int, height: Int, alpha: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#D4AF37") // Islamic Gold
            this.alpha = (alpha * 140).toInt().coerceIn(0, 255)
            style = Paint.Style.STROKE
            strokeWidth = (min(width, height) * 0.0035f).coerceAtLeast(2f)
        }

        val margin = min(width, height) * 0.045f
        val rect = RectF(margin, margin, width - margin, height - margin)

        // Outer border
        canvas.drawRoundRect(rect, 16f, 16f, paint)

        // Inner ornate border
        val innerMargin = margin + 8f
        val innerRect = RectF(innerMargin, innerMargin, width - innerMargin, height - innerMargin)
        paint.strokeWidth = 1.5f
        paint.alpha = (alpha * 90).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(innerRect, 12f, 12f, paint)

        // Corner Arabesque diamonds
        val cornerSize = 10f
        val corners = listOf(
            Pair(margin, margin),
            Pair(width - margin, margin),
            Pair(margin, height - margin),
            Pair(width - margin, height - margin)
        )
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#D4AF37")
            this.alpha = (alpha * 180).toInt().coerceIn(0, 255)
            style = Paint.Style.FILL
        }
        for ((cx, cy) in corners) {
            val path = Path().apply {
                moveTo(cx, cy - cornerSize)
                lineTo(cx + cornerSize, cy)
                lineTo(cx, cy + cornerSize)
                lineTo(cx - cornerSize, cy)
                close()
            }
            canvas.drawPath(path, fillPaint)
        }
    }

    private fun drawSurahHeader(
        canvas: Canvas,
        title: String,
        subtitle: String,
        style: TypographyStyle,
        layer: LayerProperties,
        width: Int,
        height: Int,
        canvasScale: Float,
        alpha: Float
    ) {
        val font = fontManager.fonts.value.find { it.id == style.fontId }
        val tf = fontManager.getTypeface(font)

        val bannerCenterX = width * layer.offsetXRatio
        val bannerY = height * layer.offsetYRatio

        val pillWidth = (width * 0.65f).coerceAtLeast(280f)
        val pillHeight = (height * 0.065f).coerceAtLeast(50f)
        val pillLeft = bannerCenterX - (pillWidth / 2f)
        val pillRect = RectF(pillLeft, bannerY, pillLeft + pillWidth, bannerY + pillHeight)

        // Draw decorative banner pill background
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb((alpha * 130).toInt().coerceIn(0, 255), 10, 26, 23)
            this.style = Paint.Style.FILL
        }
        val pillStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#D4AF37")
            this.alpha = (alpha * 180).toInt().coerceIn(0, 255)
            this.style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        canvas.drawRoundRect(pillRect, pillHeight / 2, pillHeight / 2, pillPaint)
        canvas.drawRoundRect(pillRect, pillHeight / 2, pillHeight / 2, pillStroke)

        // Draw Surah Name
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            color = style.textColorArgb
            this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            textSize = style.fontSizeSp * canvasScale * 0.75f
            textAlign = Paint.Align.CENTER
            setShadowLayer(
                style.textShadowRadius,
                0f,
                4f,
                AndroidColor.argb((alpha * 200).toInt().coerceIn(0, 255), 0, 0, 0)
            )
        }

        canvas.drawText(title, bannerCenterX, bannerY + (pillHeight * 0.62f), textPaint)
    }

    private fun drawQuranVerse(
        canvas: Canvas,
        text: String,
        ayahNumberBadge: String,
        style: TypographyStyle,
        layer: LayerProperties,
        width: Int,
        height: Int,
        canvasScale: Float,
        alpha: Float
    ) {
        val font = fontManager.fonts.value.find { it.id == style.fontId }
        val tf = fontManager.getTypeface(font)

        val maxTextWidth = (width * style.maxWidthRatio).toInt().coerceAtLeast(100)
        val maxAllowedHeight = height * style.maxHeightRatio

        val formattedVerse = "$text  ﴿$ayahNumberBadge﴾"

        // Determine effective font size (Fit-to-Box vs Manual)
        var effectiveFontSizeSp = style.fontSizeSp
        if (style.isFitToBox) {
            var currentSize = style.fontSizeSp
            while (currentSize > style.minFontSizeSp) {
                val testPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = tf
                    textSize = currentSize * canvasScale
                }
                val layout = StaticLayout.Builder
                    .obtain(formattedVerse, 0, formattedVerse.length, testPaint, maxTextWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(0f, style.lineSpacingMultiplier)
                    .setIncludePad(true)
                    .build()

                if (layout.height <= maxAllowedHeight) {
                    break
                }
                currentSize -= 1.0f
            }
            effectiveFontSizeSp = currentSize.coerceAtLeast(style.minFontSizeSp)
        }

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            color = style.textColorArgb
            this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            textSize = effectiveFontSizeSp * canvasScale
            setShadowLayer(
                style.textShadowRadius,
                0f,
                6f,
                AndroidColor.argb((alpha * 220).toInt().coerceIn(0, 255), 0, 0, 0)
            )
        }

        val staticLayout = StaticLayout.Builder
            .obtain(formattedVerse, 0, formattedVerse.length, textPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, style.lineSpacingMultiplier)
            .setIncludePad(true)
            .build()

        // Position verse using layer coordinate system
        val targetCenterX = width * layer.offsetXRatio
        val targetCenterY = height * layer.offsetYRatio

        val textTop = targetCenterY - (staticLayout.height / 2f)
        val textLeft = targetCenterX - (maxTextWidth / 2f)

        canvas.save()
        canvas.translate(textLeft, textTop)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawTranslation(
        canvas: Canvas,
        translationText: String,
        style: TypographyStyle,
        layer: LayerProperties,
        width: Int,
        height: Int,
        canvasScale: Float,
        alpha: Float
    ) {
        val font = fontManager.fonts.value.find { it.id == style.fontId }
        val tf = fontManager.getTypeface(font)

        val maxTextWidth = (width * style.maxWidthRatio).toInt().coerceAtLeast(100)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            color = style.textColorArgb
            this.alpha = (alpha * 220).toInt().coerceIn(0, 255)
            textSize = style.fontSizeSp * canvasScale
            setShadowLayer(
                style.textShadowRadius,
                0f,
                3f,
                AndroidColor.argb((alpha * 180).toInt().coerceIn(0, 255), 0, 0, 0)
            )
        }

        val staticLayout = StaticLayout.Builder
            .obtain(translationText, 0, translationText.length, textPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, style.lineSpacingMultiplier)
            .setIncludePad(true)
            .build()

        val targetCenterX = width * layer.offsetXRatio
        val textTop = height * layer.offsetYRatio
        val textLeft = targetCenterX - (maxTextWidth / 2f)

        // Draw soft dark background panel for translation
        val panelPadding = 18f
        val panelRect = RectF(
            textLeft - panelPadding,
            textTop - panelPadding,
            textLeft + maxTextWidth + panelPadding,
            textTop + staticLayout.height + panelPadding
        )
        val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb((alpha * 100).toInt().coerceIn(0, 255), 10, 16, 14)
            this.style = Paint.Style.FILL
        }
        canvas.drawRoundRect(panelRect, 16f, 16f, panelPaint)

        canvas.save()
        canvas.translate(textLeft, textTop)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawReciterBadge(
        canvas: Canvas,
        reciterText: String,
        style: TypographyStyle,
        layer: LayerProperties,
        width: Int,
        height: Int,
        canvasScale: Float,
        alpha: Float
    ) {
        val tf = fontManager.getTypeface(fontManager.fonts.value.find { it.id == style.fontId })
        val badgePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            color = style.textColorArgb
            this.alpha = (alpha * 200).toInt().coerceIn(0, 255)
            textSize = style.fontSizeSp * canvasScale
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 0f, 2f, AndroidColor.BLACK)
        }

        val badgeCenterX = width * layer.offsetXRatio
        val badgeY = height * layer.offsetYRatio
        canvas.drawText(reciterText, badgeCenterX, badgeY, badgePaint)
    }

    /**
     * Draws delicate safe-area guides onto canvas for editor preview.
     */
    private fun drawSafeAreaGuides(canvas: Canvas, aspectRatio: AspectRatio, width: Int, height: Int) {
        val (topMarginRatio, bottomMarginRatio, sideMarginRatio) = when (aspectRatio) {
            AspectRatio.PORTRAIT_9_16 -> Triple(0.12f, 0.12f, 0.06f)
            AspectRatio.LANDSCAPE_16_9 -> Triple(0.06f, 0.06f, 0.06f)
            AspectRatio.SQUARE_1_1 -> Triple(0.06f, 0.06f, 0.06f)
            AspectRatio.PORTRAIT_4_5 -> Triple(0.08f, 0.08f, 0.06f)
        }

        val left = width * sideMarginRatio
        val right = width * (1f - sideMarginRatio)
        val top = height * topMarginRatio
        val bottom = height * (1f - bottomMarginRatio)

        val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#4DD0E1") // Cyan safe guide
            alpha = 110
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
        }

        canvas.drawRoundRect(RectF(left, top, right, bottom), 12f, 12f, guidePaint)

        // Corner tick markers
        val cornerTick = 16f
        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#D4AF37")
            alpha = 180
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        // Top-left
        canvas.drawLine(left, top, left + cornerTick, top, tickPaint)
        canvas.drawLine(left, top, left, top + cornerTick, tickPaint)
        // Top-right
        canvas.drawLine(right, top, right - cornerTick, top, tickPaint)
        canvas.drawLine(right, top, right, top + cornerTick, tickPaint)
        // Bottom-left
        canvas.drawLine(left, bottom, left + cornerTick, bottom, tickPaint)
        canvas.drawLine(left, bottom, left, bottom - cornerTick, tickPaint)
        // Bottom-right
        canvas.drawLine(right, bottom, right - cornerTick, bottom, tickPaint)
        canvas.drawLine(right, bottom, right, bottom - cornerTick, tickPaint)

        // Label
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#4DD0E1")
            alpha = 150
            textSize = 22f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("منطقة الأمان (Safe Area)", left + 10f, top + 30f, labelPaint)
    }

    /**
     * Inspects text layout and reports whether the text fits within the canvas and layers do not collide.
     */
    fun checkTextFit(
        project: ProjectConfig,
        currentAyah: Ayah?,
        width: Int,
        height: Int
    ): TextFitStatus {
        if (currentAyah == null) return TextFitStatus()

        val effectiveConfig = project.getEffectiveConfigForAspectRatio(project.aspectRatio)
        val quranStyle = effectiveConfig.quranTextStyle
        val layers = effectiveConfig.layers
        val canvasScale = calculateCanvasScale(width, height)

        val font = fontManager.fonts.value.find { it.id == quranStyle.fontId }
        val tf = fontManager.getTypeface(font)
        val maxTextWidth = (width * quranStyle.maxWidthRatio).toInt().coerceAtLeast(100)

        val formattedVerse = "${currentAyah.arabicText}  ﴿${currentAyah.numberInSurah}﴾"

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            textSize = quranStyle.fontSizeSp * canvasScale
        }

        val staticLayout = StaticLayout.Builder
            .obtain(formattedVerse, 0, formattedVerse.length, textPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, quranStyle.lineSpacingMultiplier)
            .setIncludePad(true)
            .build()

        val fontHeight = textPaint.fontSpacing.coerceAtLeast(textPaint.textSize)
        val estimatedHeight = maxOf(staticLayout.height, (staticLayout.lineCount * fontHeight * quranStyle.lineSpacingMultiplier).toInt())

        val textLayer = layers.find { it.id == "quran_text" }
            ?: LayerProperties("quran_text", "نص الآية")
        val targetCenterY = height * textLayer.offsetYRatio
        val textTop = targetCenterY - (estimatedHeight / 2f)
        val textBottom = targetCenterY + (estimatedHeight / 2f)

        val exceedsTop = textTop < (height * 0.05f)
        val exceedsBottom = textBottom > (height * 0.95f)
        val maxAllowedHeight = height * quranStyle.maxHeightRatio
        val exceedsMaxHeight = estimatedHeight > maxAllowedHeight || quranStyle.fontSizeSp > quranStyle.maxFontSizeSp

        val warningMsg = when {
            exceedsTop && exceedsBottom -> "النص القرآني يتجاوز أعلى وأسفل إطار الفيديو تماماً!"
            exceedsTop -> "النص القرآني يقترب من الحافة العلوية أو يتداخل مع عنوان السورة."
            exceedsBottom -> "النص القرآني يتجاوز المساحة السفلية أو يتداخل مع الترجمة."
            exceedsMaxHeight -> "ارتفاع النص (${staticLayout.height.toInt()}px) يتجاوز الصندوق المخصص."
            else -> null
        }

        return TextFitStatus(
            fitsInsideCanvas = !exceedsTop && !exceedsBottom && !exceedsMaxHeight,
            isOverlapping = exceedsTop || exceedsBottom,
            effectiveFontSizeSp = quranStyle.fontSizeSp,
            scaleFactor = canvasScale,
            warningMessage = warningMsg,
            exceedsTop = exceedsTop,
            exceedsBottom = exceedsBottom,
            exceedsSides = false
        )
    }

    /**
     * Converts western digits to eastern Arabic-Indic digits: 123 -> ١٢٣
     */
    private fun toArabicIndicNumerals(number: Int): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { char ->
            if (char.isDigit()) arabicDigits[char - '0'] else char
        }.joinToString("")
    }

    /**
     * Renders a high-resolution Still Thumbnail (PNG / JPEG).
     */
    fun exportThumbnail(
        project: ProjectConfig,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 95
    ): File {
        val (width, height) = project.aspectRatio.getDimensions(Resolution.RES_1080P)
        val bitmap = renderFrame(
            project = project,
            currentAyahIndex = 0,
            frameAlpha = 1.0f,
            width = width,
            height = height
        )

        val outDir = File(context.getExternalFilesDir(null), "thumbnails").apply { mkdirs() }
        val ext = if (format == Bitmap.CompressFormat.PNG) "png" else "jpg"
        val outFile = File(outDir, "thumb_${project.id}_${System.currentTimeMillis()}.$ext")

        FileOutputStream(outFile).use { out ->
            bitmap.compress(format, quality, out)
        }
        bitmap.recycle()
        return outFile
    }

    /**
     * Complete Genuine Video Generation Pipeline.
     * Renders all frames with chosen fonts, styles, animations and encodes to MP4 file.
     */
    suspend fun renderAndExportVideo(project: ProjectConfig): File? = withContext(Dispatchers.Default) {
        val (width, height) = project.aspectRatio.getDimensions(project.resolution)
        val fps = project.fps.value
        val ayahs = quranRepository.getVersesForSurah(
            project.surahNumber,
            project.startAyah,
            project.endAyah
        )
        val verseCount = ayahs.size.coerceAtLeast(1)
        val durationPerAyahSec = project.verseDurationSeconds
        val totalDurationSec = verseCount * durationPerAyahSec
        val totalFrames = totalDurationSec * fps

        val outDir = File(context.getExternalFilesDir(null), "exported_videos").apply { mkdirs() }
        val outputFile = File(outDir, "quran_video_${System.currentTimeMillis()}.mp4")

        _progress.value = RenderProgress(
            isRendering = true,
            progressPercent = 0,
            currentFrame = 0,
            totalFrames = totalFrames,
            statusText = "بدء تهيئة محرك ترميز الفيديو..."
        )

        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var videoTrackIndex = -1
        var isMuxerStarted = false

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, if (project.resolution == Resolution.RES_1080P) 6_000_000 else 3_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val bufferInfo = MediaCodec.BufferInfo()

            val framesPerAyah = durationPerAyahSec * fps

            for (frameIdx in 0 until totalFrames) {
                if (!coroutineContext.isActive) {
                    throw InterruptedException("تم إلغاء عملية التصدير من قبل المستخدم")
                }

                val currentAyahIdx = (frameIdx / framesPerAyah).coerceIn(0, verseCount - 1)
                val frameInAyah = frameIdx % framesPerAyah

                // Animation alpha calculation
                val alpha = when (project.animation) {
                    TextAnimation.NONE -> 1.0f
                    TextAnimation.FADE_IN -> {
                        val fadeFrames = (fps * 1.0f).toInt()
                        (frameInAyah.toFloat() / fadeFrames).coerceIn(0f, 1f)
                    }
                    TextAnimation.GENTLE_SLIDE, TextAnimation.SMOOTH_REVEAL -> {
                        val revealFrames = (fps * 0.8f).toInt()
                        (frameInAyah.toFloat() / revealFrames).coerceIn(0.1f, 1f)
                    }
                }

                // Render frame onto surface canvas
                val surfaceCanvas = inputSurface.lockCanvas(null)
                try {
                    val frameBmp = renderFrame(
                        project = project,
                        currentAyahIndex = currentAyahIdx,
                        frameAlpha = alpha,
                        width = width,
                        height = height
                    )
                    surfaceCanvas.drawBitmap(frameBmp, 0f, 0f, null)
                    frameBmp.recycle()
                } finally {
                    inputSurface.unlockCanvasAndPost(surfaceCanvas)
                }

                // Drain encoder
                drainEncoder(encoder, muxer, bufferInfo) { trackIdx, started ->
                    videoTrackIndex = trackIdx
                    isMuxerStarted = started
                }

                val percent = ((frameIdx + 1) * 100 / totalFrames)
                _progress.value = RenderProgress(
                    isRendering = true,
                    progressPercent = percent,
                    currentFrame = frameIdx + 1,
                    totalFrames = totalFrames,
                    statusText = "جاري تصدير الإطار ${frameIdx + 1} من $totalFrames ($percent%)..."
                )
            }

            // Signal end of stream
            encoder.signalEndOfInputStream()

            // Final drain until EOS
            drainEncoder(encoder, muxer, bufferInfo, endOfStream = true) { trackIdx, started ->
                videoTrackIndex = trackIdx
                isMuxerStarted = started
            }

            _progress.value = RenderProgress(
                isRendering = false,
                progressPercent = 100,
                currentFrame = totalFrames,
                totalFrames = totalFrames,
                statusText = "اكتمل التصدير بنجاح!",
                outputFilePath = outputFile.absolutePath
            )

            outputFile
        } catch (e: Exception) {
            Log.e("VideoRenderer", "Video export failed", e)
            _progress.value = RenderProgress(
                isRendering = false,
                statusText = "فشل التصدير: ${e.message}",
                error = e.localizedMessage
            )
            null
        } finally {
            try {
                encoder?.stop()
                encoder?.release()
            } catch (e: Exception) {
                Log.e("VideoRenderer", "Error stopping encoder", e)
            }
            try {
                if (isMuxerStarted) {
                    muxer?.stop()
                }
                muxer?.release()
            } catch (e: Exception) {
                Log.e("VideoRenderer", "Error stopping muxer", e)
            }
        }
    }

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean = false,
        onMuxerStateChange: (trackIndex: Int, isStarted: Boolean) -> Unit
    ) {
        var videoTrackIndex = -1
        var isMuxerStarted = false

        while (true) {
            val encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = encoder.outputFormat
                videoTrackIndex = muxer.addTrack(newFormat)
                muxer.start()
                isMuxerStarted = true
                onMuxerStateChange(videoTrackIndex, isMuxerStarted)
            } else if (encoderStatus >= 0) {
                val encodedData: ByteBuffer = encoder.getOutputBuffer(encoderStatus) ?: continue
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0
                }

                if (bufferInfo.size != 0) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(videoTrackIndex.coerceAtLeast(0), encodedData, bufferInfo)
                }

                encoder.releaseOutputBuffer(encoderStatus, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }
}
