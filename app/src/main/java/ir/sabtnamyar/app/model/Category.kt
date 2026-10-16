package ir.sabtnamyar.app.model

/**
 * دسته‌بندی ثبت‌نام‌ها. هر دسته یک شناسه پایدار (برای فایل JSON) و یک عنوان فارسی دارد.
 */
class Category(
    val id: String,
    val title: String,
    val emoji: String
) {
    override fun equals(other: Any?): Boolean = other is Category && other.id == id
    override fun hashCode(): Int = id.hashCode()

    companion object {
        val CIVIL = Category("civil", "هویت و ثبت احوال", "\uD83E\uDEAA")
        val INSURANCE = Category("insurance", "بیمه و تأمین اجتماعی", "\uD83D\uDEE1")
        val HEALTH = Category("health", "سلامت و درمان", "\uD83E\uDE7A")
        val TAX = Category("tax", "مالیات و دارایی", "\uD83E\uDDFE")
        val BANKING = Category("banking", "بانک و بازار سرمایه", "\uD83C\uDFE6")
        val VEHICLE = Category("vehicle", "خودرو و راهنمایی", "\uD83D\uDE97")
        val MILITARY = Category("military", "نظام وظیفه", "\uD83C\uDF96")
        val PASSPORT = Category("passport", "گذرنامه و امور کنسولی", "\uD83D\uDEE2")
        val HOUSING = Category("housing", "مسکن و املاک", "\uD83C\uDFE0")
        val EDUCATION = Category("education", "آموزش و آزمون", "\uD83C\uDF93")
        val JOB = Category("job", "اشتغال و کاریابی", "\uD83D\uDCBC")
        val JUDICIAL = Category("judicial", "قضایی و انتظامی", "\u2696")
        val URBAN = Category("urban", "خدمات شهری", "\uD83C\uDFD9")
        val BUSINESS = Category("business", "کسب‌وکار و اصناف", "\uD83C\uDFEA")
        val WELFARE = Category("welfare", "رفاه و یارانه", "\uD83E\uDD32")
        val TRAVEL = Category("travel", "سفر و زیارت", "\uD83D\uDD4C")

        val ALL = listOf(
            CIVIL, INSURANCE, HEALTH, TAX, BANKING, VEHICLE, MILITARY, PASSPORT,
            HOUSING, EDUCATION, JOB, JUDICIAL, URBAN, BUSINESS, WELFARE, TRAVEL
        )

        fun fromId(id: String?): Category =
            ALL.firstOrNull { it.id == id } ?: Category(id ?: "other", "سایر", "\uD83D\uDCCC")
    }
}
