package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.app.model.state.SyncState
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
    sources: List<@JvmSuppressWildcards SongsSource>,
    private val dbMan: DatabaseManager
) {
    /////////////////////////////////////////////////////////////////////////////////////////
    // SOURCE WRAPPER:

    private inner class SongsSourceWrapper(val source: SongsSource) {
        var nextSyncTime = 0L
        var lastErrorTime = 0L
        var waitPermissions = false

        val name
            get() = when(source) {
                is ExternalStorageSongsSource -> R.string.songs_source_external_storage
                else -> throw Exception(
                    "Unknown ${SongsSource::class.java.simpleName}: ${this.javaClass.simpleName}" )
            }.let { appContext.getString(it) }

        fun sync() {
            val songEntities = source.fetch().map { song ->
                SongEntity.fromSong(song, source.javaClass)
            }
            dbMan.updateSource(source.javaClass, songEntities)
        }
    }

    private val sourceWrappers: List<SongsSourceWrapper>

    init {
        sourceWrappers = List(sources.size) { index ->
            SongsSourceWrapper(sources[index])
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

            val missingPermissions = swrap.source.permissions.filter { perm ->
                val permStatus = ContextCompat.checkSelfPermission(appContext, perm)
                permStatus != PackageManager.PERMISSION_GRANTED
            }
            if(missingPermissions.isNotEmpty()) {
                swrap.waitPermissions = true
                stateSubject.onNext(
                    SyncState.MissingPermissions(swrap.name, missingPermissions) )
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
                    swrap.name,
                    thr
                ).also {
                    stateSubject.onNext(it)
                    if(it.shouldDisturbUser)
                        swrap.lastErrorTime = now
                }
            }

            val syncPeriod = 1000L * swrap.source.recommendedSyncPeriod
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