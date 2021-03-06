package by.vadim_churun.individual.heartbeat2.app.presenter.service

import android.content.Context
import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService


interface ServiceBoundUI {
    /** [Context] to be bound to [HeartBeatMediaService] **/
    val boundContext: Context

    /** Action to perform when the [HeartBeatMediaService] is bound. **/
    fun onConnected(service: HeartBeatMediaService)
}