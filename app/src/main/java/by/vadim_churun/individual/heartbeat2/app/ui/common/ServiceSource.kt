package by.vadim_churun.individual.heartbeat2.app.ui.common

import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import io.reactivex.Observable


/** Interface for a UI class which can provide [HeartBeatMediaService]
  * to components that need it. **/
interface ServiceSource {
    fun observableService(): Observable<HeartBeatMediaService>
}