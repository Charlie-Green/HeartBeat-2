package by.vadim_churun.individual.heartbeat2.app.presenter.song

import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class PlaylistContentModifPresenter {
    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeUpdate(service: HeartBeatMediaService, ui: PlaylistContentModifUI)
        = ui.intentUpdateContent()
            .observeOn(Schedulers.computation())
            .doOnNext { action ->
                val removedIds = mutableListOf<Int>()
                val addedIds = mutableListOf<Int>()
                for(entry in action.userChecks) {
                    val songID = entry.key
                    val destination = if(entry.value == true) addedIds else removedIds
                    destination.add(songID)
                }

                service.updatePlaylistContent(action.playlistID, removedIds, addedIds)
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe()

    private fun subscribeState(service: HeartBeatMediaService, ui: PlaylistContentModifUI)
        = service.observablePlaylistContentModifState()
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // BIND/UNBIND:

    private var bound = false

    fun bind(service: HeartBeatMediaService, ui: PlaylistContentModifUI) {
        if(bound) return
        bound = true

        disposable.addAll(
            subscribeUpdate(service, ui),
            subscribeState(service, ui)
        )
    }

    fun unbind() {
        bound = false
        disposable.clear()
    }
}