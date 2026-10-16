package ir.sabtnamyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.sabtnamyar.app.model.Guide
import ir.sabtnamyar.app.model.Step

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideDetailScreen(
    guide: Guide,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("راهنمای ثبت‌نام", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = { Intents.share(context, shareText(guide)) }) {
                        Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (guide.media.isNotBlank()) {
                item {
                    StepImage(guide.media, modifier = Modifier.clip(RoundedCornerShape(16.dp)))
                }
            }

            item { Header(guide) }

            if (guide.requirements.isNotEmpty()) {
                item { RequirementsCard(guide.requirements) }
            }

            if (guide.steps.isNotEmpty()) {
                item {
                    Text(
                        "مراحل ثبت‌نام",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(guide.steps.size) { index ->
                    StepCard(guide.steps[index], isLast = index == guide.steps.lastIndex)
                }
            }

            if (guide.url.isNotBlank()) {
                item {
                    Button(
                        onClick = { Intents.openUrl(context, guide.url) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("رفتن به سامانه ثبت‌نام")
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { Intents.copy(context, shareText(guide)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("کپی متن راهنما")
                }
            }

            item {
                Text(
                    text = "این راهنما برای اطلاع‌رسانی تهیه شده است؛ در صورت تغییر رویه سامانه‌ها، " +
                        "دکمه «به‌روزرسانی» را در صفحه اصلی بزنید.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Header(guide: Guide) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Badge(
                text = "${guide.category.emoji} ${guide.category.title}",
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer
            )
            val (bg, fg) = statusColor(guide.status)
            Badge(text = guide.status.title, container = bg, content = fg)
        }

        Spacer(Modifier.padding(top = 10.dp))

        Text(
            text = guide.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (guide.summary.isNotBlank()) {
            Spacer(Modifier.padding(top = 6.dp))
            Text(
                text = guide.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.padding(top = 10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (guide.duration.isNotBlank()) MetaItem("زمان تقریبی", guide.duration)
            if (guide.fee.isNotBlank()) MetaItem("هزینه", guide.fee)
            MetaItem("تعداد گام", "${guide.steps.size}")
        }

        if (guide.updatedAt > 0) {
            Spacer(Modifier.padding(top = 6.dp))
            Text(
                text = "آخرین به‌روزرسانی: ${Format.updated(guide.updatedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetaItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RequirementsCard(items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("مدارک و پیش‌نیازها", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.padding(top = 6.dp))
            for (item in items) {
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text("•  ", style = MaterialTheme.typography.bodyMedium)
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun StepCard(step: Step, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${step.number}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 48.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (step.body.isNotBlank()) {
                    Spacer(Modifier.padding(top = 6.dp))
                    Text(
                        text = step.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (step.image.isNotBlank()) {
                    Spacer(Modifier.padding(top = 10.dp))
                    StepImage(step.image, modifier = Modifier.clip(RoundedCornerShape(12.dp)))
                }
                if (step.tip.isNotBlank()) {
                    Spacer(Modifier.padding(top = 10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "نکته: ${step.tip}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun shareText(guide: Guide): String {
    val steps = guide.steps.joinToString("\n") { "${it.number}. ${it.title}\n   ${it.body}" }
    return "${guide.title}\n${guide.summary}\n\n$steps\n\n${guide.url}"
}
