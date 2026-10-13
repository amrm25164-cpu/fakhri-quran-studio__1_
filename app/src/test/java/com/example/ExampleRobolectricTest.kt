package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FontManager
import com.example.data.ProjectRepository
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import com.example.engine.AspectRatio
import com.example.engine.ProjectConfig
import com.example.engine.QualityControlManager
import com.example.engine.Resolution
import com.example.engine.TypographyStyle
import com.example.engine.VideoRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext_matchesAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Fakhri Quran Studio", appName)
    }

    @Test
    fun quranRepository_contains114Surahs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranRepository(context)
        assertEquals(114, repo.allSurahs.size)
        val fatiha = repo.allSurahs.first()
        assertEquals(1, fatiha.number)
        assertEquals("الفاتحة", fatiha.arabicName)
        assertEquals(7, fatiha.versesCount)
    }

    @Test
    fun quranRepository_verifiedVerses() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranRepository(context)
        val verses = repo.getVersesForSurah(1, 1, 7)
        assertEquals(7, verses.size)
        assertTrue(verses[0].arabicText.contains("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"))
    }

    @Test
    fun reciterRepository_has16Reciters() {
        val repo = ReciterRepository()
        assertEquals(16, repo.reciters.size)
        val sudais = repo.reciters.find { it.id == "sudais" }
        assertNotNull(sudais)
        val audioUrl = repo.getAudioUrlForSurah(sudais!!, 1)
        assertTrue(audioUrl.endsWith("001.mp3"))
    }

    @Test
    fun fontManager_hasBundledFonts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fontManager = FontManager(context)
        val fonts = fontManager.fonts.value
        assertTrue(fonts.size >= 5)
        assertTrue(fonts.any { it.id == "bundled_amiri" })
        assertTrue(fonts.any { it.id == "bundled_cairo" })
    }

    @Test
    fun qualityControl_checksIntegrity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val quranRepo = QuranRepository(context)
        val reciterRepo = ReciterRepository()
        val fontManager = FontManager(context)
        val qc = QualityControlManager(context, quranRepo, reciterRepo, fontManager)

        val project = ProjectConfig(
            surahNumber = 1,
            startAyah = 1,
            endAyah = 7,
            isPreviewReviewed = true
        )
        val checks = qc.runPreExportInspection(project)
        assertTrue(checks.isNotEmpty())
        assertTrue(checks.any { it.id == "quran_integrity" && it.isPassed })
        assertTrue(checks.any { it.id == "numbering" && it.isPassed })
        assertTrue(checks.any { it.id == "typography_canvas_fit" })
    }

    @Test
    fun projectRepository_savesAndDuplicatesProjects() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ProjectRepository(context)
        val initialSize = repo.projects.value.size
        assertTrue(initialSize > 0)

        val newProj = ProjectConfig(
            title = "مشروع اختباري",
            surahNumber = 112,
            startAyah = 1,
            endAyah = 4,
            aspectRatio = AspectRatio.SQUARE_1_1,
            resolution = Resolution.RES_720P
        )
        repo.saveProject(newProj)
        assertTrue(repo.projects.value.any { it.id == newProj.id })

        val duplicated = repo.duplicateProject(newProj)
        assertTrue(duplicated.title.contains("نسخة"))
        assertTrue(repo.projects.value.any { it.id == duplicated.id })
    }

    /**
     * Requirement 1 & 4 Verification:
     * Responsive canvasScale calculation across all 4 aspect ratios at 1080p and 720p.
     * Text scale factor must remain constant (3.0f) across 1080p regardless of aspect ratio,
     * preventing 16:9 text from exploding.
     */
    @Test
    fun responsiveScaling_consistentAcrossAllAspectRatiosAt1080p() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val renderer = VideoRenderer(
            context,
            FontManager(context),
            QuranRepository(context),
            ReciterRepository()
        )

        // 1080p resolutions:
        // 9:16 -> 1080 x 1920
        val scale916 = renderer.calculateCanvasScale(1080, 1920)
        // 16:9 -> 1920 x 1080
        val scale169 = renderer.calculateCanvasScale(1920, 1080)
        // 1:1  -> 1080 x 1080
        val scale11 = renderer.calculateCanvasScale(1080, 1080)
        // 4:5  -> 1080 x 1350
        val scale45 = renderer.calculateCanvasScale(1080, 1350)

        assertEquals("9:16 at 1080p must have scale 3.0f", 3.0f, scale916, 0.001f)
        assertEquals("16:9 at 1080p must NOT explode and must have scale 3.0f", 3.0f, scale169, 0.001f)
        assertEquals("1:1 at 1080p must have scale 3.0f", 3.0f, scale11, 0.001f)
        assertEquals("4:5 at 1080p must have scale 3.0f", 3.0f, scale45, 0.001f)

        // Changing preview resolution without changing aspect ratio scales strictly proportionally (2/3)
        val scale720p = renderer.calculateCanvasScale(720, 1280)
        assertEquals("720p must scale proportionally to 2.0f", 2.0f, scale720p, 0.001f)
        assertEquals("Resolution scale ratio must be exactly 720/1080 = 2/3", 2.0f / 3.0f, scale720p / scale916, 0.001f)
    }

    /**
     * Requirement 3 Verification:
     * Independent Settings for Each Aspect Ratio.
     * Changing aspect ratio preserves individual typography and layer settings.
     */
    @Test
    fun independentSettings_preservedForEachAspectRatio() {
        var project = ProjectConfig(
            aspectRatio = AspectRatio.PORTRAIT_9_16,
            quranTextStyle = TypographyStyle(fontSizeSp = 28f)
        )

        // Switch to 16:9 and customize font size to 22f
        project = project.withAspectRatio(AspectRatio.LANDSCAPE_16_9)
        project = project.copy(
            quranTextStyle = project.quranTextStyle.copy(fontSizeSp = 22f)
        )
        assertEquals(22f, project.quranTextStyle.fontSizeSp, 0.01f)

        // Switch back to 9:16 -> 9:16 setting should be 28f!
        project = project.withAspectRatio(AspectRatio.PORTRAIT_9_16)
        assertEquals(28f, project.quranTextStyle.fontSizeSp, 0.01f)

        // Switch back to 16:9 -> 16:9 setting should still be 22f!
        project = project.withAspectRatio(AspectRatio.LANDSCAPE_16_9)
        assertEquals(22f, project.quranTextStyle.fontSizeSp, 0.01f)

        // Copy settings from 16:9 to 1:1
        project = project.copyTypographySettings(from = AspectRatio.LANDSCAPE_16_9, to = AspectRatio.SQUARE_1_1)
        project = project.withAspectRatio(AspectRatio.SQUARE_1_1)
        assertEquals(22f, project.quranTextStyle.fontSizeSp, 0.01f)
    }

    /**
     * Requirement 6 & 7 Verification:
     * Quran text fit inspection and Fit-to-Box layout mode.
     */
    @Test
    fun quranTextLayout_fitInspectionAndFitToBox() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val renderer = VideoRenderer(
            context,
            FontManager(context),
            QuranRepository(context),
            ReciterRepository()
        )
        val quranRepo = QuranRepository(context)
        val verse = quranRepo.getVersesForSurah(1, 1, 1).first()

        // Normal font size fits inside 1080x1920 canvas
        val normalProject = ProjectConfig(
            aspectRatio = AspectRatio.PORTRAIT_9_16,
            quranTextStyle = TypographyStyle(fontSizeSp = 28f, isFitToBox = false)
        )
        val fitNormal = renderer.checkTextFit(normalProject, verse, 1080, 1920)
        assertTrue("Normal verse text must fit inside canvas", fitNormal.fitsInsideCanvas)

        // Extremely huge font size without fit-to-box triggers overflow warning
        val hugeProject = ProjectConfig(
            aspectRatio = AspectRatio.LANDSCAPE_16_9,
            quranTextStyle = TypographyStyle(fontSizeSp = 220f, isFitToBox = false)
        )
        val fitHuge = renderer.checkTextFit(hugeProject, verse, 1920, 1080)
        assertFalse("Huge font size without fit-to-box should report overflow", fitHuge.fitsInsideCanvas)
        assertNotNull("Warning message must be present on overflow", fitHuge.warningMessage)
    }

    /**
     * Requirement 8 & 10 Verification:
     * Full Frame Rendering across 1080x1920, 1920x1080, 1080x1080, and 1080x1350.
     * Text renders and produces valid non-null bitmaps with custom Arabic fonts.
     */
    @Test
    fun renderFrame_producesValidBitmapsAtAllAspectRatios() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val renderer = VideoRenderer(
            context,
            FontManager(context),
            QuranRepository(context),
            ReciterRepository()
        )

        val project = ProjectConfig(
            surahNumber = 1,
            startAyah = 1,
            endAyah = 7
        )

        val dimensions = listOf(
            Pair(1080, 1920), // 9:16
            Pair(1920, 1080), // 16:9
            Pair(1080, 1080), // 1:1
            Pair(1080, 1350)  // 4:5
        )

        for ((w, h) in dimensions) {
            val bmp = renderer.renderFrame(
                project = project,
                currentAyahIndex = 0,
                frameAlpha = 1.0f,
                width = w,
                height = h
            )
            assertNotNull(bmp)
            assertEquals(w, bmp.width)
            assertEquals(h, bmp.height)
            bmp.recycle()
        }
    }
}
