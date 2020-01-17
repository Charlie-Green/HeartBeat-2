package by.vadim_churun.individual.heartbeat2.app.presenter.control

import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import io.reactivex.disposables.CompositeDisposable


/** MVI Presenter for [MediaControlUI]. **/
class MediaControlPresenter {
    /////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeStop(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.stopIntent()
            .doOnNext {
                service.stopPlayback()
            }.subscribe()

    private fun subscribeSeek(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.seekIntent()
            .doOnNext { action ->
                service.seek(action.position)
            }.subscribe()

    private fun subscribeReplay(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.replayIntent()
            .doOnNext {
                service.replayCurrentSong()
            }.subscribe()

    private fun subscribeSetRate(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.setRateIntent()
            .doOnNext { action ->
                service.setPlaybackRate(action.rate)
            }.subscribe()

    private fun subscribeSetVolume(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.setVolumeIntent()
            .doOnNext { action ->
                service.setVolume(action.volume)
            }.subscribe()

    private fun subscribePlayPause(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.playPauseIntent()
            .doOnNext {
                service.playOrPause()
            }.subscribe()

    private fun subscribeNext(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.requestNextIntent()
            .doOnNext {
                service.playNext()
            }.subscribe()

    private fun subscribeSetPriority(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.setPriorityIntent()
            .doOnNext { action ->
                service.setCurrentSongPriority(action.priority)
            }.subscribe()

    private fun subscribeSetSongsOrder(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.setSongsOrderIntent()
            .doOnNext { action ->
                service.setSongsOrder(action.order)
            }.subscribe()

    private fun subscribePrevious(service: HeartBeatMediaService, ui: MediaControlUI)
        = ui.requestPreviousIntent()
            .doOnNext {
                service.playPrevious()
            }.subscribe()

    private fun subscribeState(service: HeartBeatMediaService, ui: MediaControlUI)
        = service.observablePlaybackState()
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////
    // BIND/UNBIND:

    private var bound = false

    fun bind(service: HeartBeatMediaService, ui: MediaControlUI) {
        if(bound) return
        bound = true

        disposable.addAll(
            subscribeStop(service, ui),
            subscribeSeek(service, ui),
            subscribeNext(service, ui),
            subscribeReplay(service, ui),
            subscribeSetRate(service, ui),
            subscribePrevious(service, ui),
            subscribeSetVolume(service, ui),
            subscribePlayPause(service, ui),
            subscribeSetPriority(service, ui),
            subscribeSetSongsOrder(service, ui),
            subscribeState(service, ui)
        )
    }

    fun unbind() {
        bound = false
        disposable.clear()
    }
}