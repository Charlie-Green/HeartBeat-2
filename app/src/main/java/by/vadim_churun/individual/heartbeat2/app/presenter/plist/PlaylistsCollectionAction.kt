package by.vadim_churun.individual.heartbeat2.app.presenter.plist

import by.vadim_churun.individual.heartbeat2.app.model.obj.*


/** Actions that [PlaylistsCollectionUI]
  * may request to perform from [PlaylistsCollectionPresenter]. **/
sealed class PlaylistsCollectionAction {
    class OpenPlaylist(val plistID: OptionalID): PlaylistsCollectionAction()
    class DecodeArt(val plistHeader: PlaylistHeader): PlaylistsCollectionAction()
}