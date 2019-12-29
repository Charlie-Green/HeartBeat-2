package by.vadim_churun.individual.heartbeat2.presenter.control

import android.content.Context
import io.reactivex.disposables.CompositeDisposable


/** MVI Presenter for [MediaControlUI]. **/
object MediaControlPresenter {
    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribePlayPause(context: Context, ui: MediaControlUI)
        = ui.playPauseIntent()
            .doOnNext {
                // TODO
            }.subscribe()

    private fun subscribeStop(context: Context, ui: MediaControlUI)
        = ui.stopIntent()
            .doOnNext {
                // TODO
            }.subscribe()

    private fun subscribeReplay(context: Context, ui: MediaControlUI)
        = ui.replayIntent()
            .doOnNext {
                // TODO
            }.subscribe()

    private fun subscribeSeek(context: Context, ui: MediaControlUI)
        = ui.seekIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeSetRate(context: Context, ui: MediaControlUI)
        = ui.setRateIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeSetVolume(context: Context, ui: MediaControlUI)
        = ui.setVolumeIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeSetPriority(context: Context, ui: MediaControlUI)
        = ui.setPriorityIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeSetSongsOrder(context: Context, ui: MediaControlUI)
        = ui.setSongsOrderIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeRequestPrevious(context: Context, ui: MediaControlUI)
        = ui.requestPreviousIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeRequestNext(context: Context, ui: MediaControlUI)
        = ui.requestNextIntent()
            .doOnNext { action ->
                // TODO
         }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // BIND/UNBIND:

    private var bound = false


    fun bind(context: Context, ui: MediaControlUI) {
        if(bound) return
        bound = true

        // TODO: Bind the service.
        disposable.addAll(
            subscribeStop(context, ui),
            subscribeSeek(context, ui),
            subscribeReplay(context, ui),
            subscribeSetRate(context, ui),
            subscribePlayPause(context, ui),
            subscribeSetVolume(context, ui),
            subscribeRequestNext(context, ui),
            subscribeSetPriority(context, ui),
            subscribeSetSongsOrder(context, ui),
            subscribeRequestPrevious(context, ui)
        )
    }

    fun unbind(context: Context) {
        if(!bound) return
        bound = false
        disposable.dispose()
        // TODO: Unbind the service.
    }
}