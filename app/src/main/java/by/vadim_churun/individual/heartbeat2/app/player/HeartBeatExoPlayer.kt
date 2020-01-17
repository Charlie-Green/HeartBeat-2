package by.vadim_churun.individual.heartbeat2.app.player

import android.content.Context
import android.net.Uri
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.shared.Song
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.math.sqrt


/** The implementation of [HeartBeatPlayer] based on [ExoPlayer]. **/
class HeartBeatExoPlayer(private val appContext: Context): HeartBeatPlayer {
    /////////////////////////////////////////////////////////////////////////////////////////
    // INTERNAL:

    private var player: ExoPlayer? = null
    private var lastRate = 1f
    private var lastVolume = 1f
    private val completeSubject = PublishSubject.create<Unit>()

    private val audioAttrs by lazy {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
    }

    private fun initPlayer(): ExoPlayer {
        val listener = object: Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if(playbackState == ExoPlayer.STATE_ENDED)
                    completeSubject.onNext(Unit)
            }
        }
        return SimpleExoPlayer
            .Builder(appContext)
            .build()
            .apply { setAudioAttributes(audioAttrs, true) }
            .apply { addListener(listener) }
            .also { player = it }
    }

    /** Scales the given user volume for the difference between different volume levels
      * to be distributed more equally.
      * The return value can be set to [ExoPlayer]. **/
    private fun volumeUserToApi(userVolume: Float)
        = userVolume * userVolume

    /** Performs transformation opposite to [volumeUserToApi].
      * The return value can be forwarded outside the class. **/
    private fun volumeApiToUser(apiVolume: Float)
        = sqrt(apiVolume.toDouble()).toFloat()


    /////////////////////////////////////////////////////////////////////////////////////////
    // API:

    override val isPreparing: Boolean
        get() {
            val p = player ?: return false
            return p.playWhenReady && !p.isPlaying
        }

    override val isPlaying: Boolean
        get() = player?.isPlaying ?: false

    override val isReleased: Boolean
        get() = (player == null)

    override var position: Long
        get() = player?.currentPosition ?: -1L
        set(value) {
            if(value < 0L)
                throw IllegalArgumentException("Cannot set a negative value to position")
            player?.seekTo(value)
        }

    override var rate: Float
        get() = player?.playbackParameters?.speed ?: lastRate
        set(value) {
            val mPlayer = player
            if(mPlayer == null) {
                lastRate = value
                return
            }

            val oldParams = mPlayer.playbackParameters
            if(oldParams.speed == value) return
            val newParams = PlaybackParameters(
                value, oldParams.pitch, oldParams.skipSilence )
            mPlayer.setPlaybackParameters(newParams)
        }

    override var volume: Float
        get() {
            val apiVolume = player?.audioComponent?.volume ?: lastVolume
            return volumeApiToUser(apiVolume)
        }
        set(value) {
            val apiVolume = volumeUserToApi(value)
            player?.audioComponent?.apply {
                volume = apiVolume
            } ?: apiVolume.also { lastVolume = it }
        }

    override fun play(song: Song) {
        val mPlayer = player ?: initPlayer()
        val uagent = Util.getUserAgent(appContext, appContext.getString(R.string.app_name))
        val sourceFact = DefaultDataSourceFactory(appContext, uagent)
        ProgressiveMediaSource.Factory(sourceFact)
            .createMediaSource( Uri.parse(song.contentUri) )
            .also { mPlayer.prepare(it) }
        mPlayer.playWhenReady = true

        this.rate = lastRate
        this.volume = lastVolume
    }

    override fun pauseOrResume() {
        val mPlayer = player ?: return
        mPlayer.playWhenReady = !mPlayer.isPlaying
    }

    override fun release() {
        player?.release()
        player = null
    }

    override val observableSongComplete: Observable<Unit>
        get() = completeSubject
}