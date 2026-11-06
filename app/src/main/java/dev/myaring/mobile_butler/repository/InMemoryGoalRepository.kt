package dev.myaring.mobile_butler.repository

import dev.myaring.mobile_butler.model.ButlerPersona
import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.GoalLog
import java.time.LocalDate
import java.util.UUID

class InMemoryGoalRepository : GoalRepository {

    private val goals = mutableListOf<Goal>()
    private val logs = mutableListOf<GoalLog>()
    private var persona: ButlerPersona = ButlerPersona.CALM

    @Synchronized
    override fun getAllGoals(): List<Goal> {
        return goals.sortedBy { it.priority }.toList()
    }

    @Synchronized
    override fun getGoal(id: String): Goal? {
        return goals.firstOrNull { it.id == id }
    }

    @Synchronized
    override fun addGoal(
        title: String,
        targetAmount: Double,
        unit: String,
        deadline: LocalDate,
        startDate: LocalDate
    ): Goal {
        val sanitizedUnit = unit.trim().ifEmpty { "쪽" }
        val newGoal = Goal(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            targetAmount = targetAmount,
            currentAmount = 0.0,
            unit = sanitizedUnit,
            startDate = startDate,
            deadline = deadline,
            priority = goals.size,
            isCompleted = false
        )
        goals.add(newGoal)
        return newGoal
    }

    @Synchronized
    override fun updateGoal(goal: Goal): Boolean {
        val index = goals.indexOfFirst { it.id == goal.id }
        if (index == -1) return false

        val isCompletedNow = goal.targetAmount > 0.0 && goal.currentAmount >= goal.targetAmount
        goals[index] = goal.copy(isCompleted = isCompletedNow)
        return true
    }

    @Synchronized
    override fun deleteGoal(goalId: String): Boolean {
        val removed = goals.removeAll { it.id == goalId }
        if (!removed) return false

        logs.removeAll { it.goalId == goalId }
        reindexPriorities()
        return true
    }

    @Synchronized
    override fun moveGoalUp(goalId: String): Boolean {
        val sortedList = goals.sortedBy { it.priority }.toMutableList()
        val index = sortedList.indexOfFirst { it.id == goalId }
        if (index <= 0) return false

        val temp = sortedList[index]
        sortedList[index] = sortedList[index - 1]
        sortedList[index - 1] = temp

        goals.clear()
        sortedList.forEachIndexed { idx, item ->
            goals.add(item.copy(priority = idx))
        }
        return true
    }

    @Synchronized
    override fun moveGoalDown(goalId: String): Boolean {
        val sortedList = goals.sortedBy { it.priority }.toMutableList()
        val index = sortedList.indexOfFirst { it.id == goalId }
        if (index == -1 || index >= sortedList.size - 1) return false

        val temp = sortedList[index]
        sortedList[index] = sortedList[index + 1]
        sortedList[index + 1] = temp

        goals.clear()
        sortedList.forEachIndexed { idx, item ->
            goals.add(item.copy(priority = idx))
        }
        return true
    }

    @Synchronized
    override fun addProgressLog(
        goalId: String,
        amount: Double,
        note: String,
        date: LocalDate
    ): GoalLog? {
        val goalIndex = goals.indexOfFirst { it.id == goalId }
        if (goalIndex == -1) return null

        val safeAmount = amount.coerceAtLeast(0.0)
        val newLog = GoalLog(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            date = date,
            amount = safeAmount,
            note = note.trim()
        )
        logs.add(newLog)

        val targetGoal = goals[goalIndex]
        val updatedCurrent = targetGoal.currentAmount + safeAmount
        val completed = updatedCurrent >= targetGoal.targetAmount
        goals[goalIndex] = targetGoal.copy(
            currentAmount = updatedCurrent,
            isCompleted = completed
        )

        return newLog
    }

    @Synchronized
    override fun getLogsForGoal(goalId: String): List<GoalLog> {
        return logs.filter { it.goalId == goalId }
            .sortedByDescending { it.timestamp }
            .toList()
    }

    @Synchronized
    override fun deleteProgressLog(logId: String): Boolean {
        val logIndex = logs.indexOfFirst { it.id == logId }
        if (logIndex == -1) return false

        val logToRemove = logs[logIndex]
        logs.removeAt(logIndex)

        val goalIndex = goals.indexOfFirst { it.id == logToRemove.goalId }
        if (goalIndex != -1) {
            val targetGoal = goals[goalIndex]
            val totalRemainingFromLogs = logs
                .filter { it.goalId == targetGoal.id }
                .sumOf { it.amount }
            val completed = totalRemainingFromLogs >= targetGoal.targetAmount
            goals[goalIndex] = targetGoal.copy(
                currentAmount = totalRemainingFromLogs,
                isCompleted = completed
            )
        }
        return true
    }

    @Synchronized
    override fun getButlerPersona(): ButlerPersona {
        return persona
    }

    @Synchronized
    override fun setButlerPersona(persona: ButlerPersona) {
        this.persona = persona
    }

    @Synchronized
    override fun clearAll() {
        goals.clear()
        logs.clear()
        persona = ButlerPersona.CALM
    }

    private fun reindexPriorities() {
        val sortedList = goals.sortedBy { it.priority }
        goals.clear()
        sortedList.forEachIndexed { idx, item ->
            goals.add(item.copy(priority = idx))
        }
    }
}
