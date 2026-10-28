package dev.myaring.mobile_butler.data.mapper

import dev.myaring.mobile_butler.data.local.entity.GoalEntity
import dev.myaring.mobile_butler.domain.model.*

fun Goal.toEntity() = GoalEntity(
    id = id,
    title = title,
    type = type.name,
    direction = direction.name,
    pacingPeriod = pacingPeriod.name,
    unit = unit,
    targetValue = targetValue,
    startValue = startValue,
    startDate = startDate,
    dueDate = dueDate,
    measurementCadence = measurementCadence?.name,
    trackingMode = trackingMode.name,
    healthDataType = healthDataType?.name,
    priority = priority,
    restDays = restDays,
    status = status.name,
    endReason = endReason?.name,
    endedAt = endedAt,
    overdueNoticeShown = overdueNoticeShown,
)

fun GoalEntity.toDomain() = Goal(
    id = id,
    title = title,
    type = GoalType.valueOf(type),
    direction = Direction.valueOf(direction),
    pacingPeriod = PacingPeriod.valueOf(pacingPeriod),
    unit = unit,
    targetValue = targetValue,
    startValue = startValue,
    startDate = startDate,
    dueDate = dueDate,
    measurementCadence = measurementCadence?.let(Cadence::valueOf),
    trackingMode = TrackingMode.valueOf(trackingMode),
    healthDataType = healthDataType?.let(HealthDataType::valueOf),
    priority = priority,
    restDays = restDays,
    status = GoalStatus.valueOf(status),
    endReason = endReason?.let(EndReason::valueOf),
    endedAt = endedAt,
    overdueNoticeShown = overdueNoticeShown,
)