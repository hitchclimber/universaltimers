package io.github.hitchclimber.universaltimers.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BundleRepository(private val dao: BundleDao) {

    fun observeAll(): Flow<List<TimerBundle>> =
        dao.observeAll().map { entities -> entities.map { it.toTimerBundle() } }

    suspend fun save(bundle: TimerBundle) {
        dao.upsert(bundle.toEntity())
    }

    suspend fun delete(bundle: TimerBundle) {
        dao.delete(bundle.toEntity())
    }
}
