package by.vadim_churun.individual.heartbeat2.ui.main.control

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.entity.*
import by.vadim_churun.individual.heartbeat2.model.obj.SongStub
import by.vadim_churun.individual.heartbeat2.model.state.MediaState
import by.vadim_churun.individual.heartbeat2.presenter.control.*
import by.vadim_churun.individual.heartbeat2.ui.UiUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.changeEvents
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.media_control_fragment.*
import kotlin.math.roundToInt


class MediaControlFragment: DialogFragment(), MediaControlUI {
    ////////////////////////////////////////////////////////////////////////////////////////
    // EXTENSION:

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


    ////////////////////////////////////////////////////////////////////////////////////////
    // LAYOUT:

    private fun preventNavBarOverlap() {
        val display = super.requireActivity().windowManager.defaultDisplay
        if(display.rotation != Surface.ROTATION_0)
            return    // The navigation bar doesn't overlap any media controls in this orientation.

        val res = super.getResources()
        val idNavHeight = res.getIdentifier("navigation_bar_height", "dimen", "android")
        val navHeight = if(idNavHeight == 0) {
            val density = DisplayMetrics().also { display.getMetrics(it) }.density
            48f.times(density).toInt()   // Typical navigation bar height is 48dp.
        } else {
            res.getDimensionPixelSize(idNavHeight)
        }

        val v = super.requireView()
        v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navHeight)
    }

    private fun setPeekHeight() {
        val v = super.requireView().apply { measure(0, 0) }
        BottomSheetBehavior.from(v).apply {
            peekHeight = tvTitle.measuredHeight
                .plus(tvArtist.measuredHeight)
                .plus(sbPosition.measuredHeight)
                .plus(imgvPlayPause.measuredHeight)
                .plus(v.paddingBottom)
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.media_control_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        preventNavBarOverlap()
        setPeekHeight()
    }

    override fun onStart() {
        super.onStart()
        MediaControlPresenter.bind(super.requireContext(), this)
    }

    override fun onStop() {
        MediaControlPresenter.unbind(super.requireContext())
        super.onStop()
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI INTENTS:

    override fun playPauseIntent(): Observable<MediaControlAction.PlayPause>
        = imgvPlayPause.clicks()
            .map { MediaControlAction.PlayPause }

    override fun replayIntent(): Observable<MediaControlAction.Replay>
        = imgvReplay.clicks()
            .map { MediaControlAction.Replay }

    override fun stopIntent(): Observable<MediaControlAction.Stop>
        = imgvStop.clicks()
            .map { MediaControlAction.Stop }

    override fun seekIntent(): Observable<MediaControlAction.Seek>
        = sbPosition.changeEvents()
            .map { event ->
                val position = event.view.progressToMediaPosition()
                MediaControlAction.Seek(position)
            }

    override fun setRateIntent(): Observable<MediaControlAction.SetRate>
        = sbRate.changeEvents()
            .map { event ->
                val rate = event.view.progressToPlaybackRate()
                MediaControlAction.SetRate(rate)
            }

    override fun setVolumeIntent(): Observable<MediaControlAction.SetVolume>
        = sbVolume.changeEvents()
            .map { event ->
                val volume = event.view.progressToVolume()
                MediaControlAction.SetVolume(volume)
            }

    override fun setPriorityIntent(): Observable<MediaControlAction.SetPriority> {
        val subj = BehaviorSubject.create<MediaControlAction.SetPriority>()
        rbPriority.setOnRatingBarChangeListener { _, rating, _ ->
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
        = imgvPrevious.clicks()
            .map { MediaControlAction.RequestPrevious }

    override fun requestNextIntent(): Observable<MediaControlAction.RequestNext>
        = imgvNext.clicks()
            .map { MediaControlAction.RequestNext }


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI RENDER:

    private fun getThemeColor(attrID: Int): Int {
        val typval = TypedValue()
        super.requireActivity().theme.resolveAttribute(attrID, typval, true)
        return typval.data
    }

    private val colorSelected by lazy {
        getThemeColor(android.R.attr.colorAccent)
    }

    private val colorUnselected by lazy {
        getThemeColor(android.R.attr.colorBackground)
    }


    private fun renderStub(stub: SongStub?) {
        tvTitle.text = stub?.displayTitle ?: ""
        tvArtist.text = stub?.displayArtist ?: ""
    }

    private fun renderSongSettings(song: SongWithSettings) {
        sbRate.setPlaybackRate(song.rate)
        sbVolume.setVolume(song.volume)
        rbPriority.rating = song.priority.toFloat()
    }

    private fun renderPosition(position: Long, duration: Long) {
        sbPosition.isEnabled = true
        sbPosition.setMediaDuration(duration)
        sbPosition.setMediaPosition(position)
        tvPosition.text = UiUtils.timeString(position)
        tvDuration.text = UiUtils.timeString(duration)
    }

    private fun renderPositionInvalid() {
        sbPosition.progress = 0
        sbPosition.isEnabled = false
        val s0 = UiUtils.timeString(0L)
        tvPosition.text = s0; tvDuration.text = s0
    }

    private fun renderPlayPauseButton(asPlaying: Boolean) {
        val iconID = if(asPlaying) R.drawable.ic_pause else R.drawable.ic_play
        imgvPlayPause.setImageResource(iconID)
    }

    private fun renderSongsOrder(order: SongsOrder) {
        val idSelectedIcon = when(order) {
            SongsOrder.SEQUENTIAL -> R.id.imgvSequential
            SongsOrder.LOOP       -> R.id.imgvLoop
            SongsOrder.SHUFFLE    -> R.id.imgvShuffle
            else                  -> 0
        }
        for(imgv in listOf(imgvSequential, imgvLoop, imgvShuffle)) {
            val tintColor = if(imgv.id == idSelectedIcon) colorSelected else colorUnselected
            imgv.drawable.setTint(tintColor)
        }
    }

    private fun renderErrorDialog(requestedStub: SongStub) {
        val appContext = super.requireContext().applicationContext
        AlertDialog.Builder(super.requireContext())
            .setTitle(R.string.error)
            .setMessage(
                appContext.getString(R.string.play_failed_message, requestedStub.displayTitle)
            ).setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }


    override fun render(state: MediaState) {
        when(state) {
            is MediaState.Preparing -> {
                renderStub(state.stub)
                renderPositionInvalid()
                renderPlayPauseButton(false)
            }

            is MediaState.Playing -> {
                renderStub(state.stub)
                renderSongSettings(state.song)
                renderPlayPauseButton(true)
                renderSongsOrder(state.order)
                renderPosition(state.position, state.song.duration)
            }

            is MediaState.Paused -> {
                renderStub(state.stub)
                renderSongSettings(state.song)
                renderPlayPauseButton(false)
                renderSongsOrder(state.order)
                renderPosition(state.position, state.song.duration)
            }

            is MediaState.Stopped -> {
                renderStub(state.lastStub)
                state.lastSong?.also { renderSongSettings(it) }
                renderPositionInvalid()
                renderSongsOrder(state.order)
                renderPlayPauseButton(false)
            }

            is MediaState.PlayFailed -> {
                renderStub(state.lastStub)
                state.lastSong?.also { renderSongSettings(it) }
                renderPositionInvalid()
                renderSongsOrder(state.order)
                renderErrorDialog(state.requestedSongStub)
                state.consumed = true
            }
        }
    }
}