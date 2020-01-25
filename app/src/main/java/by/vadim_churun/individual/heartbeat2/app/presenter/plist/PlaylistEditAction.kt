package by.vadim_churun.individual.heartbeat2.app.presenter.plist

import by.vadim_churun.individual.heartbeat2.app.model.obj.Playlist


/** An action that [PlaylistEditUI] may request [PlaylistEditPresenter] to perform. **/
sealed class PlaylistEditAction {
    class Add(val title: String): PlaylistEditAction()
    class Update(val updatedPlaylist: Playlist): PlaylistEditAction()
}