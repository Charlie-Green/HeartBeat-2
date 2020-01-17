package by.vadim_churun.individual.heartbeat2.app.model.logic

import by.vadim_churun.individual.heartbeat2.app.model.logic.internal.*
import by.vadim_churun.individual.heartbeat2.app.model.obj.SongsList
import by.vadim_churun.individual.heartbeat2.app.model.state.SongsCollectionState
import by.vadim_churun.individual.heartbeat2.shared.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/** One of the highest level classes of MVI's "model" layer,
  * the one which manages current collection of songs. **/
class SongsCollectionRepository @Inject constructor(
    private val collectMan: SongsCollectionManager,
    private val dbMan: DatabaseManager,
    private val syncMan: SyncManager,
    private val stubMan: SongStubsManager
) {
    //////////////////////////////////////////////////////////////////////////////////////////
    // INTERNAL:

    private val disposable = CompositeDisposable()

    private fun subscribeSync()
        = Observable.interval(1L, TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .switchMap {
                syncMan.syncIfTime()
                Observable.empty<Unit>()
            }.subscribe()

    private fun subscribeCollectionPrepared()
        = observableState()
            .doOnNext { state ->
                if(state is SongsCollectionState.CollectionPrepared)
                    collectMan.collection = state.songs
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // API:

    private var stateRx: Observable<out SongsCollectionState>? = null
    private var cachedPreparedState: SongsCollectionState.CollectionPrepared? = null

    fun observableState(): Observable<out SongsCollectionState>
        = stateRx ?: dbMan.observableSongs()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map { songEntities ->
                // TODO: Filter, etc.

                songEntities.map { songEntity ->
                    // For now, just provide the default settings for each song.
                    // Later, that settings will be made customizable.
                    SongWithSettings(
                        songEntity.ID,
                        songEntity.title,
                        songEntity.artist,
                        songEntity.duration,
                        songEntity.filename,
                        songEntity.contentUri,
                        /* rate:     */ 1f,
                        /* volume:   */ 1f,
                        /* priority: */ 3
                    )
                }
            }.map { songs ->
                val songsList = SongsList.from(songs) { song ->
                    stubMan.stubFrom(song)
                }
                SongsCollectionState.CollectionPrepared(songsList)
            }.doOnNext { state ->
                cachedPreparedState = state
            }.mergeWith( Observable.create { emitter ->
                // This way, a new subscriber doesn't have to wait
                // for all the above operations to finish.
                cachedPreparedState?.also { emitter.onNext(it) }
            }).observeOn(AndroidSchedulers.mainThread())
            .also { stateRx = it }


    fun observableSyncState()
        = syncMan.observableState()
            .observeOn(AndroidSchedulers.mainThread())

    fun setSongsOrder(order: SongsOrder) {
        collectMan.order = order
    }

    fun notifySyncPermissionsGranted()
        = syncMan.notifyPermissionsGranted()

    val previousSong: SongWithSettings?
        get() = collectMan.previous

    val nextSong: SongWithSettings?
        get() = collectMan.next


    //////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    init {
        disposable.add(subscribeSync())
        disposable.add(subscribeCollectionPrepared())
    }

    fun dispose() {
        disposable.clear()
        collectMan.dispose()
    }
}