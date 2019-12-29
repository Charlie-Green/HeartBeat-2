package by.vadim_churun.individual.heartbeat2.presenter.song

import by.vadim_churun.individual.heartbeat2.entity.Song


/** Actions that [SongsCollectionUI] may request [SongsCollectionPresenter] to perform. **/
sealed class SongsCollectionAction {
    class DecodeArt(val song: Song): SongsCollectionAction()
    class Play(val song: Song): SongsCollectionAction()
    class SetPriority(val songID: Int, newPriority: Byte): SongsCollectionAction()
}