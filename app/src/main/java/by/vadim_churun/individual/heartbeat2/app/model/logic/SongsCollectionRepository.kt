package by.vadim_churun.individual.heartbeat2.app.model.logic

import by.vadim_churun.individual.heartbeat2.app.model.logic.internal.*
import by.vadim_churun.individual.heartbeat2.app.model.obj.*
import by.vadim_churun.individual.heartbeat2.app.model.state.PlaylistContentModifState
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
    private val subjectCollectionInternal = PublishSubject.create<SongsList>()

    private fun subscribeSync()
        = Observable.interval(1L, TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .switchMap {
                syncMan.syncIfTime()
                Observable.empty<Unit>()
            }.subscribe()

    private fun subscribeCollectionPrepared()
        = subjectCollectionInternal
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { songs ->
                collectMan.collection = songs
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // PREPARING COLLECTION:

    /** A help class to construct [SongsCollectionState.CollectionPrepared] **/
    private class PreparedHolder(
        var playlistID: Int?,
        var allSongs: SongsList?,
        var playlistSongs: SongsList?,
        var searchQuery: String
    )

    private var cachedPreparedState: SongsCollectionState.CollectionPrepared? = null
    private val subjectPlaylistId = PublishSubject.create<OptionalID>()
    private val subjectSearchQuery = PublishSubject.create<String>()
    private var lastHolder = PreparedHolder(null, null, null, "")
    private var rxPreparedState: Observable<SongsCollectionState>? = null

    private fun observableCollectionPreparedState(): Observable<SongsCollectionState>
        = rxPreparedState ?: dbMan
            .observableSongs()
            .subscribeOn(Schedulers.io())
            .map { songs ->
                val songsList = mapper.songsListFromEntities(songs)
                lastHolder.apply { allSongs = songsList }
            }.mergeWith(
                subjectPlaylistId.doOnNext { optionalID ->
                    lastHolder.playlistID = optionalID.idOrNull
                }.switchMap { optionalID ->
                    optionalID.idOrNull?.let {
                        dbMan.observablePlaylistContent(it)
                            .subscribeOn(Schedulers.io())
                            .map { playlistContent ->
                                val songs = mapper.songsListFromViews(playlistContent)
                                lastHolder.apply { playlistSongs = songs }
                            }
                    } ?: Observable.just(lastHolder)
                }.subscribeOn(Schedulers.computation())
            ).mergeWith(
                subjectSearchQuery.map { query ->
                    lastHolder.apply { searchQuery = query }
                }
            ).observeOn(Schedulers.computation())
            .map<SongsCollectionState> { holder ->
                android.util.Log.v(
                    "HbSongs", "Preparing collection for playlist ${holder.playlistID}" )

                val actualSongs =
                    if(holder.playlistID == null) holder.allSongs!!
                    else holder.playlistSongs!!

                // Signal the full (with no search applied) collection to underlying engines:
                subjectCollectionInternal.onNext(actualSongs)

                // TODO: Filter the songs according to the current search query.
                val filteredSongs = actualSongs

                SongsCollectionState.CollectionPrepared(
                    filteredSongs,
                    if(holder.playlistID == null) null
                        else holder.allSongs!!,
                    holder.playlistID ?: 0
                ).also { cachedPreparedState = it }
            }.mergeWith(
                Observable.create { emitter ->
                    // This way, a new subscriber doesn't have to wait
                    // for all the above operations to finish.
                    cachedPreparedState?.also { emitter.onNext(it) }
                    emitter.onComplete()
                }
            ).also { rxPreparedState = it }


    //////////////////////////////////////////////////////////////////////////////////////////
    // MODIFYING PLAYLIST CONTENT:

    private class PlaylistContentEditHolder(
        val playlistID: Int,
        val removedSongIDs: List<Int>,
        val addedSongIDs: List<Int>
    )

    private val subjectEditPlistContent = PublishSubject.create<PlaylistContentEditHolder>()
    private val subjectPlistContentState = BehaviorSubject.create<PlaylistContentModifState>()

    private fun subscribePlaylistContentEdit()
        = subjectEditPlistContent
            .observeOn(Schedulers.io())
            .doOnNext { holder ->
                subjectPlistContentState.onNext(PlaylistContentModifState.Processing)
                dbMan.updatePlaylistContent(
                    holder.playlistID, holder.removedSongIDs, holder.addedSongIDs )
                subjectPlistContentState.onNext(PlaylistContentModifState.Updated)
                subjectPlaylistId.onNext( OptionalID.wrap(holder.playlistID) )
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // ART DECODE:

    private var subjectDecodeArt = PublishSubject.create<Song>()

    fun requestArtDecode(song: Song) {
        subjectDecodeArt.onNext(song)
    }

    private fun observableArtDecodedState(): Observable<SongsCollectionState>
        = subjectDecodeArt.observeOn(Schedulers.io())
            .concatMap { song ->
                val source = sourcesMan.sourceByCode(song.sourceCode)
                try {
                    val art = source.artFor(song)
                    art?.let {
                        SongsCollectionState.ArtDecoded(song.ID, it)
                    }?.let { Observable.just(it) }
                    ?: Observable.empty<SongsCollectionState>()
                } catch(thr: Throwable) {
                    Observable.just(SongsCollectionState.ArtDecodeFailed(song.ID))
                }
            }.observeOn(AndroidSchedulers.mainThread())


    //////////////////////////////////////////////////////////////////////////////////////////
    // API:


    fun observableState(): Observable<SongsCollectionState>
        = observableCollectionPreparedState()
            .mergeWith(observableArtDecodedState())
            .observeOn(AndroidSchedulers.mainThread())

    fun observableSyncState()
        = syncMan.observableState()
            .observeOn(AndroidSchedulers.mainThread())

    fun observablePlaylistContentState()
        = subjectPlistContentState.observeOn(AndroidSchedulers.mainThread())


    fun setSongsOrder(order: SongsOrder) {
        collectMan.order = order
    }

    fun submitSyncPermissionsResult(sourceCode: Byte, granted: Boolean)
        = syncMan.submitPermissionsResult(sourceCode, granted)

    fun openPlaylist(playlistID: OptionalID)
        = subjectPlaylistId.onNext(playlistID)

    fun updatePlaylistContent
    (playlistID: Int, removedSongIDs: List<Int>, addedSongIDs: List<Int>) {
        subjectEditPlistContent.onNext(
            PlaylistContentEditHolder(playlistID, removedSongIDs, addedSongIDs) )
    }

    val previousSong: SongWithSettings?
        get() = collectMan.previous

    val nextSong: SongWithSettings?
        get() = collectMan.next


    //////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    init {
        disposable.addAll(
            subscribeSync(),
            subscribeCollectionPrepared(),
            subscribePlaylistContentEdit()
        )
    }

    fun dispose() {
        disposable.clear()
        collectMan.dispose()
    }
}