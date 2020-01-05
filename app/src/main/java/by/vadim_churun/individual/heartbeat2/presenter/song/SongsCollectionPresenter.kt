package by.vadim_churun.individual.heartbeat2.presenter.song

import android.content.Context
import by.vadim_churun.individual.heartbeat2.service.MediaServiceBinder
import io.reactivex.disposables.CompositeDisposable


object SongsCollectionPresenter {
    private val disposable = CompositeDisposable()
    private val serviceBinder = MediaServiceBinder()
    private var bound = false

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
        if(bound) return
        bound = true

        serviceBinder.bind(context.applicationContext)
        disposable.addAll(
            subscribePlay(ui),
            subscribeDecodeArt(ui),
            subscribeSetPriority(ui)
        )
    }

    fun unbind(context: Context) {
        if(!bound) return
        bound = false

        disposable.clear()
        serviceBinder.unbind(context.applicationContext)
    }
}