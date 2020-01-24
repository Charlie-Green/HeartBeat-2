package by.vadim_churun.individual.heartbeat2.app.presenter.song

import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import io.reactivex.disposables.CompositeDisposable


/** MVI Presenter for [SongsCollectionUI]. **/
class SongsCollectionPresenter {
    ////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribePlay(service: HeartBeatMediaService, ui: SongsCollectionUI)
        = ui.playIntent()
            .doOnNext { action ->
                service.play(action.song)
            }.subscribe()

    private fun subscribeDecodeArt(service: HeartBeatMediaService, ui: SongsCollectionUI)
        = ui.decodeArtIntent()
            .doOnNext { action ->
                service.requestArtDecode(action.song)
            }.subscribe()

    private fun subscribeSetPriority(service: HeartBeatMediaService, ui: SongsCollectionUI)
        = ui.setPriorityIntent()
            .doOnNext { action ->
                service.setSongPriority(action.songID, action.newPriority)
            }.subscribe()

    private fun subscribePermissions(service: HeartBeatMediaService, ui: SongsCollectionUI)
        = ui.submitPermissionsResultIntent()
            .doOnNext { action ->
                service.submitSyncPermissionsResult(action.sourceCode, action.granted)
            }.subscribe()

    private fun subscribeCollectionState(service: HeartBeatMediaService, ui: SongsCollectionUI)
        = service.observableSongsCollectionState()
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()

    private fun subscribeSyncState(service: HeartBeatMediaService, ui: SongsCollectionUI)
        = service.observableSyncState()
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()

    private fun subscribePlaybackState(service: HeartBeatMediaService, ui: SongsCollectionUI)
        = service.observablePlaybackState()
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()


    ////////////////////////////////////////////////////////////////////////////////////////////
    // BIND/UNBIND:

    private var bound = false

    fun bind(service: HeartBeatMediaService, ui: SongsCollectionUI) {
        if(bound) return
        bound = true

        disposable.addAll(
            subscribePlay(service, ui),
            subscribeDecodeArt(service, ui),
            subscribeSetPriority(service, ui),
            subscribePermissions(service, ui),
            subscribeCollectionState(service, ui),
            subscribeSyncState(service, ui),
            subscribePlaybackState(service, ui)
        )
    }

    fun unbind() {
        bound = false
        disposable.clear()
    }
}