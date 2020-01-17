package by.vadim_churun.individual.heartbeat2.app.presenter.song

import by.vadim_churun.individual.heartbeat2.app.model.state.*
import io.reactivex.Observable


interface SongsCollectionUI {
    fun playIntent(): Observable<SongsCollectionAction.Play>
    fun decodeArtIntent(): Observable<SongsCollectionAction.DecodeArt>
    fun setPriorityIntent(): Observable<SongsCollectionAction.SetPriority>
    fun missingPermissionsGrantedIntent():
        Observable<SongsCollectionAction.NotifyPermissionsGranted>
    fun render(state: SongsCollectionState)
    fun render(state: SyncState)
    fun render(state: PlaybackState)
}