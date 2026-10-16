package ir.sabtnamyar.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.sabtnamyar.app.data.GuideRepository
import ir.sabtnamyar.app.model.Category
import ir.sabtnamyar.app.model.Guide
import ir.sabtnamyar.app.model.GuideStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GuideUiState(
    val guides: List<Guide> = emptyList(),
    val visibleGuides: List<Guide> = emptyList(),
    val selectedCategory: Category? = null,
    val statusFilter: GuideStatus? = null,
    val query: String = "",
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val lastUpdate: Long = 0L,
    val message: String? = null
)

class GuideViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = GuideRepository(app)

    private val _state = MutableStateFlow(GuideUiState())
    val state: StateFlow<GuideUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val guides = repo.initialLoad()
            _state.update { it.copy(guides = guides, loading = false, lastUpdate = repo.lastUpdate).applied() }
            if (repo.isStale()) refresh(silent = true)
        }
    }

    fun onQueryChange(q: String) = _state.update { it.copy(query = q).applied() }

    fun onCategorySelected(category: Category?) = _state.update {
        it.copy(selectedCategory = if (it.selectedCategory == category) null else category).applied()
    }

    fun onStatusSelected(status: GuideStatus?) = _state.update {
        it.copy(statusFilter = if (it.statusFilter == status) null else status).applied()
    }

    fun clearMessage() = _state.update { it.copy(message = null) }

    fun setCustomUrl(value: String) {
        repo.customUrl = value
    }

    fun customUrl(): String = repo.customUrl

    /** دریافت ثبت‌نام‌های جدید از گیت‌هاب. */
    fun refresh(silent: Boolean = false) {
        if (_state.value.refreshing) return
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            val result = repo.refresh()
            val guides = repo.initialLoad()
            val message = when {
                result.added > 0 && result.updated > 0 ->
                    "${result.added} ثبت‌نام تازه و ${result.updated} به‌روزرسانی افزوده شد"
                result.added > 0 -> "${result.added} ثبت‌نام تازه افزوده شد"
                result.updated > 0 -> "${result.updated} ثبت‌نام به‌روزرسانی شد"
                !result.networkOk -> "اتصال برقرار نشد؛ محتوای ذخیره‌شده نمایش داده می‌شود"
                else -> "همه‌چیز به‌روز است"
            }
            _state.update {
                it.copy(
                    guides = guides,
                    refreshing = false,
                    lastUpdate = result.lastUpdate,
                    message = if (silent && !result.networkOk) null else message
                ).applied()
            }
        }
    }

    private fun GuideUiState.applied(): GuideUiState {
        val q = query.trim()
        val filtered = guides.filter { g ->
            (selectedCategory == null || g.category == selectedCategory) &&
                (statusFilter == null || g.status == statusFilter) &&
                (q.isBlank() ||
                    g.title.contains(q, ignoreCase = true) ||
                    g.summary.contains(q, ignoreCase = true) ||
                    g.tags.any { it.contains(q, ignoreCase = true) } ||
                    g.steps.any { it.title.contains(q, ignoreCase = true) || it.body.contains(q, ignoreCase = true) })
        }
        return copy(visibleGuides = filtered)
    }
}
