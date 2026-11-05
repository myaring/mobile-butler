package dev.myaring.mobile_butler.repository

import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.GoalLog
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

object GoalSerializer {

    private fun encode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    }

    private fun decode(value: String): String {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString())
    }

    fun serializeGoal(goal: Goal): String {
        return listOf(
            goal.id,
            encode(goal.title),
            goal.targetAmount.toString(),
            goal.currentAmount.toString(),
            encode(goal.unit),
            goal.startDate.toString(),
            goal.deadline.toString(),
            goal.priority.toString(),
            goal.isCompleted.toString(),
            goal.createdAt.toString()
        ).joinToString(",")
    }

    fun deserializeGoal(raw: String): Goal? {
        val parts = raw.split(",")
        if (parts.size < 10) return null
        return try {
            Goal(
                id = parts[0],
                title = decode(parts[1]),
                targetAmount = parts[2].toDouble(),
                currentAmount = parts[3].toDouble(),
                unit = decode(parts[4]),
                startDate = LocalDate.parse(parts[5]),
                deadline = LocalDate.parse(parts[6]),
                priority = parts[7].toInt(),
                isCompleted = parts[8].toBoolean(),
                createdAt = parts[9].toLong()
            )
        } catch (_: Exception) {
            null
        }
    }

    fun serializeLog(log: GoalLog): String {
        return listOf(
            log.id,
            log.goalId,
            log.date.toString(),
            log.amount.toString(),
            encode(log.note),
            log.timestamp.toString()
        ).joinToString(",")
    }

    fun deserializeLog(raw: String): GoalLog? {
        val parts = raw.split(",")
        if (parts.size < 6) return null
        return try {
            GoalLog(
                id = parts[0],
                goalId = parts[1],
                date = LocalDate.parse(parts[2]),
                amount = parts[3].toDouble(),
                note = decode(parts[4]),
                timestamp = parts[5].toLong()
            )
        } catch (_: Exception) {
            null
        }
    }
}
