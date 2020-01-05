package by.vadim_churun.individual.heartbeat2.service

import android.content.Context


internal object MediaActions {
    fun playpause(appContext: Context)
        = "${appContext.packageName}.action.PPAUSE"

    fun previous(appContext: Context)
        = "${appContext.packageName}.action.PREV"

    fun next(appContext: Context)
        = "${appContext.packageName}.action.NEXT"
}