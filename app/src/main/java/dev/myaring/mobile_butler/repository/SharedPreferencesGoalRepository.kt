package dev.myaring.mobile_butler.repository

import android.content.Context
import android.content.SharedPreferences
import dev.myaring.mobile_butler.model.ButlerPersona
import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.GoalLog
import java.time.LocalDate

class SharedPreferencesGoalRepository(
    private val preferences: SharedPreferences
) : GoalRepository {

    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val inMemory = InMemoryGoalRepository()

    init {
        loadFromPreferences()
    }

    private fun loadFromPreferences() {
        val personaCode = preferences.getString(KEY_PERSONA, ButlerPersona.CALM.code)
        inMemory.setButlerPersona(ButlerPersona.fromCode(personaCode))

        val serializedGoals = preferences.getStringSet(KEY_GOALS, emptySet()) ?: emptySet()
        val loadedGoals = serializedGoals.mapNotNull { GoalSerializer.deserializeGoal(it) }
            .sortedBy { it.priority }

        val serializedLogs = preferences.getStringSet(KEY_LOGS, emptySet()) ?: emptySet()
        val loadedLogs = serializedLogs.mapNotNull { GoalSerializer.deserializeLog(it) }

        inMemory.clearAll()
        inMemory.setButlerPersona(ButlerPersona.fromCode(personaCode))

        loadedGoals.forEach { goal ->
            inMemory.addGoal(
                title = goal.title,
                targetAmount = goal.targetAmount,
                unit = goal.unit,
                deadline = goal.deadline,
                startDate = goal.startDate
            )
        }

        // Reapply loaded goals with their exact id, currentAmount, isCompleted, priority
        loadedGoals.forEach { goal ->
            inMemory.updateGoal(goal)
        }

        loadedLogs.forEach { log ->
            inMemory.addProgressLog(
                goalId = log.goalId,
                amount = log.amount,
                note = log.note,
                date = log.date
            )
        }
    }

    private fun saveToPreferences() {
        val goalsSet = inMemory.getAllGoals()
            .map { GoalSerializer.serializeGoal(it) }
            .toSet()

        val allLogs = inMemory.getAllGoals().flatMap { inMemory.getLogsForGoal(it.id) }
        val logsSet = allLogs
            .map { GoalSerializer.serializeLog(it) }
            .toSet()

        preferences.edit()
            .putStringSet(KEY_GOALS, goalsSet)
            .putStringSet(KEY_LOGS, logsSet)
            .putString(KEY_PERSONA, inMemory.getButlerPersona().code)
            .apply()
    }

    override fun getAllGoals(): List<Goal> {
        return inMemory.getAllGoals()
    }

    override fun getGoal(id: String): Goal? {
        return inMemory.getGoal(id)
    }

    override fun addGoal(
        title: String,
        targetAmount: Double,
        unit: String,
        deadline: LocalDate,
        startDate: LocalDate
    ): Goal {
        val goal = inMemory.addGoal(title, targetAmount, unit, deadline, startDate)
        saveToPreferences()
        return goal
    }

    override fun updateGoal(goal: Goal): Boolean {
        val result = inMemory.updateGoal(goal)
        if (result) saveToPreferences()
        return result
    }

    override fun deleteGoal(goalId: String): Boolean {
        val result = inMemory.deleteGoal(goalId)
        if (result) saveToPreferences()
        return result
    }

    override fun moveGoalUp(goalId: String): Boolean {
        val result = inMemory.moveGoalUp(goalId)
        if (result) saveToPreferences()
        return result
    }

    override fun moveGoalDown(goalId: String): Boolean {
        val result = inMemory.moveGoalDown(goalId)
        if (result) saveToPreferences()
        return result
    }

    override fun addProgressLog(
        goalId: String,
        amount: Double,
        note: String,
        date: LocalDate
    ): GoalLog? {
        val log = inMemory.addProgressLog(goalId, amount, note, date)
        if (log != null) saveToPreferences()
        return log
    }

    override fun getLogsForGoal(goalId: String): List<GoalLog> {
        return inMemory.getLogsForGoal(goalId)
    }

    override fun deleteProgressLog(logId: String): Boolean {
        val result = inMemory.deleteProgressLog(logId)
        if (result) saveToPreferences()
        return result
    }

    override fun getButlerPersona(): ButlerPersona {
        return inMemory.getButlerPersona()
    }

    override fun setButlerPersona(persona: ButlerPersona) {
        inMemory.setButlerPersona(persona)
        saveToPreferences()
    }

    override fun clearAll() {
        inMemory.clearAll()
        saveToPreferences()
    }

    companion object {
        private const val PREFS_NAME = "mobile_butler_storage"
        private const val KEY_GOALS = "key_stored_goals"
        private const val KEY_LOGS = "key_stored_logs"
        private const val KEY_PERSONA = "key_selected_persona"
    }
}
