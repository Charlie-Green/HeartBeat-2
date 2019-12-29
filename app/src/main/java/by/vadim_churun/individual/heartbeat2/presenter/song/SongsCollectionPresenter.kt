package by.vadim_churun.individual.heartbeat2.presenter.song

import android.content.Context
import io.reactivex.disposables.CompositeDisposable


object SongsCollectionPresenter {
    val disposable = CompositeDisposable()
    var bound = false

    private fun subscribePlay(ui: SongsCollectionUI)
        = ui.playIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeDecodeArt(ui: SongsCollectionUI)
        = ui.decodeArtIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()

    private fun subscribeSetPriority(ui: SongsCollectionUI)
        = ui.setPriorityIntent()
            .doOnNext { action ->
                // TODO
            }.subscribe()


    fun bind(context: Context, ui: SongsCollectionUI) {
        // TODO: Bind the Service.
        disposable.addAll(
            subscribePlay(ui),
            subscribeDecodeArt(ui),
            subscribeSetPriority(ui)
        )
    }

    fun unbind(context: Context) {
        disposable.clear()
        // TODO: Unbind the Service.
    }
}