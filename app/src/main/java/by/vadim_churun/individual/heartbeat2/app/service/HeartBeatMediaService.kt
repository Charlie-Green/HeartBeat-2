package by.vadim_churun.individual.heartbeat2.app.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import by.vadim_churun.individual.heartbeat2.app.HeartBeatApplication
import by.vadim_churun.individual.heartbeat2.app.model.logic.*
import by.vadim_churun.individual.heartbeat2.app.model.logic.PlaylistsCollectionRepository
import by.vadim_churun.individual.heartbeat2.app.model.obj.PlaylistHeader
import by.vadim_churun.individual.heartbeat2.shared.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class HeartBeatMediaService: Service() {
    /////////////////////////////////////////////////////////////////////////////////////////
    // DEPENDENCIES:

    @Inject lateinit var actions: MediaActions
    @Inject lateinit var notifFact: MediaNotificationFactory
    @Inject lateinit var songsRepo: SongsCollectionRepository
    @Inject lateinit var playerRepo: PlayerRepository
    @Inject lateinit var plistsRepo: PlaylistsCollectionRepository

    private fun inject() {
        val app = super.getApplication() as HeartBeatApplication
        app.diComponent.inject(this)
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // API:

    fun play(song: SongWithSettings) {
        playerRepo.play(song)
    }

    fun playOrPause() {
        playerRepo.playOrPause()
    }

    fun replayCurrentSong() {
        playerRepo.replay()
    }

    fun stopPlayback() {
        playerRepo.stop()
    }

    fun playPrevious() {
        songsRepo.previousSong?.also { playerRepo.play(it) }
    }

    fun playNext() {
        songsRepo.nextSong?.also { playerRepo.play(it) }
    }

    fun seek(position: Long) {
        android.util.Log.v("HbService", "seek($position)")
        playerRepo.seek(position)
    }

    fun setPlaybackRate(rate: Float) {
        playerRepo.setPlaybackRate(rate)
    }

    fun setVolume(volume: Float) {
        playerRepo.setVolume(volume)
    }

    fun setSongsOrder(order: SongsOrder) {
        songsRepo.setSongsOrder(order)
    }

    fun setSongPriority(songID: Int, priority: Byte) {
        // TODO
    }

    fun setCurrentSongPriority(priority: Byte) {
        // TODO
    }

    fun requestArtDecode(song: Song) {
        songsRepo.requestArtDecode(song)
    }

    fun requestArtDecode(plistHeader: PlaylistHeader) {
        // TODO
    }

    fun notifySyncPermissionsGranted() {
        songsRepo.notifySyncPermissionsGranted()
    }


    fun observableSongsCollectionState()
        = songsRepo.observableState()

    fun observableSyncState()
        = songsRepo.observableSyncState()

    fun observablePlaybackState()
        = playerRepo.observableState()

    fun observablePlaylistsCollectionState()
        = plistsRepo.observableState()


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
    // RX:

    private val disposable = CompositeDisposable()

    private fun subscribeSongComplete()
        = playerRepo.observableSongComplete
            .doOnNext {
                android.util.Log.v("HbDebug", "Song complete")
                songsRepo.nextSong?.also { playerRepo.play(it) }
            }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreate() {
        inject()
        super.startForeground(notifFact.notificationID, notifFact.notification)
        notifFact.observeState(
            playerRepo.observableState()
                .observeOn(Schedulers.computation())
        )
        disposable.add(subscribeSongComplete())
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
        songsRepo.dispose()
        playerRepo.stop()
        notifFact.dispose()
    }
}