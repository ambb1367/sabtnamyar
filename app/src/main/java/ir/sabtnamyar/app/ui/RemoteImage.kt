package ir.sabtnamyar.app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ir.sabtnamyar.app.util.Http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val memoryCache = mutableMapOf<String, Bitmap>()

/**
 * نمایش تصویر راهنما بدون کتابخانه بیرونی؛ تصویر از نشانی اینترنتی دریافت و
 * در حافظه نگه‌داری می‌شود. در صورت نبود تصویر چیزی نمایش داده نمی‌شود.
 */
@Composable
fun StepImage(url: String, modifier: Modifier = Modifier) {
    if (url.isBlank()) return
    var bitmap by remember(url) { mutableStateOf(memoryCache[url]) }

    LaunchedEffect(url) {
        if (bitmap == null) {
            val loaded = withContext(Dispatchers.IO) {
                Http.getBytes(url)?.let { bytes ->
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            }
            if (loaded != null) {
                memoryCache[url] = loaded
                bitmap = loaded
            }
        }
    }

    val bmp = bitmap
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "در حال دریافت تصویر…",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
