package com.example.data

import android.net.Uri

data class Reciter(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val riwayah: String, // رواية حفص عن عاصم، ورش، etc.
    val serverBaseUrl: String, // e.g. "https://server11.mp3quran.net/sds/"
    val quality: String = "128 kbps",
    val isCustom: Boolean = false,
    val customAudioUri: String? = null
)

class ReciterRepository {

    val reciters: List<Reciter> = listOf(
        Reciter(
            id = "sudais",
            arabicName = "عبد الرحمن السديس",
            englishName = "Abdul Rahman Al-Sudais",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server11.mp3quran.net/sds/"
        ),
        Reciter(
            id = "shuraim",
            arabicName = "سعود الشريم",
            englishName = "Saud Al-Shuraim",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server7.mp3quran.net/shur/"
        ),
        Reciter(
            id = "muaiqly",
            arabicName = "ماهر المعيقلي",
            englishName = "Maher Al-Muaiqly",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server12.mp3quran.net/maher/"
        ),
        Reciter(
            id = "afasy",
            arabicName = "مشاري راشد العفاسي",
            englishName = "Mishary Rashid Alafasy",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server8.mp3quran.net/afs/"
        ),
        Reciter(
            id = "abdulbasit_murattal",
            arabicName = "عبد الباسط عبد الصمد",
            englishName = "Abdul Basit Abdul Samad",
            riwayah = "المصحف المرتل",
            serverBaseUrl = "https://server7.mp3quran.net/basit/"
        ),
        Reciter(
            id = "minshawi_murattal",
            arabicName = "محمد صديق المنشاوي",
            englishName = "Mohamed Siddiq Al-Minshawi",
            riwayah = "المصحف المرتل",
            serverBaseUrl = "https://server10.mp3quran.net/minsh/"
        ),
        Reciter(
            id = "husary",
            arabicName = "محمود خليل الحصري",
            englishName = "Mahmoud Khalil Al-Husary",
            riwayah = "المصحف المرتل برواية حفص",
            serverBaseUrl = "https://server13.mp3quran.net/husr/"
        ),
        Reciter(
            id = "mustafa_ismail",
            arabicName = "مصطفى إسماعيل",
            englishName = "Mustafa Ismail",
            riwayah = "المصحف المرتل",
            serverBaseUrl = "https://server8.mp3quran.net/mustafa/"
        ),
        Reciter(
            id = "fares_abbad",
            arabicName = "فارس عباد",
            englishName = "Fares Abbad",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server8.mp3quran.net/frs_a/"
        ),
        Reciter(
            id = "hani_rifai",
            arabicName = "هاني الرفاعي",
            englishName = "Hani Ar-Rifai",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server8.mp3quran.net/rifai/"
        ),
        Reciter(
            id = "yasser_dosari",
            arabicName = "ياسر الدوسري",
            englishName = "Yasser Al-Dosari",
            riwayah = "المصحف المرتل",
            serverBaseUrl = "https://server11.mp3quran.net/yasser/"
        ),
        Reciter(
            id = "ahmed_ajmi",
            arabicName = "أحمد بن علي العجمي",
            englishName = "Ahmed Al-Ajmi",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server10.mp3quran.net/ajm/"
        ),
        Reciter(
            id = "idrees_abkar",
            arabicName = "إدريس أبكر",
            englishName = "Idrees Abkar",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server6.mp3quran.net/abkr/"
        ),
        Reciter(
            id = "abdullah_basfar",
            arabicName = "عبد الله بصفر",
            englishName = "Abdullah Basfar",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server6.mp3quran.net/bsfr/"
        ),
        Reciter(
            id = "ali_jaber",
            arabicName = "علي جابر",
            englishName = "Ali Jaber",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server11.mp3quran.net/a_jbr/"
        ),
        Reciter(
            id = "shatri",
            arabicName = "أبو بكر الشاطري",
            englishName = "Abu Bakr Al-Shatri",
            riwayah = "حفص عن عاصم",
            serverBaseUrl = "https://server11.mp3quran.net/shatri/"
        )
    )

    fun getAudioUrlForSurah(reciter: Reciter, surahNumber: Int): String {
        if (reciter.isCustom && reciter.customAudioUri != null) {
            return reciter.customAudioUri
        }
        val formattedNum = surahNumber.toString().padStart(3, '0')
        return "${reciter.serverBaseUrl}$formattedNum.mp3"
    }

    fun searchReciters(query: String): List<Reciter> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return reciters
        return reciters.filter {
            it.arabicName.contains(q) ||
            it.englishName.lowercase().contains(q) ||
            it.riwayah.contains(q)
        }
    }
}
