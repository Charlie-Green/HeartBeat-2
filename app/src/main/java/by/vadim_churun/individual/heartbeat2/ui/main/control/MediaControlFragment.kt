package by.vadim_churun.individual.heartbeat2.ui.main.control

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.model.obj.SongStub
import by.vadim_churun.individual.heartbeat2.model.state.PlaybackState
import by.vadim_churun.individual.heartbeat2.presenter.control.*
import by.vadim_churun.individual.heartbeat2.service.HeartBeatMediaService
import by.vadim_churun.individual.heartbeat2.shared.*
import by.vadim_churun.individual.heartbeat2.ui.common.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.userChanges
import io.reactivex.Observable
import io.reactivex.subjects.*
import kotlinx.android.synthetic.main.media_control_fragment.*
import kotlin.math.roundToInt


class MediaControlFragment:
DialogFragment(), MediaControlUI, SystemUiOverlapped, ServiceDependent {
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


    private fun TextView.updateTextIfNew(newText: String?) {
        if(newText == null)
            text = ""
        else if(text != newText)  // This check is crucial for marquee animation to work.
            text = newText
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // LAYOUT:

    /* SystemUiOverlapped */
    override fun onSystemUiVisibilityChanged(isVisible: Boolean) {
        val display = super.requireActivity().windowManager.defaultDisplay
        val navSize = if(isVisible) {
            // Navigation bar is visible. Need its size to know how much padding to add.
            UiUtils.navBarHeight(super.getResources(), display)
        } else {
            // Navigation bar is invisible. No additional padding is needed.
            0
        }

        val v = super.requireView()
        when(display.rotation) {
            Surface.ROTATION_0 ->
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navSize)
            Surface.ROTATION_90 ->
                v.setPadding(v.paddingLeft, v.paddingTop, navSize, v.paddingBottom)
            Surface.ROTATION_270 ->
                v.setPadding(navSize, v.paddingTop, v.paddingRight, v.paddingBottom)
        }

        if(display.rotation != Surface.ROTATION_0)
            return    // The navigation bar doesn't overlap any media controls in this orientation.
    }

    private fun setPeekHeight() {
        val res = super.getResources()
        val v = super.requireView().apply { measure(0, 0) }
        BottomSheetBehavior.from(v).apply {
            peekHeight = tvTitle.measuredHeight
                .plus(tvArtist.measuredHeight)
                .plus(sbPosition.measuredHeight)
                .plus(imgvPlayPause.measuredHeight)
                .plus(v.paddingBottom)
                .plus(res.getDimensionPixelSize(R.dimen.media_control_line_margin_small))
                .plus(res.getDimensionPixelSize(R.dimen.media_control_line_margin))
                .plus(res.getDimensionPixelSize(R.dimen.media_control_line_margin_big))
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun letTitleAndArtistMarquee() {
        tvTitle.isSelected = true
        tvArtist.isSelected = true
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // GESTURE CONTROL:

    private val normalizeRateSubject = PublishSubject.create<Unit>()

    private val rateNormalizingGestListener =
        object: GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean
                = true

            override fun onDoubleTap(event: MotionEvent): Boolean {
                normalizeRateSubject.onNext(Unit)
                return true    // Consumed.
            }
        }

    private val rateNormalizingGestDetector by lazy {
        GestureDetector(super.requireContext(), rateNormalizingGestListener)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.media_control_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        for(tv in listOf(tvRateLabel, tvRate)) {
            tv.setOnTouchListener { _, event ->
                // Double-tap on these TextViews will set rate to 1.0.
                rateNormalizingGestDetector.onTouchEvent(event)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setPeekHeight()
    }

    override fun onPause() {
        super.onPause()
        preventInitialSeekbarChanges()
    }

    override fun onResume() {
        super.onResume()
        letTitleAndArtistMarquee()
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI PRESENTER:

    private val presenter = MediaControlPresenter()

    /* ServiceDependent */
    override fun useBoundService(service: HeartBeatMediaService) {
        presenter.bind(service, this)
    }

    /* Service Dependent */
    override fun notifyServiceUnbound() {
        presenter.unbind()
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI INTENTS:

    private var wasInitialSeek = false
    private var wasInitialRateSet = false
    private var wasInitialVolumeSet = false

    private fun preventInitialSeekbarChanges() {
        wasInitialRateSet = false; wasInitialVolumeSet = false
        wasInitialSeek = false
    }

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
        = sbPosition.userChanges()
            .filter {
                wasInitialSeek.also { wasInitialSeek = true }
            }.map { event ->
                val position = sbPosition.progressToMediaPosition()
                MediaControlAction.Seek(position)
            }

    override fun setRateIntent(): Observable<MediaControlAction.SetRate>
        = sbRate.userChanges()
            .filter {
                wasInitialRateSet.also { wasInitialRateSet = true }
            }.map { event ->
                val rate = sbRate.progressToPlaybackRate()
                MediaControlAction.SetRate(rate)
            }.mergeWith( normalizeRateSubject
                .map { MediaControlAction.SetRate(1.0f) }
            )

    override fun setVolumeIntent(): Observable<MediaControlAction.SetVolume>
        = sbVolume.userChanges()
            .filter {
                wasInitialVolumeSet.also { wasInitialVolumeSet = true }
            }.map { event ->
                val volume = sbVolume.progressToVolume()
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
        tvTitle.updateTextIfNew(stub?.displayTitle)
        tvArtist.updateTextIfNew(stub?.displayArtist)
    }

    private fun renderSongSettings(song: SongWithSettings) {
        sbRate.setPlaybackRate(song.rate)
        sbVolume.setVolume(song.volume)
        rbPriority.rating = song.priority.toFloat()
        tvRate.text = "x%.2f".format(song.rate)
        tvVolume.text = "${song.volume.times(100f).toInt()}%"

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
            val drawable = imgv.drawable.apply { setTint(tintColor) }
            imgv.setImageDrawable(drawable)
        }

        val idLabel = when(order) {
            SongsOrder.SEQUENTIAL -> R.string.sequential_order
            SongsOrder.LOOP       -> R.string.loop_order
            SongsOrder.SHUFFLE    -> R.string.shuffle_order
            else                  -> 0
        }
        tvSongsOrder.setText(idLabel)
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


    override fun render(state: PlaybackState) {
        when(state) {
            is PlaybackState.Preparing -> {
                renderStub(state.stub)
                renderPositionInvalid()
                renderPlayPauseButton(false)
            }

            is PlaybackState.Playing -> {
                renderStub(state.stub)
                renderSongSettings(state.song)
                renderPlayPauseButton(true)
                renderSongsOrder(state.order)
                renderPosition(state.position, state.song.duration)
            }

            is PlaybackState.Paused -> {
                renderStub(state.stub)
                renderSongSettings(state.song)
                renderPlayPauseButton(false)
                renderSongsOrder(state.order)
                renderPosition(state.position, state.song.duration)
            }

            is PlaybackState.Stopped -> {
                renderStub(state.lastStub)
                state.lastSong?.also { renderSongSettings(it) }
                renderPositionInvalid()
                renderSongsOrder(state.order)
                renderPlayPauseButton(false)
            }

            is PlaybackState.PlayFailed -> {
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