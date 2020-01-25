package by.vadim_churun.individual.heartbeat2.app.model.logic

import by.vadim_churun.individual.heartbeat2.app.model.logic.internal.*
import by.vadim_churun.individual.heartbeat2.app.model.obj.*
import by.vadim_churun.individual.heartbeat2.app.model.state.*
import javax.inject.Inject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class PlaylistsCollectionRepository @Inject constructor(
    private val dbMan: DatabaseManager,
    private val mapper: Mapper
) {
    ////////////////////////////////////////////////////////////////////////////////////////
    // COLLECTION STATE:

    private var rxState: Observable<out PlaylistsCollectionState>? = null

    fun observableState()
        = rxState ?: dbMan.observablePlaylistHeaders()
            .subscribeOn(Schedulers.io())
            .map { entities ->
                entities.map { headerEntity ->
                    PlaylistHeader.fromEntity(headerEntity)
                }
            }.map<PlaylistsCollectionState> { headers ->
                PlaylistsCollection.from(headers)
                    .let { PlaylistsCollectionState.Prepared(it) }
            }.startWith( PlaylistsCollectionState.Preparing )
            .observeOn(AndroidSchedulers.mainThread())
            .also { rxState = it }


    ////////////////////////////////////////////////////////////////////////////////////////
    // EDIT STATE:

    private val subjectAdd = PublishSubject.create<String>()
    private val subjectUpdate = PublishSubject.create<Playlist>()
    private val subjectEditState = PublishSubject.create<PlaylistEditState>()

    fun add(title: String) {
        subjectAdd.onNext(title)
    }

    fun update(playlist: Playlist) {
        subjectUpdate.onNext(playlist)
    }

    fun observableEditState(playlistID: Int)
        = subjectAdd.observeOn(Schedulers.io())
            .distinctUntilChanged()
            .map { title ->
                subjectEditState.onNext(PlaylistEditState.Processing)
                val playlist = mapper.playlistEntity(title)
                if(dbMan.addPlaylistIfNew(playlist))
                    PlaylistEditState.Added(title)
                else
                    PlaylistEditState.AddRefused(
                        title, PlaylistEditState.AddRefused.Reason.TITLE_EXISTS )
            }.mergeWith(
                subjectUpdate.observeOn(Schedulers.io())
                    .map { plist ->
                        subjectEditState.onNext(PlaylistEditState.Processing)
                        val plistEntity = mapper.playlistEntity(plist)
                        dbMan.addOrUpdatePlaylist(plistEntity)
                        PlaylistEditState.Updated(plist.title)
                    }
            ).mergeWith(
                dbMan.observablePlaylist(playlistID)
                    .map { playlistEntity ->
                        val plist = mapper.playlist(playlistEntity)
                        PlaylistEditState.LastPlaylistAvailable(plist)
                    }.subscribeOn(Schedulers.io())
            ).mergeWith(subjectEditState)
            .observeOn(AndroidSchedulers.mainThread())
}