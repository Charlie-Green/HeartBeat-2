package by.vadim_churun.individual.heartbeat2.app.presenter.plist


/** An action that [PlaylistEditUI] may request [PlaylistEditPresenter] to perform. **/
sealed class PlaylistEditAction {
    class Add(val title: String): PlaylistEditAction()
    class Update(val id: Int, val title: String): PlaylistEditAction()
}