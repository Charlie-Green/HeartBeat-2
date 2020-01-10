package by.vadim_churun.individual.heartbeat2.model.logic.internal

import by.vadim_churun.individual.heartbeat2.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import javax.inject.Inject


/** This class is responsible for synchronization of the local database with [SongsSource]s. **/
class SyncManager @Inject constructor(
    private val sources: List<@JvmSuppressWildcards SongsSource>,
    private val dbMan: DatabaseManager
) {
    private val nextSyncTimes = MutableList(sources.size) { 0L }

    private fun SongsSource.sync() {
        val songEntities = fetch().map { song ->
            SongEntity.fromSong(song, this.javaClass)
        }
        dbMan.updateSource(this.javaClass, songEntities)
    }

    fun syncIfTime() {
        for(index in sources.indices) {
            if(System.currentTimeMillis() < nextSyncTimes[index])
                continue  // It's not time to sync with this source yet.

            val src = sources[index]
            src.sync()

            // The time spent for the sync is not encountered.
            nextSyncTimes[index] = System.currentTimeMillis() + 1000L*src.recommendedSyncPeriod
        }
    }
}