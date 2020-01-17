package by.vadim_churun.individual.heartbeat2.app.ui.common

import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService


/** Interface for a UI class which depends on [HeartBeatMediaService]
  * but cannot bind the service by itself. **/
interface ServiceDependent {
    fun useBoundService(service: HeartBeatMediaService)
    fun notifyServiceUnbound()
}