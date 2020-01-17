package by.vadim_churun.individual.heartbeat2.app.presenter.song

import by.vadim_churun.individual.heartbeat2.shared.Song
import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings


/** Actions that [SongsCollectionUI] may request [SongsCollectionPresenter] to perform. **/
sealed class SongsCollectionAction {
    class DecodeArt(val song: Song): SongsCollectionAction()
    class Play(val song: SongWithSettings): SongsCollectionAction()
    class SetPriority(val songID: Int, val newPriority: Byte): SongsCollectionAction()
    object NotifyPermissionsGranted: SongsCollectionAction()
}