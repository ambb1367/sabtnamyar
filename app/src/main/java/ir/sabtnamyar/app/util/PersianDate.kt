package ir.sabtnamyar.app.util

/**
 * تبدیل تاریخ میلادی به شمسی (الگوریتم jalaali) برای نمایش تاریخ آخرین به‌روزرسانی.
 */
object PersianDate {

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    fun fromEpochMillis(millis: Long): String {
        if (millis <= 0L) return ""
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        val (jy, jm, jd) = fromGregorian(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        return "$jd ${monthName(jm)} $jy"
    }

    private fun monthName(m: Int): String = when (m) {
        1 -> "فروردین"; 2 -> "اردیبهشت"; 3 -> "خرداد"; 4 -> "تیر"
        5 -> "مرداد"; 6 -> "شهریور"; 7 -> "مهر"; 8 -> "آبان"
        9 -> "آذر"; 10 -> "دی"; 11 -> "بهمن"; else -> "اسفند"
    }

    private data class JalCal(val leap: Int, val march: Int)

    private fun fromGregorian(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val jdn = g2d(gy, gm, gd)
        var jy = d2g(jdn).first - 621
        val r = jalCal(jy)
        val jdn1f = g2d(d2g(jdn).first, 3, r.march)
        var k = jdn - jdn1f
        if (k in 0..185) return Triple(jy, 1 + div(k, 31), mod(k, 31) + 1)
        if (k >= 0) {
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        return Triple(jy, 7 + div(k, 30), mod(k, 30) + 1)
    }

    private fun jalCal(jy: Int): JalCal {
        val bl = breaks.size
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until bl) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
            jp = jm
        }
        var n = jy - jp
        leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1
        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
        var leap = mod(mod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4
        return JalCal(leap, march)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int =
        div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
            div(153 * mod(gm + 9, 12) + 2, 5) + gd - 34840408

    private fun d2g(jdn: Int): Triple<Int, Int, Int> {
        var j = 4 * jdn + 139361631
        j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = div(mod(j, 1461), 4) * 5 + 308
        val gd = div(mod(i, 153), 5) + 1
        val gm = mod(div(i, 153), 12) + 1
        val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
        return Triple(gy, gm, gd)
    }

    private fun div(a: Int, b: Int): Int = Math.floor(a.toDouble() / b).toInt()

    private fun mod(a: Int, b: Int): Int = a - Math.floor(a.toDouble() / b).toInt() * b
}
