package by.vadim_churun.individual.heartbeat2.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import by.vadim_churun.individual.heartbeat2.HeartBeatApplication
import by.vadim_churun.individual.heartbeat2.shared.*
import javax.inject.Inject


class HeartBeatMediaService: Service() {
    /////////////////////////////////////////////////////////////////////////////////////////
    // API:

    fun play(song: Song) {
        // TODO
    }

    fun playOrPause() {
        // TODO
    }

    fun replayCurrentSong() {
        // TODO
    }

    fun stopPlayback() {
        // TODO
    }

    fun playPrevious() {
        // TODO
    }

    fun playNext() {
        // TODO
    }

    fun seek(position: Long) {
        // TODO
    }

    fun setPlaybackRate(rate: Float) {
        // TODO
    }

    fun setVolume(volume: Float) {
        // TODO
    }

    fun setSongsOrder(order: SongsOrder) {
        // TODO
    }

    fun setCurrentSongPriority(priority: Byte) {
        // TODO
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // BINDING:

    inner class MediaBinder: Binder() {
        val service
            get() = this@HeartBeatMediaService
    }
    private val BINDER = MediaBinder()

    override fun onBind(int: Intent): IBinder?
        = BINDER


    /////////////////////////////////////////////////////////////////////////////////////////
    // DEPENDENCIES:

    @Inject lateinit var actions: MediaActions
    @Inject lateinit var notifFact: MediaNotificationFactory

    private fun inject() {
        val app = super.getApplication() as HeartBeatApplication
        app.diComponent.inject(this)
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreate() {
        inject()
        super.startForeground(notifFact.notificationID, notifFact.createNotification())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        android.util.Log.v("Service", "Start: ${intent.action}")
        intent.action?.also {
            actions.recognize(
                action = it,
                onPlayPause = { playOrPause() },
                onPrevious = { playPrevious() },
                onNext = { playNext() }
            )
        }
        return START_STICKY
    }

    override fun onDestroy() {
        // TODO: Release player.
    }
}