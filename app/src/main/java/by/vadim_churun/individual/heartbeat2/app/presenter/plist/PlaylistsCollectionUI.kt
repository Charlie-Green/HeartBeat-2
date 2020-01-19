package by.vadim_churun.individual.heartbeat2.app.presenter.plist

import by.vadim_churun.individual.heartbeat2.app.model.state.PlaylistsCollectionState
import io.reactivex.Observable


interface PlaylistsCollectionUI {
    fun decodeArtIntent(): Observable<PlaylistsCollectionAction.DecodeArt>
    fun render(state: PlaylistsCollectionState)
}