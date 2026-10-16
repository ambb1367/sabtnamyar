package ir.sabtnamyar.app.data

import android.content.Context
import ir.sabtnamyar.app.model.Guide
import ir.sabtnamyar.app.model.GuideStatus
import ir.sabtnamyar.app.model.Category
import ir.sabtnamyar.app.model.Step
import ir.sabtnamyar.app.util.Http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class RefreshResult(
    val added: Int,
    val updated: Int,
    val total: Int,
    val networkOk: Boolean,
    val lastUpdate: Long
)

/**
 * مخزن راهنماهای ثبت‌نام. سه لایه:
 *  ۱) بذر همراه برنامه (assets/seed_guides.json) — کاملاً آفلاین
 *  ۲) حافظه محلی (آخرین نسخه دریافت‌شده) — روی بذر سوار می‌شود
 *  ۳) فایل JSON راه دور در گیت‌هاب — با دکمه «به‌روزرسانی» دریافت و ادغام می‌شود
 */
class GuideRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("guides", Context.MODE_PRIVATE)

    /** نشانی JSON دلخواه؛ خالی یعنی استفاده از آینه‌های پیش‌فرض. */
    var customUrl: String
        get() = prefs.getString(KEY_REMOTE_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_REMOTE_URL, value.trim()).apply()

    val lastUpdate: Long get() = prefs.getLong(KEY_LAST_UPDATE, 0L)

    private fun mirrors(): List<String> {
        val custom = customUrl.trim()
        return if (custom.isNotEmpty()) listOf(custom) else DEFAULT_MIRRORS
    }

    suspend fun initialLoad(): List<Guide> = withContext(Dispatchers.IO) {
        merge(loadSeed(), loadCache())
    }

    /** دریافت ثبت‌نام‌های جدید از گیت‌هاب و ادغام آن‌ها با محتوای موجود. */
    suspend fun refresh(): RefreshResult = withContext(Dispatchers.IO) {
        val existing = merge(loadSeed(), loadCache())
        for (mirror in mirrors()) {
            if (mirror.isBlank()) continue
            val raw = Http.get(mirror) ?: continue
            val remote = parseGuides(raw)
            if (remote.isEmpty()) continue
            val merged = merge(existing, remote)
            saveCache(merged)
            val existingIds = existing.map { it.id }.toSet()
            val added = merged.count { it.id !in existingIds }
            val updated = merged.count { g -> existing.any { it.id == g.id && it.updatedAt < g.updatedAt } }
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_UPDATE, now).apply()
            return@withContext RefreshResult(added, updated, merged.size, true, now)
        }
        RefreshResult(0, 0, existing.size, false, lastUpdate)
    }

    /** اگر از آخرین به‌روزرسانی بیش از [hours] ساعت گذشته باشد، خودکار به‌روزرسانی می‌کند. */
    fun isStale(hours: Int = 24): Boolean =
        System.currentTimeMillis() - lastUpdate > hours * 60L * 60L * 1000L

    private fun merge(a: List<Guide>, b: List<Guide>): List<Guide> {
        val map = LinkedHashMap<String, Guide>()
        for (g in a) map[g.id] = g
        for (g in b) map[g.id] = g
        return map.values.sortedWith(
            compareByDescending<Guide> { it.status == GuideStatus.ACTIVE }
                .thenByDescending { it.status == GuideStatus.UPCOMING }
                .thenByDescending { it.updatedAt }
                .thenBy { it.title }
        )
    }

    private fun loadSeed(): List<Guide> = try {
        val raw = context.assets.open("seed_guides.json").bufferedReader().use { it.readText() }
        parseGuides(raw)
    } catch (_: Exception) {
        emptyList()
    }

    private fun loadCache(): List<Guide> {
        val file = java.io.File(context.filesDir, CACHE_FILE)
        if (!file.exists()) return emptyList()
        return try {
            parseGuides(file.readText(Charsets.UTF_8))
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveCache(guides: List<Guide>) {
        try {
            java.io.File(context.filesDir, CACHE_FILE)
                .writeText(serializeGuides(guides), Charsets.UTF_8)
        } catch (_: Exception) {
            // ignore
        }
    }

    private fun parseGuides(json: String): List<Guide> = try {
        val trimmed = json.trim()
        val arr = if (trimmed.startsWith("{")) {
            JSONObject(trimmed).optJSONArray("guides") ?: JSONArray()
        } else {
            JSONArray(trimmed)
        }
        (0 until arr.length()).mapNotNull { i ->
            arr.optJSONObject(i)?.let { parseGuide(it) }
        }.filter { it.id.isNotBlank() && it.title.isNotBlank() }
    } catch (_: Exception) {
        emptyList()
    }

    private fun parseGuide(o: JSONObject): Guide = Guide(
        id = o.optString("id"),
        title = o.optString("title"),
        category = Category.fromId(o.optString("category")),
        status = GuideStatus.fromId(o.optString("status", "active")),
        summary = o.optString("summary"),
        url = o.optString("url"),
        fee = o.optString("fee"),
        duration = o.optString("duration"),
        requirements = o.optJSONArray("requirements").toStringList(),
        steps = parseSteps(o.optJSONArray("steps")),
        tags = o.optJSONArray("tags").toStringList(),
        updatedAt = o.optLong("updatedAt"),
        media = o.optString("media")
    )

    private fun parseSteps(arr: JSONArray?): List<Step> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            Step(
                number = o.optInt("number", i + 1),
                title = o.optString("title"),
                body = o.optString("body"),
                tip = o.optString("tip"),
                image = o.optString("image")
            )
        }.filter { it.title.isNotBlank() || it.body.isNotBlank() }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optString(it).takeIf { s -> s.isNotBlank() } }
    }

    private fun serializeGuides(guides: List<Guide>): String {
        val root = JSONObject()
        root.put("updatedAt", System.currentTimeMillis())
        val arr = JSONArray()
        for (g in guides) {
            arr.put(
                JSONObject().apply {
                    put("id", g.id)
                    put("title", g.title)
                    put("category", g.category.id)
                    put("status", g.status.id)
                    put("summary", g.summary)
                    put("url", g.url)
                    put("fee", g.fee)
                    put("duration", g.duration)
                    put("requirements", JSONArray(g.requirements))
                    put("tags", JSONArray(g.tags))
                    put("updatedAt", g.updatedAt)
                    put("media", g.media)
                    put(
                        "steps",
                        JSONArray().apply {
                            for (s in g.steps) put(
                                JSONObject().apply {
                                    put("number", s.number)
                                    put("title", s.title)
                                    put("body", s.body)
                                    put("tip", s.tip)
                                    put("image", s.image)
                                }
                            )
                        }
                    )
                }
            )
        }
        root.put("guides", arr)
        return root.toString()
    }

    companion object {
        private const val CACHE_FILE = "guides_cache.json"
        private const val KEY_REMOTE_URL = "remote_url"
        private const val KEY_LAST_UPDATE = "last_update"

        /**
         * آینه‌های محتوای ثبت‌نام‌ها؛ raw گیت‌هاب، سپس CDN jsDelivr و در پایان GitHub Pages.
         * برای مخزن خودتان مقدار GITHUB_OWNER/GITHUB_REPO را تغییر دهید.
         */
        const val GITHUB_OWNER = "ambb1367"
        const val GITHUB_REPO = "sabtnamyar"

        val DEFAULT_MIRRORS = listOf(
            "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/main/docs/registrations.json",
            "https://cdn.jsdelivr.net/gh/$GITHUB_OWNER/$GITHUB_REPO@main/docs/registrations.json",
            "https://$GITHUB_OWNER.github.io/$GITHUB_REPO/registrations.json"
        )
    }
}
