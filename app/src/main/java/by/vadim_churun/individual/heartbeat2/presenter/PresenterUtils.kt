package by.vadim_churun.individual.heartbeat2.presenter

import by.vadim_churun.individual.heartbeat2.service.HeartBeatMediaService
import by.vadim_churun.individual.heartbeat2.service.MediaServiceBinder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


object PresenterUtils {
    private val interactFailedSubject = PublishSubject.create<Unit>()

    fun interactMediaService
    (binder: MediaServiceBinder, action: (service: HeartBeatMediaService) -> Unit) {
        if(!binder.interact(action))
            interactFailedSubject.onNext(Unit)
    }

    fun mediaServiceInteractFailedObservable(): Observable<Unit>
        = interactFailedSubject
}