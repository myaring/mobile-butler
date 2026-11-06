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
 * [C-8] 목표 화면 ViewModel.
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

            // 목표량 달성 여부 검사
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

    fun updateProgressEntry(goalId: String, entryId: String, newAmount: Double) {
        require(newAmount > 0.0) { "Amount must be positive." }

        _uiState.update { current ->
            val currentEntries = current.entries[goalId] ?: return@update current
            val updatedEntries = currentEntries.map { entry ->
                if (entry.id == entryId) {
                    entry.copy(amount = newAmount)
                } else {
                    entry
                }
            }
            val newEntriesMap = current.entries.toMutableMap()
            newEntriesMap[goalId] = updatedEntries

            // 수정으로 합계가 목표량 이상이 되면 ACHIEVED로 완료
            val targetGoal = current.goals.find { it.id == goalId }
            val newTotal = updatedEntries.sumOf { it.amount }
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

    fun deleteProgressEntry(goalId: String, entryId: String) {
        _uiState.update { current ->
            val currentEntries = current.entries[goalId] ?: return@update current
            val updatedEntries = currentEntries.filterNot { it.id == entryId }
            val newEntriesMap = current.entries.toMutableMap()
            newEntriesMap[goalId] = updatedEntries
            current.copy(entries = newEntriesMap)
        }
    }

    fun completeGoalManually(goalId: String) {
        val today = LocalDate.now(clock)
        _uiState.update { current ->
            val updatedGoals = current.goals.map { g ->
                if (g.id == goalId) {
                    g.copy(
                        status = GoalStatus.COMPLETED,
                        endReason = EndReason.MANUAL,
                        endedAt = today
                    )
                } else {
                    g
                }
            }
            current.copy(goals = updatedGoals)
        }
    }

    fun extendDueDate(goalId: String, newDueDate: LocalDate) {
        _uiState.update { current ->
            val updatedGoals = current.goals.map { g ->
                if (g.id == goalId) {
                    g.copy(dueDate = newDueDate, overdueNoticeShown = false)
                } else {
                    g
                }
            }
            current.copy(goals = updatedGoals)
        }
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
                title = "알고리즘 문제 풀이",
                type = GoalType.CUMULATIVE,
                targetValue = 50.0,
                unit = "문제",
                startDate = LocalDate.now().minusDays(15),
                dueDate = LocalDate.now().minusDays(1),
                priority = 2,
                status = GoalStatus.COMPLETED,
                endReason = EndReason.MANUAL,
                endedAt = LocalDate.now().minusDays(1)
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
                ),
                ProgressEntry(
                    id = "e-2",
                    goalId = "sample-1",
                    recordedAt = Instant.now().minusSeconds(86400),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 15.0,
                    source = EntrySource.MANUAL
                ),
                ProgressEntry(
                    id = "e-3",
                    goalId = "sample-1",
                    recordedAt = Instant.now(),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 25.0,
                    source = EntrySource.MANUAL
                )
            ),
            "sample-3" to listOf(
                ProgressEntry(
                    id = "e-4",
                    goalId = "sample-3",
                    recordedAt = Instant.now().minusSeconds(86400 * 2),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 30.0,
                    source = EntrySource.MANUAL
                )
            )
        )
    }
}
