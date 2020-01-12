package by.vadim_churun.individual.heartbeat2.ui.common

import by.vadim_churun.individual.heartbeat2.service.HeartBeatMediaService


/** Interface for a UI class which depends on [HeartBeatMediaService]
  * but cannot bind the service by itself. **/
interface ServiceDependent {
    fun useBoundService(service: HeartBeatMediaService)
    fun notifyServiceUnbound()
}