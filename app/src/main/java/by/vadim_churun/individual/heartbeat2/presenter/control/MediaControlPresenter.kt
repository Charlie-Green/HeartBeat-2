package by.vadim_churun.individual.heartbeat2.presenter.control

import android.content.Context
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.entity.Song
import by.vadim_churun.individual.heartbeat2.presenter.common.SongStub
import io.reactivex.disposables.CompositeDisposable


/** MVI Presenter for [MediaControlUI]. **/
object MediaControlPresenter {

    private fun debugLog(message: String)
        = android.util.Log.v("MediaControl", message)


    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribePlayPause(context: Context, ui: MediaControlUI)
        = ui.playPauseIntent()
            .doOnNext {
                debugLog("Play/Pause")
            }.subscribe()

    private fun subscribeStop(context: Context, ui: MediaControlUI)
        = ui.stopIntent()
            .doOnNext {
                debugLog("Stop")
            }.subscribe()

    private fun subscribeReplay(context: Context, ui: MediaControlUI)
        = ui.replayIntent()
            .doOnNext {
                debugLog("Replay")
            }.subscribe()

    private fun subscribeSeek(context: Context, ui: MediaControlUI)
        = ui.seekIntent()
            .doOnNext { action ->
                debugLog("Seek: ${action.position}")
            }.subscribe()

    private fun subscribeSetRate(context: Context, ui: MediaControlUI)
        = ui.setRateIntent()
            .doOnNext { action ->
                debugLog("Set rate: ${action.rate}")
            }.subscribe()

    private fun subscribeSetVolume(context: Context, ui: MediaControlUI)
        = ui.setVolumeIntent()
            .doOnNext { action ->
                debugLog("Set volume: ${action.volume}")
            }.subscribe()

    private fun subscribeSetPriority(context: Context, ui: MediaControlUI)
        = ui.setPriorityIntent()
            .doOnNext { action ->
                debugLog("Set priority: ${action.priority}")
            }.subscribe()

    private fun subscribeSetSongsOrder(context: Context, ui: MediaControlUI)
        = ui.setSongsOrderIntent()
            .doOnNext { action ->
                debugLog("Set songs order: ${action.order.name}")
            }.subscribe()

    private fun subscribeRequestPrevious(context: Context, ui: MediaControlUI)
        = ui.requestPreviousIntent()
            .doOnNext { action ->
                debugLog("Previous, please!")
            }.subscribe()

    private fun subscribeRequestNext(context: Context, ui: MediaControlUI)
        = ui.requestNextIntent()
            .doOnNext { action ->
                debugLog("Next, please!")
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


    //////////////////////////////////////////////////////////////////////////////////////////
    // OTHER:

    fun stubForSong(appContext: Context, song: Song): SongStub {
        // TODO: The implementation is to be more complicated...
        return SongStub(
            song.title,
            song.artist ?: appContext.getString(R.string.unknown_artist)
        )
    }
}