package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import by.vadim_churun.individual.heartbeat2.app.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.app.model.state.SyncState
import by.vadim_churun.individual.heartbeat2.shared.*
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton


/** This class is responsible for synchronization of the local database with [SongsSource]s. **/
@Singleton
class SyncManager @Inject constructor(
    private val appContext: Context,
    private val dbMan: DatabaseManager,
    private val sourcesMan: SongsSourcesManager
) {
    /////////////////////////////////////////////////////////////////////////////////////////
    // SOURCE WRAPPER:

    private inner class SongsSourceWrapper(
        val sourceMeta: SongsSourcesManager.SongsSourceMeta
    ) {
        var nextSyncTime = 0L
        var lastErrorTime = 0L
        var waitPermissions = false

        fun sync() {
            val old = dbMan.rawSongs(); val new = sourceMeta.source.fetch()
            val added = HashMap<Int, Song>()       // Songs added after this sync.
            val removedIds = mutableListOf<Int>()  // Songs removed from this source.

            for(song in new) {
                added[song.ID] = song
            }
            // Same elements in 'added' as in 'new'.

            for(song in old) {
                if(added.containsKey(song.ID))
                    added.remove(song.ID)   // This song is already known, so not 'added'.
                else
                    removedIds.add(song.ID) // This song is not in the source anymore.
            }

            val addedEntities = added.map { pair ->
                SongEntity.fromSong(pair.value)
            }
            dbMan.updateSongs(removedIds, addedEntities)
        }
    }

    private val sourceWrappers: List<SongsSourceWrapper> by lazy {
        mutableListOf<SongsSourceWrapper>().apply {
            sourcesMan.forEachSource { sourceMeta ->
                this.add( SongsSourceWrapper(sourceMeta) )
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // STATE:

    // Do not disturb the user with error messages related to the same source
    // more frequently then once in 2 hours.
    private val ERROR_DISTURB_INTERVAL = 7_200_000L

    private val stateSubject = BehaviorSubject.create<SyncState>()

    fun observableState(): Observable<SyncState>
        = stateSubject
            .startWith(SyncState.NotSyncing())
            .filter { (it !is SyncState.Error) || (!it.consumed) }


    /////////////////////////////////////////////////////////////////////////////////////////
    // IMPLEMENTATION:

    fun syncIfTime() {
        var activeStateEmitted = false

        for(index in sourceWrappers.indices) {
            val swrap = sourceWrappers[index]
            if(System.currentTimeMillis() < swrap.nextSyncTime /* Not time yet */ ||
                swrap.waitPermissions /* Sync impossible: missing permissions */ )
                continue

            val allPermissions = swrap.sourceMeta.source.permissions
            val missingPermissions = allPermissions.filter { perm ->
                val permStatus = ContextCompat.checkSelfPermission(appContext, perm)
                permStatus != PackageManager.PERMISSION_GRANTED
            }
            if(missingPermissions.isNotEmpty()) {
                swrap.waitPermissions = true
                stateSubject.onNext(
                    SyncState.MissingPermissions(swrap.sourceMeta.name, missingPermissions) )
                return
            }

            if(!activeStateEmitted) {
                stateSubject.onNext( SyncState.Active() )
                activeStateEmitted = true
            }

            try {
                swrap.sync()
            } catch(thr: Throwable) {
                val now = System.currentTimeMillis()
                SyncState.Error(
                    now - swrap.lastErrorTime >= ERROR_DISTURB_INTERVAL,
                    swrap.sourceMeta.name,
                    thr
                ).also {
                    stateSubject.onNext(it)
                    if(it.shouldDisturbUser)
                        swrap.lastErrorTime = now
                }
            }

            val syncPeriod = 1000L * swrap.sourceMeta.source.recommendedSyncPeriod
            swrap.nextSyncTime = System.currentTimeMillis() + syncPeriod
        }

        if(activeStateEmitted)
            stateSubject.onNext(SyncState.NotSyncing())
    }

    fun notifyPermissionsGranted() {
        for(swrap in sourceWrappers) {
            swrap.waitPermissions = false
        }
    }
}