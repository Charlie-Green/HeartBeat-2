package by.vadim_churun.individual.heartbeat2.app.model.logic

import by.vadim_churun.individual.heartbeat2.app.model.logic.internal.DatabaseManager
import by.vadim_churun.individual.heartbeat2.app.model.obj.*
import by.vadim_churun.individual.heartbeat2.app.model.state.PlaylistsCollectionState
import javax.inject.Inject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class PlaylistsCollectionRepository @Inject constructor(
    private val dbMan: DatabaseManager
) {
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
}