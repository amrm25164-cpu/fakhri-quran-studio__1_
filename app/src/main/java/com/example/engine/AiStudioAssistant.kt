package com.example.engine

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiStudioAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemInstruction = """
        أنت المساعد الذكي لاستوديو فخري للقرآن الكريم ألترا (Fakhri Quran Studio ULTRA).
        مهمتك مساعدة صانعي المحتوى القرآني في:
        1. اقتراح أفكار العناوين الإبداعية لفيديوهات القرآن الكريم.
        2. كتابة الوصف والوسوم (Hashtags) المناسبة لمنصات التواصل (يوتيوب، تيك توك، إنستغرام).
        3. اقتراح تناغم الخطوط والألوان والخلفيات البصرية التي تليق بجلال القرآن الكريم.
        4. تقديم نصائح إعدادات التصدير المثلى (9:16 أو 16:9، 30fps أو 60fps).
        
        قواعد إلزامية وأخلاقية صارمة:
        - لا تقم بتعديل أو إعادة صياغة أي آية قرآنية إطلاقاً.
        - لا تخترع أحاديث نبوية أو تفاسير غير معتمدة.
        - لا تقترح تقليد أصوات القراء بالذكاء الاصطناعي.
        - ركز على الجمال البصري والهندسي والتقني والإسلامي.
    """.trimIndent()

    suspend fun askAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineSmartResponse(prompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.w("AiStudioAssistant", "Gemini API error code: ${response.code}")
                return@withContext getOfflineSmartResponse(prompt)
            }

            val jsonResp = JSONObject(responseString)
            val candidates = jsonResp.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text")
                }
            }
            getOfflineSmartResponse(prompt)
        } catch (e: Exception) {
            Log.e("AiStudioAssistant", "Error querying Gemini", e)
            getOfflineSmartResponse(prompt)
        }
    }

    /**
     * Highly rich offline domain response for immediate high-quality recommendations.
     */
    private fun getOfflineSmartResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("عنوان") || p.contains("title") || p.contains("يوتيوب") -> {
                """
                🌟 اقتراحات عناوين احترافية لفيديوهات القرآن:
                1. ﴿ وَبِالْحَقِّ أَنزَلْنَاهُ ﴾ | تلاوة خاشعة تريح القلوب المتعبة بصوت القارئ
                2. سورة الفاتحة كاملة بخشوع يلامس الروح | علاج الضيق والهم
                3. آيات السكينة والطمأنينة لدفع القلق والوسواس | جودة عالية 4K
                4. تلاوة هادئة قبل النوم لراحة النفس والسكينة في المنزل
                5. روائع التلاوات الخاشعة | سورة تفيض نوراً وبركة
                """.trimIndent()
            }
            p.contains("وسوم") || p.contains("هاشتاق") || p.contains("وصف") -> {
                """
                🏷️ الوسوم الأكثر انتشاراً (Hashtags):
                #قرآن #قرآن_كريم #تلاوة_خاشعة #سورة_الفاتحة #quran #راحة_نفسية #إسلاميات #اكسبلور_فولو #shorts #reels

                📝 نموذج وصف الفيديو:
                تلاوة خاشعة من آيات الذكر الحكيم بصوت عذب يملأ النفس سكينة واطمئناناً.
                تصميم وإنتاج: استوديو فخري للقرآن ألترا.
                اللهم اجعل القرآن العظيم ربيع قلوبنا ونور صدورنا وجلاء أحزاننا وذهاب همومنا.
                """.trimIndent()
            }
            p.contains("خط") || p.contains("font") || p.contains("تناغم") -> {
                """
                ✒️ توصيات تناغم الخطوط في الاستوديو:
                • لنص الآيات الكريمة: نوصي بـ (خط أميري) أو (خط شهرزاد الجديد العثماني) لضبط الرسم والتشكيل بدقة.
                • للعناوين وشارات السور: نوصي بـ (خط كايرو العريض) أو (خط ريم الكوفي) لإضفاء لمسة جمالية وفخامة.
                • للترجمة والتفسير: نوصي بـ (خط كايرو العادي) أو (خط نوتو نسخ) لسهولة القراءة باللغات الأجنبية.
                """.trimIndent()
            }
            p.contains("لون") || p.contains("palette") || p.contains("ثيم") -> {
                """
                🎨 لوحات ألوان إسلامية ملكية مقترحة:
                1. ثيم "سكينة الفجر":
                   - الخلفية: أخضر زمردي داكن (#081715)
                   - النص: ذهبي لؤلؤي كريمي (#FFFDF7)
                   - الزخارف والشارات: ذهب ملكي عيار 24 (#D4AF37)
                
                2. ثيم "جلال الليل والنجوم":
                   - الخلفية: كحلي ملكي ياقوتي (#06101E)
                   - النص: أبيض ناصع مع ظل ناعم (#FFFFFF)
                   - الزخارف: فضي قمري وأزرق مائي (#90CAF9)
                """.trimIndent()
            }
            else -> {
                """
                مرحباً بك في استوديو فخري للقرآن ألترا! 🎙️
                أنا هنا لمساعدتك في تصميم أفضل مقاطع القرآن الكريم.
                يمكنني مساعدتك في:
                • اقتراح عناوين مميزة للفيديوهات وقوائم التشغيل.
                • كتابة وصف مخصص ووسوم قوية لزيادة الانتشار.
                • اختيار أفضل الخطوط والتنسيقات للآيات الكريمة.
                • اختيار أفضل دقة (1080p بمعدل 30 أو 60 إطاراً في الثانية).
                """.trimIndent()
            }
        }
    }
}
