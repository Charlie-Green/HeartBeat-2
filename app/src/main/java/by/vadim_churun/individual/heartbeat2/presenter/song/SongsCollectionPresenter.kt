package by.vadim_churun.individual.heartbeat2.presenter.song

import android.content.Context
import by.vadim_churun.individual.heartbeat2.presenter.PresenterUtils
import by.vadim_churun.individual.heartbeat2.service.HeartBeatMediaService
import by.vadim_churun.individual.heartbeat2.service.MediaServiceBinder
import io.reactivex.disposables.CompositeDisposable


object SongsCollectionPresenter {
    private val disposable = CompositeDisposable()
    private val serviceBinder = MediaServiceBinder()
    private var bound = false

    private fun subscribePlay(ui: SongsCollectionUI)
        = ui.playIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(serviceBinder) { service ->
                    service.play(action.song)
                }
            }.subscribe()

    private fun subscribeDecodeArt(ui: SongsCollectionUI)
        = ui.decodeArtIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(serviceBinder) { service ->
                    service.requestArtDecode(action.song)
                }
            }.subscribe()

    private fun subscribeSetPriority(ui: SongsCollectionUI)
        = ui.setPriorityIntent()
            .doOnNext { action ->
                PresenterUtils.interactMediaService(serviceBinder) { service ->
                    service.setSongPriority(action.songID, action.newPriority)
                }
            }.subscribe()

    private fun subscribeState(service: HeartBeatMediaService, ui: SongsCollectionUI)
        = service.observableSongsCollectionState()
            .doOnNext { state ->
                ui.render(state)
            }.subscribe()


    fun bind(context: Context, ui: SongsCollectionUI) {
        if(bound) return
        bound = true

        serviceBinder.bind(context.applicationContext) { service ->
            disposable.add(subscribeState(service, ui))
        }
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