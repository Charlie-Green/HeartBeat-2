package by.vadim_churun.individual.heartbeat2.model.logic

import by.vadim_churun.individual.heartbeat2.model.state.SongsCollectionState
import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/** One of the top-level classes of MVI's "model" layer,
  * the one which is for a collection of songs. **/
class SongsRepository @Inject constructor(
    private val dbMan: DatabaseManager,
    private val syncMan: SyncManager,
    private val stateMan: StatesManager
) {
    //////////////////////////////////////////////////////////////////////////////////////////
    // INTERNAL:

    private val disposable = CompositeDisposable()

    private fun subscribeSync()
        = Observable.interval(1L, TimeUnit.SECONDS)
            .switchMap {
                syncMan.syncIfTime()
                Observable.empty<Unit>()
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // API:

    fun observableSongsCollectionState(): Observable<out SongsCollectionState>
        = dbMan.observableSongs()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map { songEntities ->
                // TODO: Filter, sort, etc.
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
            }.doOnNext { songs ->
                // TODO: Set the new songs collection to SongsCollectionManager.
            }.map { songs ->
                stateMan.songsCollectionPrepared(songs)
            }.observeOn(AndroidSchedulers.mainThread())


            //////////////////////////////////////////////////////////////////////////////////////////
            // LIFECYCLE:

            init {
                disposable.add(subscribeSync())
            }

            fun dispose() {
                disposable.clear()
            }
}