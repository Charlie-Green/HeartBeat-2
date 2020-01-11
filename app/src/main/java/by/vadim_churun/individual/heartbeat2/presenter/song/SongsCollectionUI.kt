package by.vadim_churun.individual.heartbeat2.presenter.song

import by.vadim_churun.individual.heartbeat2.model.state.SongsCollectionState
import by.vadim_churun.individual.heartbeat2.model.state.SyncState
import io.reactivex.Observable


interface SongsCollectionUI {
    fun playIntent(): Observable<SongsCollectionAction.Play>
    fun decodeArtIntent(): Observable<SongsCollectionAction.DecodeArt>
    fun setPriorityIntent(): Observable<SongsCollectionAction.SetPriority>
    fun render(state: SongsCollectionState)
    fun render(state: SyncState)
}