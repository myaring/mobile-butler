package dev.myaring.mobile_butler.repository

import dev.myaring.mobile_butler.model.ButlerPersona
import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.GoalLog
import java.time.LocalDate

interface GoalRepository {
    fun getAllGoals(): List<Goal>
    fun getGoal(id: String): Goal?
    fun addGoal(
        title: String,
        targetAmount: Double,
        unit: String,
        deadline: LocalDate,
        startDate: LocalDate = LocalDate.now()
    ): Goal
    fun updateGoal(goal: Goal): Boolean
    fun deleteGoal(goalId: String): Boolean
    fun moveGoalUp(goalId: String): Boolean
    fun moveGoalDown(goalId: String): Boolean
    fun addProgressLog(
        goalId: String,
        amount: Double,
        note: String = "",
        date: LocalDate = LocalDate.now()
    ): GoalLog?
    fun getLogsForGoal(goalId: String): List<GoalLog>
    fun deleteProgressLog(logId: String): Boolean
    fun getButlerPersona(): ButlerPersona
    fun setButlerPersona(persona: ButlerPersona)
    fun clearAll()
}
