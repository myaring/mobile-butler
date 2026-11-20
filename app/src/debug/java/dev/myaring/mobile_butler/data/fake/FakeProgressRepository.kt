package dev.myaring.mobile_butler.data.fake

import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant

/**
 * [A-7] 디버그용 메모리 기반 가짜 ProgressRepository 구현체.
 * 릴리스 빌드에는 포함되지 않습니다.
 */
class FakeProgressRepository(
    initialEntries: List<ProgressEntry> = emptyList()
) : ProgressRepository {

    private val entriesList = mutableListOf<ProgressEntry>().apply {
        addAll(initialEntries)
    }
    private val flow = MutableStateFlow(entriesList.toList())

    override fun observeEntries(goalId: String): Flow<List<ProgressEntry>> =
        flow.map { list ->
            list.filter { it.goalId == goalId }
                .sortedWith(compareBy({ it.recordedAt }, { it.id }))
        }

    override suspend fun getEntry(id: String): ProgressEntry? =
        entriesList.find { it.id == id }

    override suspend fun getEntriesBetween(
        goalId: String,
        from: Instant,
        until: Instant
    ): List<ProgressEntry> =
        entriesList.filter {
            it.goalId == goalId && !it.recordedAt.isBefore(from) && it.recordedAt.isBefore(until)
        }

    override suspend fun addEntry(entry: ProgressEntry) {
        entriesList.add(entry)
        flow.value = entriesList.toList()
    }

    override suspend fun updateEntry(entry: ProgressEntry) {
        val index = entriesList.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            entriesList[index] = entry
            flow.value = entriesList.toList()
        }
    }

    override suspend fun deleteEntry(id: String) {
        entriesList.removeAll { it.id == id }
        flow.value = entriesList.toList()
    }
}
