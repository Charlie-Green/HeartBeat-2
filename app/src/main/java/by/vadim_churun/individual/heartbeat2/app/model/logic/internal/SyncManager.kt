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

    /** Container for a [SongsSource] instance with related metadata. **/
    private inner class SongsSourceWrapper(
        val sourceCode: Byte
    ) {
        val source
            = sourcesMan.sourceByCode(sourceCode)
        val syncPeriodMillis
            = 1000L*this.source.recommendedSyncPeriod

        var nextSyncTime = 0L
        var lastErrorTime = 0L
        var waitPermissions = false
        var permissionsRerequestFlag = true

        /** The actual work of syncing with this source,
          * without threading. nor exceptions handling. **/
        fun sync() {
            val old = dbMan.rawSongs(); val new = this.source.fetch()
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

    private var sourceWrappers: List<SongsSourceWrapper>? = null

    private fun createSourceWrappers(): List<SongsSourceWrapper>
        = mutableListOf<SongsSourceWrapper>().apply {
            sourcesMan.forEachSource { source ->
                this.add( SongsSourceWrapper(source.ID) )
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

        val wrappers = sourceWrappers ?: createSourceWrappers().also { sourceWrappers = it }
        for(index in wrappers.indices) {
            val swrap = wrappers[index]
            if(System.currentTimeMillis() < swrap.nextSyncTime /* Not time yet */ ||
                swrap.waitPermissions /* Missing permissions */ )
                continue

            val allPermissions = swrap.source.permissions
            val missingPermissions = allPermissions.filter { perm ->
                val permStatus = ContextCompat.checkSelfPermission(appContext, perm)
                permStatus != PackageManager.PERMISSION_GRANTED
            }
            if(missingPermissions.isNotEmpty()) {
                synchronized(swrap) {
                    swrap.waitPermissions = true
                }
                SyncState.MissingPermissions(
                    swrap.sourceCode,
                    swrap.source.name,
                    missingPermissions
                ).also { stateSubject.onNext(it) }
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
                    swrap.source.name,
                    thr
                ).also {
                    stateSubject.onNext(it)
                    if(it.shouldDisturbUser)
                        swrap.lastErrorTime = now
                }
            }

            swrap.nextSyncTime = System.currentTimeMillis() + swrap.syncPeriodMillis
        }

        if(activeStateEmitted)
            stateSubject.onNext(SyncState.NotSyncing())
    }

    fun submitPermissionsResult(sourceCode: Byte, granted: Boolean) {
        val swrappers = sourceWrappers ?: throw IllegalStateException(
            "submitPermissionsResult is called before any sync is performed" )
        val swrap = swrappers.find {
            it.sourceCode == sourceCode
        } ?: return

        synchronized(swrap) {
            if(granted) {
                // Let sync work.
                swrap.permissionsRerequestFlag = true
                swrap.waitPermissions = false
            } else if(swrap.permissionsRerequestFlag) {
                // Re-request the permissions later.
                swrap.permissionsRerequestFlag = false
                swrap.nextSyncTime = System.currentTimeMillis() + swrap.syncPeriodMillis
                swrap.waitPermissions = false
            }
            // Otherwise, stop disturbing the user.
        }
    }
}