package dev.myaring.mobile_butler.domain.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.EntrySource
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.repository.GoalRepository
import dev.myaring.mobile_butler.domain.repository.ProgressRepository
import dev.myaring.mobile_butler.ui.navigation.EGoalTab
import dev.myaring.mobile_butler.ui.navigation.GoalScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * [C-12] 목표 화면 ViewModel.
 */
class GoalScreenViewModel(
    private val goalRepository: GoalRepository? = null,
    private val progressRepository: ProgressRepository? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel(), ITopBarViewModel {

    private val _title = mutableStateOf("목표")
    override val title: State<String> = _title

    private val _returnSubTabOrdinal = mutableIntStateOf(-1)
    override val showBackButton: State<Boolean> =
        derivedStateOf { _returnSubTabOrdinal.value >= 0 }

    private val _uiState = MutableStateFlow(
        GoalScreenUiState(
            goals = defaultGoals(),
            entries = defaultEntries()
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
        _uiState.update { it.copy(editingGoalId = null) }
        onChangeTab(EGoalTab.LIST)
    }

    fun onChangeTabToDetail(goalId: String? = null) {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        if (goalId != null) {
            _uiState.update { it.copy(selectedGoalId = goalId, editingGoalId = null) }
        }
        onChangeTab(EGoalTab.DETAIL)
    }

    fun onChangeTabToCreation() {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        _uiState.update { it.copy(editingGoalId = null) }
        onChangeTab(EGoalTab.CREATION)
    }

    fun startEditingGoal(goalId: String) {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        _uiState.update { it.copy(editingGoalId = goalId) }
        _uiState.update { it.copy(selectedTab = EGoalTab.CREATION) }
        _title.value = "목표 수정"
    }

    fun onChangeTabToArchive() {
        _returnSubTabOrdinal.value = -1
        _uiState.update { it.copy(editingGoalId = null) }
        onChangeTab(EGoalTab.ARCHIVE)
    }

    fun selectGoal(goalId: String) {
        _uiState.update { it.copy(selectedGoalId = goalId) }
        onChangeTabToDetail(goalId)
    }

    /**
     * [C-12] 목표 수정 저장 및 마감일 연장 하루 분량 재계산 처리.
     * - 목표량을 낮춰서 기록 합계가 목표량 이상이 되면 바로 완료하지 않고 확인 요청 다이얼로그 플래그 설정
     */
    fun saveGoalEdits(
        goalId: String,
        newTitle: String,
        newTarget: Double,
        newUnit: String,
        newDueDate: LocalDate?,
    ) {
        val currentEntries = uiState.value.entries[goalId] ?: emptyList()
        val totalRecorded = currentEntries.sumOf { it.amount }

        if (totalRecorded >= newTarget) {
            // "이미 목표량을 채우셨습니다. 완료하시겠습니까?" 팝업 표시 플래그 설정
            _uiState.update {
                it.copy(
                    showAchievedConfirmDialog = true,
                    pendingTargetValue = newTarget,
                    pendingDueDate = newDueDate
                )
            }
        } else {
            // 일반 수정 저장
            applyGoalChanges(
                goalId = goalId,
                newTitle = newTitle,
                newTarget = newTarget,
                newUnit = newUnit,
                newDueDate = newDueDate,
                newStatus = GoalStatus.ACTIVE,
                newEndReason = null,
                newEndedAt = null
            )
            onChangeTabToDetail(goalId)
        }
    }

    /**
     * 확인 팝업에서 "예" 선택: ACHIEVED로 완료 처리.
     */
    fun confirmAchieved(goalId: String) {
        val target = uiState.value.pendingTargetValue ?: return
        val due = uiState.value.pendingDueDate
        val goal = uiState.value.goals.find { it.id == goalId } ?: return

        applyGoalChanges(
            goalId = goalId,
            newTitle = goal.title,
            newTarget = target,
            newUnit = goal.unit,
            newDueDate = due,
            newStatus = GoalStatus.COMPLETED,
            newEndReason = EndReason.ACHIEVED,
            newEndedAt = LocalDate.now(clock)
        )

        _uiState.update {
            it.copy(
                showAchievedConfirmDialog = false,
                pendingTargetValue = null,
                pendingDueDate = null
            )
        }
        onChangeTabToArchive()
    }

    /**
     * 확인 팝업에서 "아니오" 선택: ACTIVE 상태 유지.
     */
    fun dismissAchievedKeepActive(goalId: String) {
        val target = uiState.value.pendingTargetValue ?: return
        val due = uiState.value.pendingDueDate
        val goal = uiState.value.goals.find { it.id == goalId } ?: return

        applyGoalChanges(
            goalId = goalId,
            newTitle = goal.title,
            newTarget = target,
            newUnit = goal.unit,
            newDueDate = due,
            newStatus = GoalStatus.ACTIVE,
            newEndReason = null,
            newEndedAt = null
        )

        _uiState.update {
            it.copy(
                showAchievedConfirmDialog = false,
                pendingTargetValue = null,
                pendingDueDate = null
            )
        }
        onChangeTabToDetail(goalId)
    }

    /**
     * [C-12] 마감일 연장 시 하루 분량(페이싱) 재계산.
     */
    fun calculateRecalculatedQuota(goal: Goal, totalRecorded: Double, today: LocalDate): Int {
        val remainingValue = (goal.targetValue - totalRecorded).coerceAtLeast(0.0)
        val remainingDays = if (goal.dueDate != null && !goal.dueDate.isBefore(today)) {
            ChronoUnit.DAYS.between(today, goal.dueDate) + 1
        } else {
            1L
        }
        return (remainingValue / remainingDays).coerceAtLeast(1.0).toInt()
    }

    private fun applyGoalChanges(
        goalId: String,
        newTitle: String,
        newTarget: Double,
        newUnit: String,
        newDueDate: LocalDate?,
        newStatus: GoalStatus,
        newEndReason: EndReason?,
        newEndedAt: LocalDate?,
    ) {
        _uiState.update { current ->
            val updatedGoals = current.goals.map { g ->
                if (g.id == goalId) {
                    g.copy(
                        title = newTitle,
                        targetValue = newTarget,
                        unit = newUnit,
                        dueDate = newDueDate,
                        status = newStatus,
                        endReason = newEndReason,
                        endedAt = newEndedAt,
                        overdueNoticeShown = false
                    )
                } else {
                    g
                }
            }
            current.copy(goals = updatedGoals, editingGoalId = null)
        }
    }

    private fun onChangeTab(tab: EGoalTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        _title.value = when (tab) {
            EGoalTab.LIST -> "목표"
            EGoalTab.DETAIL -> "목표 상세"
            EGoalTab.CREATION -> if (uiState.value.editingGoalId != null) "목표 수정" else "목표 만들기"
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
                startDate = LocalDate.now().minusDays(5),
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
                startDate = LocalDate.now().minusDays(3),
                dueDate = LocalDate.now().plusDays(7),
                priority = 1,
                status = GoalStatus.ACTIVE
            )
        )

        fun defaultEntries(): Map<String, List<ProgressEntry>> = mapOf(
            "sample-1" to listOf(
                ProgressEntry(
                    id = "e-1",
                    goalId = "sample-1",
                    recordedAt = Instant.now().minusSeconds(86400 * 2),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 40.0,
                    source = EntrySource.MANUAL
                ),
                ProgressEntry(
                    id = "e-2",
                    goalId = "sample-1",
                    recordedAt = Instant.now().minusSeconds(86400),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 30.0,
                    source = EntrySource.MANUAL
                )
            )
        )
    }
}
