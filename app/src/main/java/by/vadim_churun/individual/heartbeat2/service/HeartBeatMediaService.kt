package by.vadim_churun.individual.heartbeat2.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import by.vadim_churun.individual.heartbeat2.HeartBeatApplication
import by.vadim_churun.individual.heartbeat2.model.logic.SongsRepository
import by.vadim_churun.individual.heartbeat2.player.HeartBeatPlayer
import by.vadim_churun.individual.heartbeat2.shared.*
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class HeartBeatMediaService: Service() {
    /////////////////////////////////////////////////////////////////////////////////////////
    // DEPENDENCIES:

    @Inject lateinit var actions: MediaActions
    @Inject lateinit var notifFact: MediaNotificationFactory
    @Inject lateinit var player: HeartBeatPlayer
    @Inject lateinit var songsRepo: SongsRepository

    private fun inject() {
        val app = super.getApplication() as HeartBeatApplication
        app.diComponent.inject(this)
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // API:

    fun play(song: Song) {
        player.play(song)
    }

    fun playOrPause() {
        player.pauseOrResume()
    }

    fun replayCurrentSong() {
        player.position = 0L
    }

    fun stopPlayback() {
        player.release()
    }

    fun playPrevious() {
        // TODO
    }

    fun playNext() {
        // TODO
    }

    fun seek(position: Long) {
        player.position = position
    }

    fun setPlaybackRate(rate: Float) {
        player.rate = rate
    }

    fun setVolume(volume: Float) {
        player.volume = volume
    }

    fun setSongsOrder(order: SongsOrder) {
        // TODO
    }

    fun setCurrentSongPriority(priority: Byte) {
        // TODO
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeSongs()
        = songsRepo.observableSongs()
            .doOnNext { songs ->
                // TODO
            }.subscribe()


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
    // LIFECYCLE:

    override fun onCreate() {
        inject()
        super.startForeground(notifFact.notificationID, notifFact.createNotification())
        disposable.add(subscribeSongs())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
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
        disposable.clear()
        player.release()
    }
}