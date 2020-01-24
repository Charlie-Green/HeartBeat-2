package by.vadim_churun.individual.heartbeat2.app.model.logic

import by.vadim_churun.individual.heartbeat2.app.model.logic.internal.*
import by.vadim_churun.individual.heartbeat2.app.model.obj.SongStub
import by.vadim_churun.individual.heartbeat2.app.model.state.PlaybackState
import by.vadim_churun.individual.heartbeat2.app.player.HeartBeatPlayer
import by.vadim_churun.individual.heartbeat2.shared.*
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/** One of the highest level classes of MVI's "model" layer,
  * the one which manages playback **/
class PlayerRepository @Inject constructor(
    private val player: HeartBeatPlayer,
    private val collectMan: SongsCollectionManager,
    private val stubMan: SongStubsManager,
    private val mapper: Mapper
) {
    ///////////////////////////////////////////////////////////////////////////////////////////
    // PLAYBACK MANAGEMENT:

    fun play(song: SongWithSettings) {
        lastSong = song
        lastStub = stubMan.stubFrom(song)
        player.play(song)
        player.volume = song.volume
        player.rate = song.rate
        collectMan.notifyPlayingSong(song)
    }

    fun playOrPause() {
        player.pauseOrResume()
    }

    fun replay() {
        player.position = 0L
    }

    fun stop() {
        player.release()
    }

    fun seek(position: Long) {
        player.position = position
    }

    fun setPlaybackRate(rate: Float) {
        player.rate = rate
        onSongSettingsChanged()
    }

    fun setVolume(volume: Float) {
        player.volume = volume
        onSongSettingsChanged()
    }

    fun notifySongPriorityChanged(songID: Int, newPriority: Byte) {
        if(lastSong?.ID == songID)
            onSongSettingsChanged(newPriority)
    }

    val observableSongComplete
        get() = player.observableSongComplete
            .observeOn(AndroidSchedulers.mainThread())


    ///////////////////////////////////////////////////////////////////////////////////////////
    // STATE OBSERVABLE:

    private var lastSong: SongWithSettings? = null
    private var lastStub: SongStub? = null

    /** Updates [lastSong] to contain the latest settings. **/
    private fun onSongSettingsChanged(newPriority: Byte = lastSong?.priority ?: 0) {
        lastSong = lastSong?.let {
            mapper.withNewSettings(it, player.rate, player.volume, newPriority)
        }
    }

    private val stoppedState
        get() = PlaybackState.Stopped(lastSong, lastStub, collectMan.order)

    private val preparingState
        get() = PlaybackState.Preparing(lastStub!!)

    private val playingState
        get() = PlaybackState.Playing(
            lastSong!!, lastStub!!, player.position, collectMan.order )

    private val pausedState
        get() = PlaybackState.Paused(
            lastSong!!, lastStub!!, player.position, collectMan.order )

    private var rxState: Observable<PlaybackState>? = null
    fun observableState()
        = rxState ?: Observable.interval(192L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                if(player.isReleased)
                    this.stoppedState
                else if(player.isPreparing)
                    this.preparingState
                else if(player.isPlaying)
                    this.playingState
                else
                    this.pausedState
            }.also { rxState = it }

}