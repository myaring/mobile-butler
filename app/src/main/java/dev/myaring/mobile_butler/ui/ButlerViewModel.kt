package dev.myaring.mobile_butler.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.myaring.mobile_butler.engine.ButlerDialogueEngine
import dev.myaring.mobile_butler.engine.GoalValidator
import dev.myaring.mobile_butler.engine.PacingEngine
import dev.myaring.mobile_butler.model.ButlerEvent
import dev.myaring.mobile_butler.model.ButlerPersona
import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.GoalLog
import dev.myaring.mobile_butler.model.GoalStatus
import dev.myaring.mobile_butler.model.PacingResult
import dev.myaring.mobile_butler.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class ButlerUiState(
    val goals: List<Goal> = emptyList(),
    val pacingResults: Map<String, PacingResult> = emptyMap(),
    val persona: ButlerPersona = ButlerPersona.CALM,
    val activeDialogue: String = "",
    val activeEvent: ButlerEvent = ButlerEvent.HOME_GREETING,
    val selectedGoalForDetail: Goal? = null,
    val selectedGoalForLog: Goal? = null,
    val selectedGoalForEdit: Goal? = null,
    val isAddGoalDialogOpen: Boolean = false,
    val isSettingsDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val today: LocalDate = LocalDate.now()
)

class ButlerViewModel(
    private val repository: GoalRepository,
    var todayProvider: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(ButlerUiState())
    val uiState: StateFlow<ButlerUiState> = _uiState.asStateFlow()

    private var dialogueSeed = 0

    init {
        refresh()
    }

    fun refresh() {
        val today = todayProvider()
        val persona = repository.getButlerPersona()
        val goals = repository.getAllGoals()
        val pacingMap = goals.associate { it.id to PacingEngine.calculate(it, today) }

        val event = determineCurrentEvent(goals, pacingMap)
        val dialogue = ButlerDialogueEngine.getDialogue(persona, event, dialogueSeed++)

        _uiState.update { current ->
            current.copy(
                goals = goals,
                pacingResults = pacingMap,
                persona = persona,
                activeDialogue = dialogue,
                activeEvent = event,
                today = today
            )
        }
    }

    private fun determineCurrentEvent(
        goals: List<Goal>,
        pacingMap: Map<String, PacingResult>
    ): ButlerEvent {
        if (goals.isEmpty()) return ButlerEvent.NO_GOALS

        val hasOverdue = pacingMap.values.any { it.status == GoalStatus.OVERDUE }
        if (hasOverdue) return ButlerEvent.DEADLINE_OVERDUE

        val hasDDay = pacingMap.values.any { it.status == GoalStatus.D_DAY }
        if (hasDDay) return ButlerEvent.DEADLINE_D_DAY

        val hasApproaching = pacingMap.values.any { it.remainingDays in 1..3 && it.status == GoalStatus.ON_TRACK }
        if (hasApproaching) return ButlerEvent.DEADLINE_APPROACHING

        return ButlerEvent.HOME_GREETING
    }

    fun openAddGoalDialog() {
        _uiState.update { it.copy(isAddGoalDialogOpen = true, errorMessage = null) }
    }

    fun closeAddGoalDialog() {
        _uiState.update { it.copy(isAddGoalDialogOpen = false, errorMessage = null) }
    }

    fun openEditGoalDialog(goal: Goal) {
        _uiState.update { it.copy(selectedGoalForEdit = goal, errorMessage = null) }
    }

    fun closeEditGoalDialog() {
        _uiState.update { it.copy(selectedGoalForEdit = null, errorMessage = null) }
    }

    fun openLogProgressDialog(goal: Goal) {
        _uiState.update { it.copy(selectedGoalForLog = goal, errorMessage = null) }
    }

    fun closeLogProgressDialog() {
        _uiState.update { it.copy(selectedGoalForLog = null, errorMessage = null) }
    }

    fun openGoalDetailDialog(goal: Goal) {
        _uiState.update { it.copy(selectedGoalForDetail = goal) }
    }

    fun closeGoalDetailDialog() {
        _uiState.update { it.copy(selectedGoalForDetail = null) }
    }

    fun openSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = true) }
    }

    fun closeSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun createGoal(
        title: String,
        targetAmountText: String,
        unit: String,
        deadline: LocalDate?
    ): Boolean {
        val today = todayProvider()
        val validation = GoalValidator.validateGoalInput(title, targetAmountText, deadline, today)
        if (!validation.isSuccess) {
            _uiState.update { it.copy(errorMessage = validation.errorMessage) }
            return false
        }

        val targetAmount = targetAmountText.trim().toDouble()
        val sanitizedUnit = unit.trim().ifEmpty { "쪽" }

        repository.addGoal(
            title = title.trim(),
            targetAmount = targetAmount,
            unit = sanitizedUnit,
            deadline = deadline!!,
            startDate = today
        )

        closeAddGoalDialog()
        refresh()
        return true
    }

    fun updateGoal(
        goalId: String,
        title: String,
        targetAmountText: String,
        unit: String,
        deadline: LocalDate?
    ): Boolean {
        val existing = repository.getGoal(goalId) ?: return false
        val today = todayProvider()
        val validation = GoalValidator.validateGoalInput(title, targetAmountText, deadline, today)
        if (!validation.isSuccess) {
            _uiState.update { it.copy(errorMessage = validation.errorMessage) }
            return false
        }

        val targetAmount = targetAmountText.trim().toDouble()
        val sanitizedUnit = unit.trim().ifEmpty { "쪽" }

        val updated = existing.copy(
            title = title.trim(),
            targetAmount = targetAmount,
            unit = sanitizedUnit,
            deadline = deadline!!
        )
        repository.updateGoal(updated)

        closeEditGoalDialog()
        // If details dialog is also showing this goal, refresh it
        if (_uiState.value.selectedGoalForDetail?.id == goalId) {
            _uiState.update { it.copy(selectedGoalForDetail = updated) }
        }
        refresh()
        return true
    }

    fun deleteGoal(goalId: String) {
        repository.deleteGoal(goalId)
        if (_uiState.value.selectedGoalForDetail?.id == goalId) {
            closeGoalDetailDialog()
        }
        refresh()
    }

    fun reorderUp(goalId: String) {
        val success = repository.moveGoalUp(goalId)
        if (success) {
            refresh()
        }
    }

    fun reorderDown(goalId: String) {
        val success = repository.moveGoalDown(goalId)
        if (success) {
            refresh()
        }
    }

    fun logProgress(
        goalId: String,
        amountText: String,
        note: String = ""
    ): Boolean {
        val validation = GoalValidator.validateProgressLogInput(amountText)
        if (!validation.isSuccess) {
            _uiState.update { it.copy(errorMessage = validation.errorMessage) }
            return false
        }

        val amount = amountText.trim().toDouble()
        val today = todayProvider()
        val logged = repository.addProgressLog(goalId, amount, note, today)
        if (logged == null) {
            _uiState.update { it.copy(errorMessage = "목표를 찾을 수 없습니다.") }
            return false
        }

        closeLogProgressDialog()

        val updatedGoal = repository.getGoal(goalId)
        val event = if (updatedGoal?.isCompleted == true) {
            ButlerEvent.GOAL_COMPLETED
        } else {
            ButlerEvent.PROGRESS_LOGGED
        }

        val dialogue = ButlerDialogueEngine.getDialogue(_uiState.value.persona, event, dialogueSeed++)
        _uiState.update { current ->
            current.copy(
                activeDialogue = dialogue,
                activeEvent = event,
                selectedGoalForDetail = if (current.selectedGoalForDetail?.id == goalId) updatedGoal else current.selectedGoalForDetail
            )
        }

        refresh()
        // Re-set active dialogue and event to reflect the log action
        _uiState.update { it.copy(activeDialogue = dialogue, activeEvent = event) }
        return true
    }

    fun deleteLog(logId: String) {
        repository.deleteProgressLog(logId)
        val currentDetail = _uiState.value.selectedGoalForDetail
        if (currentDetail != null) {
            val refreshed = repository.getGoal(currentDetail.id)
            _uiState.update { it.copy(selectedGoalForDetail = refreshed) }
        }
        refresh()
    }

    fun getLogsForGoal(goalId: String): List<GoalLog> {
        return repository.getLogsForGoal(goalId)
    }

    fun selectPersona(persona: ButlerPersona) {
        repository.setButlerPersona(persona)
        val dialogue = ButlerDialogueEngine.getDialogue(persona, ButlerEvent.HOME_GREETING, dialogueSeed++)
        _uiState.update { current ->
            current.copy(
                persona = persona,
                activeDialogue = dialogue,
                activeEvent = ButlerEvent.HOME_GREETING,
                isSettingsDialogOpen = false
            )
        }
    }
}

class ButlerViewModelFactory(
    private val repository: GoalRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ButlerViewModel::class.java)) {
            return ButlerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
