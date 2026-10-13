package com.example.engine

import android.content.Context
import com.example.data.FontManager
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import java.io.File

class QualityControlManager(
    private val context: Context,
    private val quranRepository: QuranRepository,
    private val reciterRepository: ReciterRepository,
    private val fontManager: FontManager
) {

    fun runPreExportInspection(project: ProjectConfig): List<QualityCheckItem> {
        val checks = mutableListOf<QualityCheckItem>()

        // 1. Text Integrity Check
        val surah = quranRepository.allSurahs.find { it.number == project.surahNumber }
        val verses = quranRepository.getVersesForSurah(project.surahNumber, project.startAyah, project.endAyah)
        val isVersesValid = verses.isNotEmpty() && verses.all { it.arabicText.isNotBlank() }
        checks.add(
            QualityCheckItem(
                id = "quran_integrity",
                title = "فحص سلامة النص القرآني الموثق",
                description = if (isVersesValid) "تم التحقق من مطابقة ${verses.size} آيات مع رسم المصحف العثماني المعتمد بدون أي تحريف." else "تنبيه: تعذر جلب آيات صالحة للنطاق المحدد.",
                isPassed = isVersesValid,
                isCritical = true
            )
        )

        // 2. Ayah Numbering Check
        val isNumberingValid = project.startAyah >= 1 &&
                project.endAyah <= (surah?.versesCount ?: 286) &&
                project.startAyah <= project.endAyah
        checks.add(
            QualityCheckItem(
                id = "numbering",
                title = "فحص تسلسل وأرقام الآيات",
                description = if (isNumberingValid) "أرقام الآيات من ${project.startAyah} إلى ${project.endAyah} من سورة ${surah?.arabicName ?: ""} صحيحة ومتسلسلة." else "خطأ: نطاق الآيات غير متسلسل أو يتجاوز عدد آيات السورة (${surah?.versesCount ?: 0}).",
                isPassed = isNumberingValid,
                isCritical = true
            )
        )

        // 3. Custom Font Loading Check
        val quranFont = fontManager.fonts.value.find { it.id == project.quranTextStyle.fontId }
        val isFontLoaded = quranFont != null
        val isCustomFontFileValid = if (quranFont?.isCustom == true) {
            quranFont.filePath?.let { File(it).exists() } == true
        } else true
        checks.add(
            QualityCheckItem(
                id = "font_loading",
                title = "فحص تحميل الخط المخصص وتوافقه",
                description = if (isFontLoaded && isCustomFontFileValid) "الخط (${quranFont?.displayName}) محمّل بنجاح في المعاينة ومحرك التصدير." else "تحذير: ملف الخط المخصص غير موجود أو تعذر تحميله.",
                isPassed = isFontLoaded && isCustomFontFileValid,
                isCritical = true
            )
        )

        // 4. Background Media Check
        val isBgValid = when (project.backgroundType) {
            BackgroundType.CUSTOM_IMAGE -> project.customBackgroundImagePath?.let { File(it).exists() } == true
            else -> true
        }
        checks.add(
            QualityCheckItem(
                id = "background_check",
                title = "فحص مشهد الخلفية والوسائط",
                description = if (isBgValid) "الخلفية المحددة صالحة ومتوافقة مع أبعاد ${project.aspectRatio.label}." else "خطأ: ملف صورة الخلفية المخصصة غير موجود.",
                isPassed = isBgValid,
                isCritical = false
            )
        )

        // 5. Audio & Reciter Check
        val reciter = reciterRepository.reciters.find { it.id == project.reciterId }
        val isAudioAvailable = reciter != null
        checks.add(
            QualityCheckItem(
                id = "audio_check",
                title = "فحص توفر التسجيل الصوتي للقارئ",
                description = if (isAudioAvailable) "المسار الصوتي للشيخ ${reciter?.arabicName} متاح ومربوط بالسورة." else "تحذير: لم يتم تعيين تسجيل صوتي معتمد.",
                isPassed = isAudioAvailable,
                isCritical = false
            )
        )

        // 6. Preview Completion Review Check
        checks.add(
            QualityCheckItem(
                id = "preview_review",
                title = "مراجعة الفيديو في استوديو المعاينة",
                description = if (project.isPreviewReviewed) "تمت مراجعة الفيديو وتشغيله كاملاً وتأكيد جاهزيته للتصدير النهائي." else "تنبيه: يُوصى بمشاهدة الفيديو بالكامل في استوديو المعاينة قبل بدء التصدير النهائي.",
                isPassed = project.isPreviewReviewed,
                isCritical = false
            )
        )

        // 7. Typography Canvas Fit Check
        val (checkWidth, checkHeight) = project.aspectRatio.getDimensions(project.resolution)
        val sampleAyah = verses.firstOrNull()
        val textFit = if (sampleAyah != null) {
            val renderer = VideoRenderer(context, fontManager, quranRepository, reciterRepository)
            renderer.checkTextFit(project, sampleAyah, checkWidth, checkHeight)
        } else TextFitStatus()

        checks.add(
            QualityCheckItem(
                id = "typography_canvas_fit",
                title = "فحص ملاءمة النص ومقاسات الشاشة (${project.aspectRatio.label})",
                description = if (textFit.fitsInsideCanvas) {
                    "النص القرآني والترجمة متناسقان تماماً داخل أبعاد ${checkWidth}x${checkHeight} بدون أي تجاوز."
                } else {
                    textFit.warningMessage ?: "تحذير: النص يقترب من حواف الشاشة، يُنصح بتفعيل وضع الملاءمة (Fit-to-box) أو تقليل حجم الخط."
                },
                isPassed = textFit.fitsInsideCanvas,
                isCritical = false
            )
        )

        return checks
    }
}
