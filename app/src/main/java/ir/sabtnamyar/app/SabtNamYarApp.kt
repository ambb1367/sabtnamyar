package ir.sabtnamyar.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.sabtnamyar.app.ui.GuideDetailScreen
import ir.sabtnamyar.app.ui.HomeScreen
import ir.sabtnamyar.app.ui.SabtNamYarTheme
import ir.sabtnamyar.app.ui.SettingsScreen
import ir.sabtnamyar.app.viewmodel.GuideViewModel

private sealed interface Screen {
    data object Home : Screen
    data class Detail(val id: String) : Screen
    data object Settings : Screen
}

@Composable
fun SabtNamYarApp() {
    SabtNamYarTheme {
        val vm: GuideViewModel = viewModel()
        val state by vm.state.collectAsState()
        var screen by remember { mutableStateOf<Screen>(Screen.Home) }

        BackHandler(enabled = screen != Screen.Home) {
            screen = Screen.Home
        }

        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                if (targetState == Screen.Home) {
                    slideInHorizontally { -it / 4 } + fadeIn() togetherWith
                        slideOutHorizontally { it / 4 } + fadeOut()
                } else {
                    slideInHorizontally { it / 4 } + fadeIn() togetherWith
                        slideOutHorizontally { -it / 4 } + fadeOut()
                }
            },
            label = "nav"
        ) { current ->
            when (current) {
                is Screen.Home -> HomeScreen(
                    state = state,
                    onQueryChange = vm::onQueryChange,
                    onCategorySelected = vm::onCategorySelected,
                    onStatusSelected = vm::onStatusSelected,
                    onRefresh = { vm.refresh() },
                    onOpenSettings = { screen = Screen.Settings },
                    onOpenGuide = { screen = Screen.Detail(it) },
                    onMessageShown = vm::clearMessage
                )

                is Screen.Detail -> {
                    val guide = state.guides.firstOrNull { it.id == current.id }
                    if (guide != null) {
                        GuideDetailScreen(guide = guide, onBack = { screen = Screen.Home })
                    } else {
                        LaunchedEffect(current.id) { screen = Screen.Home }
                    }
                }

                is Screen.Settings -> SettingsScreen(
                    currentUrl = vm.customUrl(),
                    onBack = { screen = Screen.Home },
                    onSave = { vm.setCustomUrl(it) }
                )
            }
        }
    }
}
