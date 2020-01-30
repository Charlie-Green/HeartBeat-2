package by.vadim_churun.individual.heartbeat2.app.presenter.plist

import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import io.reactivex.disposables.CompositeDisposable


/** MVI Presenter for [PlaylistsCollectionUI] **/
class PlaylistsCollectionPresenter {
    private val disposable = CompositeDisposable()

    private fun subscribeOpenPlaylist(service: HeartBeatMediaService, ui: PlaylistsCollectionUI)
        = ui.openPlaylistIntent()
            .doOnNext { action ->
                service.openPlaylist(action.plistID)
            }.subscribe()

    private fun subscribeDecodeArt(service: HeartBeatMediaService, ui: PlaylistsCollectionUI)
        = ui.decodeArtIntent()
            .doOnNext { action ->
                service.requestArtDecode(action.plistHeader)
            }.subscribe()

    private fun subscribeSearch(service: HeartBeatMediaService, ui: PlaylistsCollectionUI)
        = ui.searchIntent()
            .doOnNext { query ->
                service.setPlaylistsSearchQuery(query)
            }.subscribe()

    private fun subscribeState(service: HeartBeatMediaService, ui: PlaylistsCollectionUI)
        = service.observablePlaylistsCollectionState()
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()


    private var bound = false

    fun bind(service: HeartBeatMediaService, ui: PlaylistsCollectionUI) {
        if(bound) return
        bound = true

        disposable.addAll(
            subscribeOpenPlaylist(service, ui),
            subscribeDecodeArt(service, ui),
            subscribeSearch(service, ui),
            subscribeState(service, ui)
        )
    }

    fun unbind() {
        if(!bound) return
        bound = false
        disposable.clear()
    }
}