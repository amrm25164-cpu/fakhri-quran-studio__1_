package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FontManager
import com.example.data.ProjectRepository
import com.example.data.QuranRepository
import com.example.data.ReciterRepository
import com.example.engine.AiStudioAssistant
import com.example.engine.AudioPlayerManager
import com.example.engine.ProjectConfig
import com.example.engine.QualityControlManager
import com.example.engine.VideoRenderer
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AiDirectorScreen
import com.example.ui.screens.BatchQueueScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportCenterScreen
import com.example.ui.screens.FontManagerScreen
import com.example.ui.screens.MediaLibraryScreen
import com.example.ui.screens.PreviewStudioScreen
import com.example.ui.screens.QuranLibraryScreen
import com.example.ui.screens.RecitersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TemplateLibraryScreen
import com.example.ui.screens.ThumbnailEditorScreen
import com.example.ui.screens.TimelineEditorScreen
import com.example.ui.screens.VideoCreatorScreen
import com.example.ui.theme.IslamicCream
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceContainerDark

class MainActivity : ComponentActivity() {

    private lateinit var quranRepository: QuranRepository
    private lateinit var reciterRepository: ReciterRepository
    private lateinit var fontManager: FontManager
    private lateinit var projectRepository: ProjectRepository
    private lateinit var videoRenderer: VideoRenderer
    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var aiStudioAssistant: AiStudioAssistant
    private lateinit var qualityControlManager: QualityControlManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        quranRepository = QuranRepository(applicationContext)
        reciterRepository = ReciterRepository()
        fontManager = FontManager(applicationContext)
        projectRepository = ProjectRepository(applicationContext)
        videoRenderer = VideoRenderer(applicationContext, fontManager, quranRepository, reciterRepository)
        audioPlayerManager = AudioPlayerManager(applicationContext)
        aiStudioAssistant = AiStudioAssistant()
        qualityControlManager = QualityControlManager(applicationContext, quranRepository, reciterRepository, fontManager)

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    FakhriQuranStudioApp(
                        quranRepository = quranRepository,
                        reciterRepository = reciterRepository,
                        fontManager = fontManager,
                        projectRepository = projectRepository,
                        videoRenderer = videoRenderer,
                        audioPlayerManager = audioPlayerManager,
                        aiStudioAssistant = aiStudioAssistant,
                        qualityControlManager = qualityControlManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.release()
    }
}

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "الرئيسية")
    object VideoCreator : Screen("creator", "صانع الفيديو")
    object PreviewStudio : Screen("preview", "المعاينة الكاملة")
    object Timeline : Screen("timeline", "محرر الطبقات")
    object FontManager : Screen("fonts", "مدير الخطوط")
    object QuranLibrary : Screen("quran", "المصحف")
    object Reciters : Screen("reciters", "القراء")
    object ExportCenter : Screen("export", "مركز التصدير")
    object AiDirector : Screen("director", "المخرج الذكي")
    object MediaLibrary : Screen("media", "الوسائط")
    object TemplateLibrary : Screen("templates", "القوالب")
    object Thumbnail : Screen("thumbnail", "الأغلفة")
    object Batch : Screen("batch", "الدفعات")
    object AiAssistant : Screen("ai", "المساعد الذكي")
    object Settings : Screen("settings", "الإعدادات")
}

@Composable
fun FakhriQuranStudioApp(
    quranRepository: QuranRepository,
    reciterRepository: ReciterRepository,
    fontManager: FontManager,
    projectRepository: ProjectRepository,
    videoRenderer: VideoRenderer,
    audioPlayerManager: AudioPlayerManager,
    aiStudioAssistant: AiStudioAssistant,
    qualityControlManager: QualityControlManager
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var editingProject by remember { mutableStateOf<ProjectConfig?>(null) }

    // Android back button handling
    BackHandler(enabled = currentScreen != Screen.Dashboard) {
        currentScreen = Screen.Dashboard
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark),
        bottomBar = {
            StudioBottomNav(
                currentScreen = currentScreen,
                onSelectScreen = { screen ->
                    currentScreen = screen
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Dashboard -> DashboardScreen(
                    quranRepository = quranRepository,
                    reciterRepository = reciterRepository,
                    fontManager = fontManager,
                    projectRepository = projectRepository,
                    onNavigate = { route ->
                        currentScreen = when (route) {
                            "creator" -> Screen.VideoCreator
                            "preview" -> Screen.PreviewStudio
                            "timeline" -> Screen.Timeline
                            "fonts" -> Screen.FontManager
                            "quran" -> Screen.QuranLibrary
                            "reciters" -> Screen.Reciters
                            "thumbnail" -> Screen.Thumbnail
                            "batch" -> Screen.Batch
                            "ai" -> Screen.AiAssistant
                            "director" -> Screen.AiDirector
                            "media" -> Screen.MediaLibrary
                            "templates" -> Screen.TemplateLibrary
                            "export" -> Screen.ExportCenter
                            "settings" -> Screen.Settings
                            else -> Screen.Dashboard
                        }
                    },
                    onOpenProject = { proj ->
                        editingProject = proj
                        currentScreen = Screen.VideoCreator
                    },
                    onNewProject = {
                        editingProject = ProjectConfig()
                        currentScreen = Screen.VideoCreator
                    }
                )

                Screen.VideoCreator -> VideoCreatorScreen(
                    quranRepository = quranRepository,
                    reciterRepository = reciterRepository,
                    fontManager = fontManager,
                    projectRepository = projectRepository,
                    videoRenderer = videoRenderer,
                    audioPlayerManager = audioPlayerManager,
                    initialProject = editingProject,
                    onNavigate = { route ->
                        currentScreen = when (route) {
                            "preview" -> Screen.PreviewStudio
                            "timeline" -> Screen.Timeline
                            "export" -> Screen.ExportCenter
                            else -> Screen.Dashboard
                        }
                    }
                )

                Screen.PreviewStudio -> PreviewStudioScreen(
                    quranRepository = quranRepository,
                    reciterRepository = reciterRepository,
                    fontManager = fontManager,
                    projectRepository = projectRepository,
                    videoRenderer = videoRenderer,
                    audioPlayerManager = audioPlayerManager,
                    qualityControlManager = qualityControlManager,
                    initialProject = editingProject,
                    onProceedToExport = { confirmedProj ->
                        editingProject = confirmedProj
                        currentScreen = Screen.ExportCenter
                    }
                )

                Screen.Timeline -> TimelineEditorScreen(
                    quranRepository = quranRepository,
                    reciterRepository = reciterRepository,
                    fontManager = fontManager,
                    projectRepository = projectRepository,
                    videoRenderer = videoRenderer,
                    onBack = { currentScreen = Screen.Dashboard }
                )

                Screen.FontManager -> FontManagerScreen(
                    fontManager = fontManager,
                    onBack = { currentScreen = Screen.Dashboard }
                )

                Screen.QuranLibrary -> QuranLibraryScreen(
                    quranRepository = quranRepository,
                    fontManager = fontManager,
                    onCreateVideoFromSurah = { surah ->
                        editingProject = ProjectConfig(
                            surahNumber = surah.number,
                            startAyah = 1,
                            endAyah = surah.versesCount.coerceAtMost(7),
                            title = "سورة ${surah.arabicName}"
                        )
                        currentScreen = Screen.VideoCreator
                    }
                )

                Screen.Reciters -> RecitersScreen(
                    reciterRepository = reciterRepository,
                    audioPlayerManager = audioPlayerManager,
                    onSelectReciterForProject = { reciter ->
                        editingProject = (editingProject ?: ProjectConfig()).copy(reciterId = reciter.id)
                        currentScreen = Screen.VideoCreator
                    }
                )

                Screen.ExportCenter -> ExportCenterScreen(
                    projectRepository = projectRepository,
                    videoRenderer = videoRenderer,
                    qualityControlManager = qualityControlManager,
                    currentProject = editingProject
                )

                Screen.AiDirector -> AiDirectorScreen(
                    aiStudioAssistant = aiStudioAssistant,
                    onApplyDirectorPreset = { config ->
                        editingProject = config
                        currentScreen = Screen.VideoCreator
                    }
                )

                Screen.MediaLibrary -> MediaLibraryScreen(
                    currentBackgroundType = editingProject?.backgroundType ?: com.example.engine.BackgroundType.PRESET_MOSQUE,
                    onSelectBackground = { type, path ->
                        editingProject = (editingProject ?: ProjectConfig()).copy(
                            backgroundType = type,
                            customBackgroundImagePath = path
                        )
                    }
                )

                Screen.TemplateLibrary -> TemplateLibraryScreen(
                    onUseTemplate = { config ->
                        editingProject = config
                        currentScreen = Screen.VideoCreator
                    }
                )

                Screen.Thumbnail -> ThumbnailEditorScreen(
                    quranRepository = quranRepository,
                    reciterRepository = reciterRepository,
                    fontManager = fontManager,
                    projectRepository = projectRepository,
                    videoRenderer = videoRenderer
                )

                Screen.Batch -> BatchQueueScreen(
                    projectRepository = projectRepository,
                    quranRepository = quranRepository,
                    videoRenderer = videoRenderer
                )

                Screen.AiAssistant -> AiAssistantScreen(
                    aiStudioAssistant = aiStudioAssistant
                )

                Screen.Settings -> SettingsScreen(
                    fontManager = fontManager,
                    projectRepository = projectRepository
                )
            }
        }
    }
}

@Composable
fun StudioBottomNav(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit
) {
    val navItems = listOf(
        Pair(Screen.Dashboard, Icons.Default.Home),
        Pair(Screen.VideoCreator, Icons.Default.Movie),
        Pair(Screen.PreviewStudio, Icons.Default.PlayCircle),
        Pair(Screen.FontManager, Icons.Default.FontDownload),
        Pair(Screen.ExportCenter, Icons.Default.Download),
        Pair(Screen.QuranLibrary, Icons.AutoMirrored.Filled.MenuBook),
        Pair(Screen.AiDirector, Icons.Default.AutoAwesome),
        Pair(Screen.Settings, Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = SurfaceContainerDark,
        contentColor = IslamicGoldPrimary,
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 8.dp
    ) {
        navItems.forEach { (screen, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 9.5.sp,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IslamicEmeraldDark,
                    selectedTextColor = IslamicGoldLight,
                    indicatorColor = IslamicGoldPrimary,
                    unselectedIconColor = IslamicCream.copy(alpha = 0.6f),
                    unselectedTextColor = IslamicCream.copy(alpha = 0.5f)
                )
            )
        }
    }
}
