package ir.sabtnamyar.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ir.sabtnamyar.app.model.GuideStatus
import ir.sabtnamyar.app.util.PersianDate

object Format {
    fun updated(epoch: Long): String {
        val jalali = PersianDate.fromEpochMillis(epoch)
        return if (jalali.isBlank()) "—" else jalali
    }
}

object Intents {
    fun openUrl(context: Context, url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
        }
    }

    fun share(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری"))
        } catch (_: Exception) {
        }
    }

    fun copy(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("متن", text))
        } catch (_: Exception) {
        }
    }
}

@Composable
fun statusColor(status: GuideStatus): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        GuideStatus.ACTIVE -> scheme.secondary to scheme.onSecondary
        GuideStatus.UPCOMING -> scheme.tertiary to scheme.surface
        GuideStatus.RETIRED -> scheme.outline to scheme.onSurface
    }
}

@Composable
fun categoryChipColors(): Pair<Color, Color> =
    MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
