package by.vadim_churun.individual.heartbeat2.app.presenter.plist


/** Actions that [PlaylistsCollectionUI]
  * may request to perform from [PlaylistsCollectionPresenter]. **/
sealed class PlaylistsCollectionAction {
    class DecodeArt(val playlistID: Int): PlaylistsCollectionAction()
}