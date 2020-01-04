package by.vadim_churun.individual.heartbeat2.presenter.song

import by.vadim_churun.individual.heartbeat2.shared.Song


/** Actions that [SongsCollectionUI] may request [SongsCollectionPresenter] to perform. **/
sealed class SongsCollectionAction {
    class DecodeArt(val song: by.vadim_churun.individual.heartbeat2.shared.Song): SongsCollectionAction()
    class Play(val song: by.vadim_churun.individual.heartbeat2.shared.Song): SongsCollectionAction()
    class SetPriority(val songID: Int, newPriority: Byte): SongsCollectionAction()
}