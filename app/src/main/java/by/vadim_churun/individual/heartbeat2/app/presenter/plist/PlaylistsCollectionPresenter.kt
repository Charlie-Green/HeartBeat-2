package by.vadim_churun.individual.heartbeat2.app.presenter.plist

import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import io.reactivex.disposables.CompositeDisposable


/** MVI Presenter for [PlaylistsCollectionUI] **/
class PlaylistsCollectionPresenter {
    private val disposable = CompositeDisposable()
    private var bound = false

    private fun subscribeDecodeArt(service: HeartBeatMediaService, ui: PlaylistsCollectionUI)
        = ui.decodeArtIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeState(service: HeartBeatMediaService, ui: PlaylistsCollectionUI)
        = Unit  // TODO


    fun bind(service: HeartBeatMediaService, ui: PlaylistsCollectionUI) {
        if(bound) return
        bound = true
        disposable.addAll( subscribeDecodeArt(service, ui) )
    }

    fun unbind() {
        // TODO
    }
}