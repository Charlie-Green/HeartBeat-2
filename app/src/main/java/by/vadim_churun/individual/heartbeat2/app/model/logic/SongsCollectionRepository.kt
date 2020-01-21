package by.vadim_churun.individual.heartbeat2.app.model.logic

import android.util.Log
import by.vadim_churun.individual.heartbeat2.app.model.logic.internal.*
import by.vadim_churun.individual.heartbeat2.app.model.obj.*
import by.vadim_churun.individual.heartbeat2.app.model.state.SongsCollectionState
import by.vadim_churun.individual.heartbeat2.shared.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/** One of the highest level classes of MVI's "model" layer,
  * the one which manages current collection of songs. **/
class SongsCollectionRepository @Inject constructor(
    private val collectMan: SongsCollectionManager,
    private val dbMan: DatabaseManager,
    private val syncMan: SyncManager,
    private val stubMan: SongStubsManager,
    private val sourcesMan: SongsSourcesManager,
    private val mapper: Mapper
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
        = observableCollectionPreparedState()
            .doOnNext { state ->
                if(state is SongsCollectionState.CollectionPrepared)
                    collectMan.collection = state.songs
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // PREPARING COLLECTION:

    private var cachedPreparedState: SongsCollectionState.CollectionPrepared? = null
    private val subjectPlaylistId = BehaviorSubject.create<OptionalID>()
    private var rxPreparedState: Observable<SongsCollectionState>? = null

    private fun observablePlaylistContent(
        playlistID: OptionalID
    ): Observable< List<SongWithSettings> >
        = playlistID.idOrNull?.let {
            dbMan.observablePlaylistContent(it)  // Songs from a specific playlist
                .map { songsInPlaylist ->
                    // List<SongInPlaylistView> -> List<SongWithSettings>
                    songsInPlaylist.map { songInPlaylist ->
                        mapper.songWithSettings(songInPlaylist)
                    }
                }.subscribeOn(Schedulers.io())
        } ?: dbMan.observableSongs()            // All songs.
            .map { songEntities ->
                // List<SongEntity> -> List<SongWithSettings>
                songEntities.map { songEntity ->
                    mapper.songWithSettings(songEntity)
                }
            }.subscribeOn(Schedulers.io())

    private fun observableCollectionPreparedState(): Observable<SongsCollectionState>
        = rxPreparedState ?: subjectPlaylistId
            .startWith( OptionalID.wrap(null) )
            .distinctUntilChanged { oid1, oid2 ->
                oid1.idOrNull == oid2.idOrNull
            }.map { optionalID ->
                Log.v("HbPlist", "Opening playlist ${optionalID.idOrNull}")
                observablePlaylistContent(optionalID)
            }.let {
                // Forget about the old playlist when a new one gets opened.
                Observable.switchOnNext(it)
            }.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map<SongsCollectionState> { songs ->
                val songsList = SongsList.from(songs) { song ->
                    stubMan.stubFrom(song)
                }
                SongsCollectionState.CollectionPrepared(songsList)
                    .also { cachedPreparedState = it }
            }.mergeWith( Observable.create { emitter ->
                // This way, a new subscriber doesn't have to wait
                // for all the above operations to finish.
                cachedPreparedState?.also {
                    emitter.onNext(it)
                } ?: emitter.onNext(SongsCollectionState.Preparing)
            }).also { rxPreparedState = it }


    //////////////////////////////////////////////////////////////////////////////////////////
    // ART DECODE:

    private var subjectDecodeArt = PublishSubject.create<Song>()

    fun requestArtDecode(song: Song) {
        subjectDecodeArt.onNext(song)
    }

    private fun observableArtDecodedState(): Observable<SongsCollectionState>
        = subjectDecodeArt.observeOn(Schedulers.io())
            .concatMap { song ->
                sourcesMan.metaFor(song.sourceClass)
                    .source
                    .artFor(song)
                    ?.let {
                        SongsCollectionState.ArtDecoded(song.ID, it) as SongsCollectionState
                    }?.let { Observable.just(it) }
                    ?: Observable.empty<SongsCollectionState>()
            }


    //////////////////////////////////////////////////////////////////////////////////////////
    // API:


    fun observableState(): Observable<SongsCollectionState>
        = observableCollectionPreparedState()
            .mergeWith(observableArtDecodedState())
            .observeOn(AndroidSchedulers.mainThread())


    fun observableSyncState()
        = syncMan.observableState()
            .observeOn(AndroidSchedulers.mainThread())

    fun setSongsOrder(order: SongsOrder) {
        collectMan.order = order
    }

    fun notifySyncPermissionsGranted()
        = syncMan.notifyPermissionsGranted()

    fun openPlaylist(playlistID: OptionalID)
        = subjectPlaylistId.onNext(playlistID)

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