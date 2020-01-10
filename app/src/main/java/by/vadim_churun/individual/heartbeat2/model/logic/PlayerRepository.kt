package by.vadim_churun.individual.heartbeat2.model.logic

import by.vadim_churun.individual.heartbeat2.model.logic.internal.SongStubsManager
import by.vadim_churun.individual.heartbeat2.model.obj.SongStub
import by.vadim_churun.individual.heartbeat2.model.state.PlaybackState
import by.vadim_churun.individual.heartbeat2.player.HeartBeatPlayer
import by.vadim_churun.individual.heartbeat2.shared.*
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/** One of the highest level classes of MVI's "model" layer,
 * the one which manages playback **/
class PlayerRepository @Inject constructor(
    private val player: HeartBeatPlayer,
    private val stubMan: SongStubsManager
) {
    ///////////////////////////////////////////////////////////////////////////////////////////
    // PLAYBACK MANAGEMENT:

    fun play(song: SongWithSettings) {
        player.play(song)
        player.volume = song.volume
        player.rate = song.rate
        lastSong = song
        lastStub = stubMan.stubFrom(song)
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


    ///////////////////////////////////////////////////////////////////////////////////////////
    // STATE OBSERVABLE:

    private var lastSong: SongWithSettings? = null
    private var lastStub: SongStub? = null

    /** Updates [lastSong] to contain the latest SongSettings. **/
    private fun onSongSettingsChanged(newPriority: Byte = lastSong?.priority ?: 0) {
        lastSong = lastSong?.let {
            SongWithSettings(
                it.ID,
                it.title,
                it.artist,
                it.duration,
                it.filename,
                it.contentUri,
                player.rate,
                player.volume,
                newPriority
            )
        }
    }

    private val stoppedState
        // TODO: Retrieve current SongsOrder from SongsCollectionManager
        get() = PlaybackState.Stopped(lastSong, lastStub, SongsOrder.SEQUENTIAL)

    private val playingState    // TODO: The same about SongsOrder
        get() = PlaybackState.Playing(
            lastSong!!, lastStub!!, player.position, SongsOrder.SEQUENTIAL )

    private val pausedState    // TODO: The same about SongsOrder
        get() = PlaybackState.Paused(
            lastSong!!, lastStub!!, player.position, SongsOrder.SEQUENTIAL )

    fun stateObservable()
        = Observable.interval(192L, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                if(player.isReleased)
                    this.stoppedState
                else if(player.isPlaying)
                    this.playingState
                else
                    this.pausedState
            }

}