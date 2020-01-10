package by.vadim_churun.individual.heartbeat2.presenter.control

import android.content.Context
import by.vadim_churun.individual.heartbeat2.presenter.PresenterUtils
import by.vadim_churun.individual.heartbeat2.service.*
import io.reactivex.disposables.CompositeDisposable


/** MVI Presenter for [MediaControlUI]. **/
object MediaControlPresenter {
    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribePlayPause(ui: MediaControlUI)
        = ui.playPauseIntent()
            .doOnNext {
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.playOrPause()
                }
            }.subscribe()

    private fun subscribeStop(ui: MediaControlUI)
        = ui.stopIntent()
            .doOnNext {
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.stopPlayback()
                }
            }.subscribe()

    private fun subscribeReplay(ui: MediaControlUI)
        = ui.replayIntent()
            .doOnNext {
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.replayCurrentSong()
                }
            }.subscribe()

    private fun subscribeSeek(ui: MediaControlUI)
        = ui.seekIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.seek(action.position)
                }
            }.subscribe()

    private fun subscribeSetRate(ui: MediaControlUI)
        = ui.setRateIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.setPlaybackRate(action.rate)
                }
            }.subscribe()

    private fun subscribeSetVolume(ui: MediaControlUI)
        = ui.setVolumeIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.setVolume(action.volume)
                }
            }.subscribe()

    private fun subscribeSetPriority(ui: MediaControlUI)
        = ui.setPriorityIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.setCurrentSongPriority(action.priority)
                }
            }.subscribe()

    private fun subscribeSetSongsOrder(ui: MediaControlUI)
        = ui.setSongsOrderIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.setSongsOrder(action.order)
                }
            }.subscribe()

    private fun subscribeRequestPrevious(ui: MediaControlUI)
        = ui.requestPreviousIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.playPrevious()
                }
            }.subscribe()

    private fun subscribeRequestNext(ui: MediaControlUI)
        = ui.requestNextIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(mediaBinder) { service ->
                    service.playNext()
                }
         }.subscribe()

    private fun subscribeState(service: HeartBeatMediaService, ui: MediaControlUI)
        = service.observablePlaybackState()
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // BIND/UNBIND:

    private var bound = false
    private val mediaBinder = MediaServiceBinder()


    fun bind(context: Context, ui: MediaControlUI) {
        if(bound) return
        bound = true

        mediaBinder.bind(context.applicationContext) { service ->
            disposable.add(subscribeState(service, ui))
        }
        disposable.addAll(
            subscribeStop(ui),
            subscribeSeek(ui),
            subscribeReplay(ui),
            subscribeSetRate(ui),
            subscribePlayPause(ui),
            subscribeSetVolume(ui),
            subscribeRequestNext(ui),
            subscribeSetPriority(ui),
            subscribeSetSongsOrder(ui),
            subscribeRequestPrevious(ui)
        )
    }

    fun unbind(context: Context) {
        if(!bound) return
        bound = false
        disposable.dispose()
        mediaBinder.unbind(context.applicationContext)
    }
}