package ir.sabtnamyar.app.model

/**
 * وضعیت یک ثبت‌نام:
 * - ACTIVE: فعال است و همین حالا می‌توان ثبت‌نام کرد.
 * - UPCOMING: در آینده فعال می‌شود (زمان‌بندی شده).
 * - RETIRED: در گذشته فعال بوده و اکنون بسته است (فقط برای مرجع).
 */
enum class GuideStatus(val id: String, val title: String) {
    ACTIVE("active", "فعال"),
    UPCOMING("upcoming", "به‌زودی"),
    RETIRED("retired", "پایان‌یافته");

    companion object {
        fun fromId(id: String?): GuideStatus = when (id) {
            "upcoming" -> UPCOMING
            "retired" -> RETIRED
            else -> ACTIVE
        }
    }
}

/** یک گام از آموزش تصویری. */
data class Step(
    val number: Int,
    val title: String,
    val body: String,
    val tip: String = "",
    val image: String = ""
)

/** یک راهنمای ثبت‌نام (قدم‌به‌قدم). */
data class Guide(
    val id: String,
    val title: String,
    val category: Category,
    val status: GuideStatus,
    val summary: String = "",
    val url: String = "",
    val fee: String = "",
    val duration: String = "",
    val requirements: List<String> = emptyList(),
    val steps: List<Step> = emptyList(),
    val tags: List<String> = emptyList(),
    val updatedAt: Long = 0L,
    val media: String = ""
)
