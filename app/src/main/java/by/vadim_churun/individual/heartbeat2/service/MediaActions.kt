package by.vadim_churun.individual.heartbeat2.service

import android.content.Context
import javax.inject.Inject


class MediaActions @Inject constructor(private val appContext: Context) {
    private val PREFIX = "${appContext.packageName}.action."
    private val POSTFIX_PLAY_PAUSE = "PPAUSE"
    private val POSTFIX_PREVIOUS   = "PREV"
    private val POSTFIX_NEXT       = "NEXT"


    fun playpause()
        = "${PREFIX}${POSTFIX_PLAY_PAUSE}"

    fun previous()
        = "${PREFIX}${POSTFIX_PREVIOUS}"

    fun next()
        = "${PREFIX}${POSTFIX_NEXT}"


    /** Attempts to recognize the given action.
      * If recognized, executes the appropriate callback and returns true.
      * If not, returns false. **/
    fun recognize(
        action: String,
        onPlayPause: () -> Unit,
        onPrevious: () -> Unit,
        onNext: () -> Unit
    ): Boolean {
        if(!action.startsWith(PREFIX, false))
            return false

        val postfix = action.substring(PREFIX.length)
        when(postfix) {
            POSTFIX_PLAY_PAUSE -> onPlayPause()
            POSTFIX_PREVIOUS   -> onPrevious()
            POSTFIX_NEXT       -> onNext()
            else               -> return false
        }
        return true
    }
}