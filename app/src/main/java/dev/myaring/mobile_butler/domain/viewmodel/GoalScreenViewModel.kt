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
import java.util.UUID

/**
 * [C-9] 목표 화면 ViewModel.
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

    fun recordProgress(goalId: String, amount: Double) {
        val entry = ProgressEntry(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            recordedAt = Instant.now(clock),
            recordedOffset = ZoneOffset.ofHours(9),
            amount = amount,
            source = EntrySource.MANUAL
        )

        _uiState.update { current ->
            val currentEntries = current.entries[goalId] ?: emptyList()
            val newEntries = currentEntries + entry
            val newEntriesMap = current.entries.toMutableMap()
            newEntriesMap[goalId] = newEntries

            val targetGoal = current.goals.find { it.id == goalId }
            val newTotal = newEntries.sumOf { it.amount }
            val updatedGoals = if (targetGoal != null && targetGoal.status == GoalStatus.ACTIVE && newTotal >= targetGoal.targetValue) {
                current.goals.map { g ->
                    if (g.id == goalId) {
                        g.copy(
                            status = GoalStatus.COMPLETED,
                            endReason = EndReason.ACHIEVED,
                            endedAt = LocalDate.now(clock)
                        )
                    } else {
                        g
                    }
                }
            } else {
                current.goals
            }

            current.copy(entries = newEntriesMap, goals = updatedGoals)
        }
    }

    /**
     * [C-9] 목표 직접 끝내기.
     * - 마감 전에 직접 끝내면 endReason = MANUAL ("조기 종료", endedAt = 오늘)
     * - 기록 합계가 목표량에 도달하면 ACHIEVED (endedAt = 그날)
     * - 마감 후의 직접 끝내기는 DEADLINE
     * - 끝낸 직후 스낵바 안내 및 되돌리기 가능 상태 저장
     */
    fun completeGoalDirectly(goalId: String) {
        val today = LocalDate.now(clock)
        _uiState.update { current ->
            val targetGoal = current.goals.find { it.id == goalId } ?: return@update current
            val currentEntries = current.entries[goalId] ?: emptyList()
            val totalRecorded = currentEntries.sumOf { it.amount }

            val reason = when {
                targetGoal.dueDate != null && today.isAfter(targetGoal.dueDate) -> EndReason.DEADLINE
                totalRecorded >= targetGoal.targetValue -> EndReason.ACHIEVED
                else -> EndReason.MANUAL
            }

            val updatedGoals = current.goals.map { g ->
                if (g.id == goalId) {
                    g.copy(
                        status = GoalStatus.COMPLETED,
                        endReason = reason,
                        endedAt = today
                    )
                } else {
                    g
                }
            }

            current.copy(
                goals = updatedGoals,
                lastCompletedGoalId = goalId,
                snackbarMessage = "목표를 종료했습니다."
            )
        }
    }

    /**
     * [C-9] 완료된 목표 직후 되돌리기.
     * - status = ACTIVE, endedAt = null, endReason = null 로 복구
     */
    fun undoGoalCompletion(goalId: String) {
        _uiState.update { current ->
            val updatedGoals = current.goals.map { g ->
                if (g.id == goalId) {
                    g.copy(
                        status = GoalStatus.ACTIVE,
                        endReason = null,
                        endedAt = null
                    )
                } else {
                    g
                }
            }
            current.copy(
                goals = updatedGoals,
                lastCompletedGoalId = null,
                snackbarMessage = "목표 종료를 취소했습니다."
            )
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
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
            ),
            Goal(
                id = "sample-3",
                title = "알고리즘 100제",
                type = GoalType.CUMULATIVE,
                targetValue = 100.0,
                unit = "문제",
                startDate = LocalDate.now().minusDays(20),
                dueDate = LocalDate.now().plusDays(5),
                priority = 2,
                status = GoalStatus.COMPLETED,
                endReason = EndReason.MANUAL,
                endedAt = LocalDate.now().minusDays(2)
            ),
            Goal(
                id = "sample-4",
                title = "영어 회화 암기",
                type = GoalType.CUMULATIVE,
                targetValue = 50.0,
                unit = "문장",
                startDate = LocalDate.now().minusDays(30),
                dueDate = LocalDate.now().minusDays(5),
                priority = 3,
                status = GoalStatus.COMPLETED,
                endReason = EndReason.DEADLINE,
                endedAt = LocalDate.now().minusDays(5)
            )
        )

        fun defaultEntries(): Map<String, List<ProgressEntry>> = mapOf(
            "sample-1" to listOf(
                ProgressEntry(
                    id = "e-1",
                    goalId = "sample-1",
                    recordedAt = Instant.now().minusSeconds(86400 * 2),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 20.0,
                    source = EntrySource.MANUAL
                )
            ),
            "sample-3" to listOf(
                ProgressEntry(
                    id = "e-3",
                    goalId = "sample-3",
                    recordedAt = Instant.now().minusSeconds(86400 * 3),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 60.0,
                    source = EntrySource.MANUAL
                )
            ),
            "sample-4" to listOf(
                ProgressEntry(
                    id = "e-4",
                    goalId = "sample-4",
                    recordedAt = Instant.now().minusSeconds(86400 * 6),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 35.0,
                    source = EntrySource.MANUAL
                )
            )
        )
    }
}
