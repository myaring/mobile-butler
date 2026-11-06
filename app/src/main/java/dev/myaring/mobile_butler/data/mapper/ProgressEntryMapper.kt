package dev.myaring.mobile_butler.data.mapper

import dev.myaring.mobile_butler.data.local.entity.ProgressEntryEntity
import dev.myaring.mobile_butler.domain.model.EntrySource
import dev.myaring.mobile_butler.domain.model.ProgressEntry

fun ProgressEntry.toEntity() = ProgressEntryEntity(
    id = id,
    goalId = goalId,
    recordedAt = recordedAt,
    recordedOffset = recordedOffset,
    amount = amount,
    source = source.name,
)

fun ProgressEntryEntity.toDomain() = ProgressEntry(
    id = id,
    goalId = goalId,
    recordedAt = recordedAt,
    recordedOffset = recordedOffset,
    amount = amount,
    source = EntrySource.valueOf(source),
)