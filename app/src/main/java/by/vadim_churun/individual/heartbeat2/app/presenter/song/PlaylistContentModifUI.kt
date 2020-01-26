package by.vadim_churun.individual.heartbeat2.app.presenter.song

import by.vadim_churun.individual.heartbeat2.app.model.state.PlaylistContentModifState
import io.reactivex.Observable


interface PlaylistContentModifUI {
    fun intentUpdateContent(): Observable<PlaylistContentModifAction.UpdateContent>
    fun render(state: PlaylistContentModifState)
}