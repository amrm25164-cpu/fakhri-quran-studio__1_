package com.example.data

import android.content.Context
import com.example.engine.AspectRatio
import com.example.engine.BackgroundType
import com.example.engine.ProjectConfig
import com.example.engine.Resolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class BatchJob(
    val id: String,
    val title: String,
    val surahNumber: Int,
    val startAyah: Int,
    val endAyah: Int,
    val status: BatchStatus = BatchStatus.QUEUED,
    val progress: Int = 0,
    val outputFilePath: String? = null,
    val error: String? = null
)

enum class BatchStatus(val label: String) {
    QUEUED("في الانتظار"),
    RENDERING("جاري التصدير"),
    COMPLETED("مكتمل"),
    FAILED("تعذر التصدير"),
    CANCELLED("ملغي")
}

class ProjectRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("fakhri_projects_prefs", Context.MODE_PRIVATE)

    private val _projects = MutableStateFlow<List<ProjectConfig>>(emptyList())
    val projects: StateFlow<List<ProjectConfig>> = _projects.asStateFlow()

    private val _batchQueue = MutableStateFlow<List<BatchJob>>(emptyList())
    val batchQueue: StateFlow<List<BatchJob>> = _batchQueue.asStateFlow()

    init {
        loadProjects()
        initPresetBatchJobsIfEmpty()
    }

    private fun loadProjects() {
        val list = mutableListOf<ProjectConfig>()
        val saved = prefs.getString("projects_json", null)
        if (saved != null) {
            try {
                val array = JSONArray(saved)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ProjectConfig(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            surahNumber = obj.getInt("surahNumber"),
                            startAyah = obj.getInt("startAyah"),
                            endAyah = obj.getInt("endAyah"),
                            reciterId = obj.optString("reciterId", "afasy"),
                            aspectRatio = AspectRatio.valueOf(obj.optString("aspectRatio", "PORTRAIT_9_16")),
                            resolution = Resolution.valueOf(obj.optString("resolution", "RES_720P")),
                            backgroundType = BackgroundType.valueOf(obj.optString("backgroundType", "PRESET_MOSQUE")),
                            showTranslation = obj.optBoolean("showTranslation", true),
                            showTafsir = obj.optBoolean("showTafsir", false),
                            showIslamicFrame = obj.optBoolean("showIslamicFrame", true),
                            verseDurationSeconds = obj.optInt("verseDurationSeconds", 6),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                // Parse error fallback
            }
        }

        if (list.isEmpty()) {
            // Default initial starter projects
            list.add(
                ProjectConfig(
                    id = "proj_fatiha_reels",
                    title = "سورة الفاتحة كاملة (ريلز 9:16)",
                    surahNumber = 1,
                    startAyah = 1,
                    endAyah = 7,
                    reciterId = "afasy",
                    aspectRatio = AspectRatio.PORTRAIT_9_16,
                    backgroundType = BackgroundType.PRESET_MOSQUE
                )
            )
            list.add(
                ProjectConfig(
                    id = "proj_mulk_youtube",
                    title = "أوائل سورة الملك (يوتيوب عريض)",
                    surahNumber = 67,
                    startAyah = 1,
                    endAyah = 5,
                    reciterId = "sudais",
                    aspectRatio = AspectRatio.LANDSCAPE_16_9,
                    backgroundType = BackgroundType.PRESET_STARS
                )
            )
            list.add(
                ProjectConfig(
                    id = "proj_ikhlas_square",
                    title = "سورة الإخلاص (مربع إنستغرام)",
                    surahNumber = 112,
                    startAyah = 1,
                    endAyah = 4,
                    reciterId = "minshawi_murattal",
                    aspectRatio = AspectRatio.SQUARE_1_1,
                    backgroundType = BackgroundType.PRESET_MOSQUE
                )
            )
        }

        _projects.value = list
    }

    private fun persistProjects(list: List<ProjectConfig>) {
        val array = JSONArray()
        list.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("title", p.title)
                put("surahNumber", p.surahNumber)
                put("startAyah", p.startAyah)
                put("endAyah", p.endAyah)
                put("reciterId", p.reciterId)
                put("aspectRatio", p.aspectRatio.name)
                put("resolution", p.resolution.name)
                put("backgroundType", p.backgroundType.name)
                put("showTranslation", p.showTranslation)
                put("showTafsir", p.showTafsir)
                put("showIslamicFrame", p.showIslamicFrame)
                put("verseDurationSeconds", p.verseDurationSeconds)
                put("createdAt", p.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString("projects_json", array.toString()).apply()
    }

    fun saveProject(project: ProjectConfig) {
        val current = _projects.value.toMutableList()
        val index = current.indexOfFirst { it.id == project.id }
        if (index != -1) {
            current[index] = project
        } else {
            current.add(0, project)
        }
        _projects.value = current
        persistProjects(current)
    }

    fun duplicateProject(project: ProjectConfig): ProjectConfig {
        val copy = project.copy(
            id = "proj_${System.currentTimeMillis()}",
            title = "${project.title} (نسخة)",
            createdAt = System.currentTimeMillis()
        )
        saveProject(copy)
        return copy
    }

    fun deleteProject(projectId: String) {
        val updated = _projects.value.filter { it.id != projectId }
        _projects.value = updated
        persistProjects(updated)
    }

    // Batch Queue Operations
    private fun initPresetBatchJobsIfEmpty() {
        if (_batchQueue.value.isEmpty()) {
            _batchQueue.value = listOf(
                BatchJob("batch_1", "سورة الفلق (1 - 5)", 113, 1, 5, BatchStatus.QUEUED),
                BatchJob("batch_2", "سورة الناس (1 - 6)", 114, 1, 6, BatchStatus.QUEUED),
                BatchJob("batch_3", "سورة الكوثر (1 - 3)", 108, 1, 3, BatchStatus.QUEUED)
            )
        }
    }

    fun addBatchJob(job: BatchJob) {
        _batchQueue.value = _batchQueue.value + job
    }

    fun updateBatchJobStatus(jobId: String, status: BatchStatus, progress: Int = 0, outputPath: String? = null, error: String? = null) {
        _batchQueue.value = _batchQueue.value.map {
            if (it.id == jobId) {
                it.copy(status = status, progress = progress, outputFilePath = outputPath, error = error)
            } else it
        }
    }

    fun removeBatchJob(jobId: String) {
        _batchQueue.value = _batchQueue.value.filter { it.id != jobId }
    }
}
