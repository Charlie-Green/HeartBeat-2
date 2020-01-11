package by.vadim_churun.individual.heartbeat2.model.logic.internal

import android.content.Context
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.model.state.SyncState
import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import by.vadim_churun.individual.heartbeat2.storage.ExternalStorageSongsSource
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton


/** This class is responsible for synchronization of the local database with [SongsSource]s. **/
@Singleton
class SyncManager @Inject constructor(
    private val appContext: Context,
    private val sources: List<@JvmSuppressWildcards SongsSource>,
    private val dbMan: DatabaseManager
) {
    /////////////////////////////////////////////////////////////////////////////////////////
    // STATE:

    private val stateSubject = BehaviorSubject.create<SyncState>()

    fun observableState(): Observable<SyncState>
        = stateSubject
            .startWith(SyncState.NotSyncing())
            .filter { (it !is SyncState.Error) || (!it.consumed) }


    /////////////////////////////////////////////////////////////////////////////////////////
    // FIELDS AND HELP:

    private val nextSyncTimes  = MutableList(sources.size) { 0L }
    private val lastErrorTimes = MutableList(sources.size) { 0L }
    private val ERROR_DISTURB_INTERVAL = 7_200_000L  // (1)
    // (1) Do not disturb the user with error messages related to the same source
    // more frequently then once in 2 hours.

    private val SongsSource.name
        get() = when(this) {
            is ExternalStorageSongsSource -> R.string.songs_source_external_storage
            else -> throw Exception(
                "Unknown ${SongsSource::class.java.simpleName}: ${this.javaClass.simpleName}" )
        }.let { appContext.getString(it) }

    private fun SongsSource.sync() {
        val songEntities = fetch().map { song ->
            SongEntity.fromSong(song, this.javaClass)
        }
        dbMan.updateSource(this.javaClass, songEntities)
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    fun syncIfTime() {
        var activeStateEmitted = false

        for(index in sources.indices) {
            if(System.currentTimeMillis() < nextSyncTimes[index])
                continue  // It's not time to sync with this source yet.

            if(!activeStateEmitted) {
                stateSubject.onNext( SyncState.Active() )
                activeStateEmitted = true
            }

            val src = sources[index]
            try {
                src.sync()
            } catch(thr: Throwable) {
                val now = System.currentTimeMillis()
                SyncState.Error(
                    now - lastErrorTimes[index] >= ERROR_DISTURB_INTERVAL,
                    src.name,
                    thr
                ).also {
                    stateSubject.onNext(it)
                    if(it.shouldDisturbUser)
                        lastErrorTimes[index] = now
                }
            }

            // The time spent for the sync is not encountered.
            nextSyncTimes[index] = System.currentTimeMillis() + 1000L*src.recommendedSyncPeriod
        }

        if(activeStateEmitted)
            stateSubject.onNext(SyncState.NotSyncing())
    }
}