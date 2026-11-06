package dev.myaring.mobile_butler.domain.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.repository.GoalRepository
import dev.myaring.mobile_butler.ui.navigation.EGoalTab
import dev.myaring.mobile_butler.ui.navigation.GoalScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * [C-5] 목표 화면 ViewModel.
 */
class GoalScreenViewModel(
    private val goalRepository: GoalRepository? = null,
) : ViewModel(), ITopBarViewModel {

    private val _title = mutableStateOf("목표")
    override val title: State<String> = _title

    private val _returnSubTabOrdinal = mutableIntStateOf(-1)
    override val showBackButton: State<Boolean> =
        derivedStateOf { _returnSubTabOrdinal.value >= 0 }

    private val _uiState = MutableStateFlow(
        GoalScreenUiState(
            goals = defaultGoals()
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        goalRepository?.let { repo ->
            viewModelScope.launch {
                repo.observeGoals().collect { list ->
                    _uiState.update { it.copy(goals = list) }
                }
            }
        }
    }

    val currentTab: EGoalTab
        get() = uiState.value.selectedTab

    override fun onBackTab() {
        when (_returnSubTabOrdinal.value) {
            0 -> onChangeTabToList()
            1 -> onChangeTabToArchive()
        }
    }

    fun onChangeTabToList() {
        _returnSubTabOrdinal.value = -1
        onChangeTab(EGoalTab.LIST)
    }

    fun onChangeTabToDetail(goalId: String? = null) {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        if (goalId != null) {
            _uiState.update { it.copy(selectedGoalId = goalId) }
        }
        onChangeTab(EGoalTab.DETAIL)
    }

    fun onChangeTabToCreation() {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        onChangeTab(EGoalTab.CREATION)
    }

    fun onChangeTabToArchive() {
        _returnSubTabOrdinal.value = -1
        onChangeTab(EGoalTab.ARCHIVE)
    }

    fun selectGoal(goalId: String) {
        _uiState.update { it.copy(selectedGoalId = goalId) }
        onChangeTabToDetail(goalId)
    }

    private fun onChangeTab(tab: EGoalTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        _title.value = when (tab) {
            EGoalTab.LIST -> "목표"
            EGoalTab.DETAIL -> "목표 상세"
            EGoalTab.CREATION -> "목표 만들기"
            EGoalTab.ARCHIVE -> "목표 보관함"
        }
    }

    companion object {
        fun defaultGoals(): List<Goal> = listOf(
            Goal(
                id = "sample-1",
                title = "코틀린 완벽 가이드",
                type = GoalType.CUMULATIVE,
                targetValue = 100.0,
                unit = "쪽",
                startDate = LocalDate.now(),
                dueDate = LocalDate.now().plusDays(10),
                priority = 0,
                status = GoalStatus.ACTIVE
            ),
            Goal(
                id = "sample-2",
                title = "모바일 집사 프로젝트",
                type = GoalType.CUMULATIVE,
                targetValue = 20.0,
                unit = "커밋",
                startDate = LocalDate.now(),
                dueDate = LocalDate.now().plusDays(7),
                priority = 1,
                status = GoalStatus.ACTIVE
            ),
            Goal(
                id = "sample-3",
                title = "매일 아침 달리기",
                type = GoalType.RECURRING,
                targetValue = 5.0,
                unit = "km",
                startDate = LocalDate.now(),
                dueDate = null,
                priority = 2,
                status = GoalStatus.ACTIVE
            )
        )
    }
}
