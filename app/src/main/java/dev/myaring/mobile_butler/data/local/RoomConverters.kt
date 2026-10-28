package dev.myaring.mobile_butler.data.local

import androidx.room3.ColumnTypeConverter
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class RoomConverters {

    @ColumnTypeConverter
    fun dateToString(value: LocalDate?): String? =
        value?.toString()

    @ColumnTypeConverter
    fun stringToDate(value: String?): LocalDate? =
        value?.let(LocalDate::parse)

    @ColumnTypeConverter
    fun instantToLong(value: Instant?): Long? =
        value?.toEpochMilli()

    @ColumnTypeConverter
    fun longToInstant(value: Long?): Instant? =
        value?.let(Instant::ofEpochMilli)

    @ColumnTypeConverter
    fun offsetToSeconds(value: ZoneOffset?): Int? =
        value?.totalSeconds

    @ColumnTypeConverter
    fun secondsToOffset(value: Int?): ZoneOffset? =
        value?.let(ZoneOffset::ofTotalSeconds)

    @ColumnTypeConverter
    fun restDaysToString(
        value: Set<DayOfWeek>?
    ): String? =
        value
            ?.sortedBy { it.value }
            ?.joinToString(",") { it.name }

    @ColumnTypeConverter
    fun stringToRestDays(
        value: String?
    ): Set<DayOfWeek>? {
        if (value == null) return null
        if (value.isEmpty()) return emptySet()

        return value.split(",")
            .map(DayOfWeek::valueOf)
            .toSet()
    }
}