package dev.myaring.mobile_butler.domain.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.pacing.GoalPriorityRule
import dev.myaring.mobile_butler.ui.navigation.EGoalTab
import dev.myaring.mobile_butler.ui.navigation.GoalScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GoalScreenViewModel : ViewModel(), ITopBarViewModel {

    private val _title = mutableStateOf("목표")
    override val title: State<String> = _title

    private val _returnSubTabOrdinal = mutableIntStateOf(-1)
    override val showBackButton: State<Boolean> =
        derivedStateOf { _returnSubTabOrdinal.value >= 0 }

    private val _uiState = MutableStateFlow(GoalScreenUiState())
    public val uiState = _uiState.asStateFlow()

    public val currentTab: EGoalTab
        get() = uiState.value.selectedTab

    public override fun onBackTab() {
        when (_returnSubTabOrdinal.value) {
            0 -> onChangeTabToList()
            1 -> onChangeTabToArchive()
        }
    }

    public fun onChangeTabToList() {
        _returnSubTabOrdinal.value = -1
        this.onChangeTab(EGoalTab.LIST)
    }

    public fun onChangeTabToDetail() {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        this.onChangeTab(EGoalTab.DETAIL)
    }

    public fun onChangeTabToCreation() {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        this.onChangeTab(EGoalTab.CREATION)
    }

    public fun onChangeTabToArchive() {
        _returnSubTabOrdinal.value = -1
        this.onChangeTab(EGoalTab.ARCHIVE)
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

    fun setGoals(goals: List<Goal>) {
        val normalized = GoalPriorityRule.normalize(goals)
        _uiState.update { it.copy(goals = normalized) }
    }

    fun moveGoalUp(index: Int) {
        val currentGoals = _uiState.value.goals
        val reordered = GoalPriorityRule.moveUp(currentGoals, index)
        _uiState.update { it.copy(goals = reordered) }
    }

    fun moveGoalDown(index: Int) {
        val currentGoals = _uiState.value.goals
        val reordered = GoalPriorityRule.moveDown(currentGoals, index)
        _uiState.update { it.copy(goals = reordered) }
    }

    fun reorderGoals(fromIndex: Int, toIndex: Int) {
        val currentGoals = _uiState.value.goals
        val reordered = GoalPriorityRule.reorder(currentGoals, fromIndex, toIndex)
        _uiState.update { it.copy(goals = reordered) }
    }
}
