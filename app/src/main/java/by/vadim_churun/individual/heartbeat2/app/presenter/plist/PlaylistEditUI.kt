package by.vadim_churun.individual.heartbeat2.app.presenter.plist

import by.vadim_churun.individual.heartbeat2.app.model.state.PlaylistEditState
import io.reactivex.Observable


interface PlaylistEditUI {
    val playlistID: Int
    fun intentAdd(): Observable<PlaylistEditAction.Add>
    fun intentUpdate(): Observable<PlaylistEditAction.Update>
    fun render(state: PlaylistEditState)
}