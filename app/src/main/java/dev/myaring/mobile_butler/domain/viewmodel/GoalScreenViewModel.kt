package dev.myaring.mobile_butler.domain.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.myaring.mobile_butler.domain.model.Cadence
import dev.myaring.mobile_butler.domain.model.Direction
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.model.PacingPeriod
import dev.myaring.mobile_butler.domain.model.TrackingMode
import dev.myaring.mobile_butler.domain.pacing.GoalCreationValidator
import dev.myaring.mobile_butler.domain.pacing.ValidationResult
import dev.myaring.mobile_butler.ui.navigation.EGoalTab
import dev.myaring.mobile_butler.ui.navigation.GoalScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.UUID

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

    fun createGoal(
        title: String,
        unit: String,
        targetValue: Double,
        startDate: LocalDate = LocalDate.now(),
        dueDate: LocalDate,
    ): Boolean {
        val titleValidation = GoalCreationValidator.validateTitle(title)
        if (titleValidation is ValidationResult.Invalid) return false

        val unitValidation = GoalCreationValidator.validateUnit(unit)
        if (unitValidation is ValidationResult.Invalid) return false

        val dateValidation = GoalCreationValidator.validateDates(startDate, dueDate, LocalDate.now())
        if (dateValidation is ValidationResult.Invalid) return false

        val newGoal = Goal(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            type = GoalType.CUMULATIVE,
            direction = Direction.UP,
            pacingPeriod = PacingPeriod.DAY,
            unit = unit.trim(),
            targetValue = targetValue,
            startValue = 0.0,
            startDate = startDate,
            dueDate = dueDate,
            priority = _uiState.value.goals.size,
            status = GoalStatus.ACTIVE,
            trackingMode = TrackingMode.MANUAL
        )

        _uiState.update { current ->
            current.copy(goals = current.goals + newGoal)
        }
        onChangeTabToList()
        return true
    }
}
