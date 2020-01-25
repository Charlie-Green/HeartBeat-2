package by.vadim_churun.individual.heartbeat2.app.presenter.plist

import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import io.reactivex.disposables.CompositeDisposable


/** MVI Presenter for [PlaylistEditUI]. **/
class PlaylistEditPresenter {
    ///////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeAdd(service: HeartBeatMediaService, ui: PlaylistEditUI)
        = ui.intentAdd()
            .doOnNext { action ->
                service.addPlaylist(action.title)
            }.subscribe()

    private fun subscribeUpdate(service: HeartBeatMediaService, ui: PlaylistEditUI)
        = ui.intentUpdate()
            .doOnNext { action ->
                service.updatePlaylist(action.updatedPlaylist)
            }.subscribe()

    private fun subscribeState(service: HeartBeatMediaService, ui: PlaylistEditUI)
        = service.observablePlaylistEditState(ui.playlistID)
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()


    ///////////////////////////////////////////////////////////////////////////////////////////
    // BIND/UNBIND:

    private var bound = false

    fun bind(service: HeartBeatMediaService, ui: PlaylistEditUI) {
        if(bound) return
        bound = true

        disposable.addAll(
            subscribeAdd(service, ui),
            subscribeUpdate(service, ui),
            subscribeState(service, ui)
        )
    }

    fun unbind() {
        bound = false
        disposable.clear()
    }
}