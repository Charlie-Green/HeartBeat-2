package by.vadim_churun.individual.heartbeat2.ui.main.control

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.media.SongsOrder
import by.vadim_churun.individual.heartbeat2.presenter.control.*
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.changeEvents
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.media_control_fragment.*
import kotlin.math.roundToInt


class MediaControlFragment: DialogFragment(), MediaControlUI {
    //////////////////////////////////////////////////////////////////////////////////////
    // HELP:

    private fun SeekBar.progressToMediaPosition()
        = progress.toLong()

    private fun SeekBar.setMediaPosition(position: Long) {
        progress = position.toInt()
    }

    private fun SeekBar.setMediaDuration(duration: Long) {
        max = duration.toInt()
    }


    private fun SeekBar.progressToPlaybackRate(): Float {
        val ratio = progress.toFloat().div(max)
        return 0.5f + ratio*1.5f
    }

    private fun SeekBar.setPlaybackRate(rate: Float) {
        val ratio = (rate - 0.5f) / 1.5f
        progress = ratio.times(max).toInt()
    }


    private fun SeekBar.progressToVolume(): Float
        = progress.toFloat().div(max)

    private fun SeekBar.setVolume(volume: Float) {
        progress = volume.times(max).toInt()
    }


    //////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.media_control_fragment, container, false)


    //////////////////////////////////////////////////////////////////////////////////////
    // MVI INTENTS:

    override fun playPauseIntent(): Observable<MediaControlAction.PlayPause>
        = imgvPlayPause.clicks().map {
            MediaControlAction.PlayPause
        }

    override fun replayIntent(): Observable<MediaControlAction.Replay>
        = imgvReplay.clicks().map {
            MediaControlAction.Replay
        }

    override fun stopIntent(): Observable<MediaControlAction.Stop>
        = imgvStop.clicks().map {
            MediaControlAction.Stop
        }

    override fun seekIntent(): Observable<MediaControlAction.Seek>
        = sbPosition.changeEvents().map { event ->
            val position = event.view.progressToMediaPosition()
            MediaControlAction.Seek(position)
        }

    override fun setRateIntent(): Observable<MediaControlAction.SetRate>
        = sbRate.changeEvents().map { event ->
            val rate = event.view.progressToPlaybackRate()
            MediaControlAction.SetRate(rate)
        }

    override fun setVolumeIntent(): Observable<MediaControlAction.SetVolume>
        = sbVolume.changeEvents().map { event ->
            val volume = event.view.progressToVolume()
            MediaControlAction.SetVolume(volume)
        }

    override fun setPriorityIntent(): Observable<MediaControlAction.SetPriority> {
        val subj = BehaviorSubject.create<MediaControlAction.SetPriority>()
        rbPriority.setOnRatingBarChangeListener { rbar, rating, fromUser ->
            val priority = rating.roundToInt().toByte()
            subj.onNext(MediaControlAction.SetPriority(priority))
        }
        return subj
    }

    override fun setSongsOrderIntent(): Observable<MediaControlAction.SetSongsOrder>
        = imgvSequential.clicks().map { SongsOrder.SEQUENTIAL }
            .mergeWith(imgvLoop.clicks().map { SongsOrder.LOOP })
            .mergeWith(imgvShuffle.clicks().map { SongsOrder.SHUFFLE })
            .map { order -> MediaControlAction.SetSongsOrder(order) }

    override fun requestPreviousIntent(): Observable<MediaControlAction.RequestPrevious>
        = imgvPrevious.clicks().map {
            MediaControlAction.RequestPrevious
        }

    override fun requestNextIntent(): Observable<MediaControlAction.RequestNext>
        = imgvNext.clicks().map {
            MediaControlAction.RequestNext
        }
}