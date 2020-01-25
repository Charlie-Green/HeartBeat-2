package by.vadim_churun.individual.heartbeat2.app.ui.main.song

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.model.obj.SongsList
import by.vadim_churun.individual.heartbeat2.app.model.state.*
import by.vadim_churun.individual.heartbeat2.app.presenter.song.*
import by.vadim_churun.individual.heartbeat2.app.ui.common.ServiceSource
import by.vadim_churun.individual.heartbeat2.trans.FabDrawableAnimator
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.songs_collection_fragment.*


class SongsCollectionFragment: Fragment(), SongsCollectionUI {
    ////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var retainedPosition: Int? = null
    private val KEY_RETAINED_POSITION = "retainPos"

    private fun displaySongs(songs: SongsList) {
        val layoutMan = recvSongs.layoutManager as LinearLayoutManager?
        val lastPosition = retainedPosition?.also {
            // This field is used only once.
            retainedPosition = null
        } ?:layoutMan?.findFirstVisibleItemPosition()
        recvSongs.layoutManager = layoutMan
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = SongsCollectionAdapter(super.requireContext(), songs)
        recvSongs.swapAdapter(newAdapter, true)

        // Preserve the previously scrolled position:
        lastPosition?.also { recvSongs.scrollToPosition(it) }
    }

    private fun showSyncErrorDialog(sourceName: String, cause: Throwable) {
        val context = super.requireContext()
        AlertDialog.Builder(context)
            .setTitle(R.string.error)
            .setMessage( context.getString(R.string.sync_error_f, sourceName) )
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun locateFindCurrentFab() {
        val small = super.getResources().getDimensionPixelSize(R.dimen.fab_margin_small)
        val medium = super.getResources().getDimensionPixelSize(R.dimen.fab_margin_medium)
        val big = super.getResources().getDimensionPixelSize(R.dimen.fab_margin_big)
        val display = super.requireActivity().windowManager.defaultDisplay
        val metrs = DisplayMetrics().also { display.getMetrics(it) }
        val isPortrait = (metrs.heightPixels > metrs.widthPixels)

        val params = fabCurrent.layoutParams as CoordinatorLayout.LayoutParams
        params.bottomMargin = if(isPortrait) medium else small
        params.marginEnd    = if(isPortrait) small else big
        fabCurrent.layoutParams = params
    }

    private fun navigateCurrentSong() {
        val adapter = recvSongs.adapter as SongsCollectionAdapter? ?: return
        val songPosition = adapter.highlightedPosition ?: return

        val layoutMan = recvSongs.layoutManager as LinearLayoutManager
        val firstPosition = layoutMan.findFirstVisibleItemPosition()
        val lastPosition = layoutMan.findLastVisibleItemPosition()
        val middlePosition = (firstPosition+lastPosition)/2
        if(songPosition < middlePosition) {
            // Need to scroll up. The song by default will appear at the top. Move it down.
            val delta = middlePosition - firstPosition
            recvSongs.smoothScrollToPosition(songPosition - delta)
        } else {
            // Move it up.
            val delta = lastPosition - middlePosition
            recvSongs.smoothScrollToPosition(songPosition + delta)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.songs_collection_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        savedInstanceState?.getInt(KEY_RETAINED_POSITION)?.also {
            retainedPosition = it
        }
        locateFindCurrentFab()
        fabCurrent.setOnClickListener { navigateCurrentSong() }
    }

    override fun onStart() {
        super.onStart()
        val serviceSource = super.requireActivity() as ServiceSource
        disposable.addAll(subscribeService(serviceSource))
    }

    override fun onStop() {
        disposable.clear()
        presenter.unbind()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val layoutMan = recvSongs.layoutManager as LinearLayoutManager?
        layoutMan?.findFirstVisibleItemPosition()?.also {
            outState.putInt(KEY_RETAINED_POSITION, it)
        }
    }

    override fun onRequestPermissionsResult
    (requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        syncPermsRequester.handleResult(requestCode, grantResults)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI PRESENTER:

    private val presenter = SongsCollectionPresenter()
    private val disposable = CompositeDisposable()

    private fun subscribeService(source: ServiceSource)
        = source.observableService()
            .doOnNext { service ->
                presenter.bind(service, this)
            }.subscribe()


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI INTENTS:

    override fun playIntent(): Observable<SongsCollectionAction.Play>
        = SongsCollectionSubjects.PLAY

    override fun decodeArtIntent(): Observable<SongsCollectionAction.DecodeArt>
        = SongsCollectionSubjects.DECODE_ART

    override fun setPriorityIntent(): Observable<SongsCollectionAction.SetPriority>
        = SongsCollectionSubjects.SET_PRIORITY

    override fun submitPermissionsResultIntent():
    Observable<SongsCollectionAction.SubmitPermissionsResult>
        = SongsCollectionSubjects.SUBMIT_PERMISSION_RESULT


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI RENDER:

    private val syncPermsRequester by lazy {
        SyncPermissionsRequester(this)
    }
    private var isPreparingCollection = false
    private var isSyncing = false

    private fun updatePrBarVisibility() {
        prBar.isVisible = isPreparingCollection || isSyncing
    }


    override fun render(state: SongsCollectionState) {
        when(state) {
            is SongsCollectionState.Preparing -> {
                isPreparingCollection = true; updatePrBarVisibility()
            }

            is SongsCollectionState.CollectionPrepared -> {
                displaySongs(state.songs)
                isPreparingCollection = false; updatePrBarVisibility()
            }

            is SongsCollectionState.ArtDecoded -> {
                val adapter = recvSongs.adapter as SongsCollectionAdapter?
                adapter?.applySongArt(state.songID, state.art)
            }
        }
    }

    override fun render(state: SyncState) {
        when(state) {
            is SyncState.Active -> {
                val adapter = recvSongs?.adapter as SongsCollectionAdapter?
                             // Otherwise, we just don't need to show the sync process.
                isSyncing = (adapter == null || adapter.itemCount == 0)
                updatePrBarVisibility()
            }

            is SyncState.NotSyncing -> {
                isSyncing = false; updatePrBarVisibility()
            }

            is SyncState.Error -> {
                if(state.consumed) return
                state.consumed = true

                val srcName = state.sourceName
                val thrName = state.cause.javaClass.simpleName
                val thrMessage = state.cause.message
                Log.w("HbSync", "$thrName from $srcName: $thrMessage")
                if(state.shouldDisturbUser)
                    showSyncErrorDialog(state.sourceName, state.cause)
            }

            is SyncState.MissingPermissions -> {
                syncPermsRequester.request(state)
            }
        }
    }

    override fun render(state: PlaybackState) {
        val adapter = recvSongs?.adapter as SongsCollectionAdapter? ?: return
        val playingSong = when(state) {
            is PlaybackState.Playing -> state.song
            is PlaybackState.Paused  -> state.song
            else -> null
        } ?: return
        adapter.highlightSong(playingSong.ID)
    }
}