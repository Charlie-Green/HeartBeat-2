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
import by.vadim_churun.individual.heartbeat2.app.model.state.*
import by.vadim_churun.individual.heartbeat2.app.presenter.song.*
import by.vadim_churun.individual.heartbeat2.app.ui.common.ServiceSource
import by.vadim_churun.individual.heartbeat2.trans.FabDrawableAnimator
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.songs_collection_fragment.*


class SongsCollectionFragment: Fragment(), SongsCollectionUI, PlaylistContentModifUI {
    ////////////////////////////////////////////////////////////////////////////////////////
    // SAVED STATE:

    private val KEY_RETAINED_POSITION = "retainPos"
    private val KEY_IS_EDITING = "edit"
    private var retainedPosition: Int? = null
    private var isEditing = false
    private var playlistID = 0

    private fun restoreState(savedState: Bundle?) {
        savedState ?: return
        retainedPosition = savedState.getInt(KEY_RETAINED_POSITION)
        isEditing = savedState.getBoolean(KEY_IS_EDITING)
    }

    private fun saveState(outState: Bundle) {
        val layoutMan = recvSongs.layoutManager as LinearLayoutManager?
        layoutMan?.findLastVisibleItemPosition()?.also {
            outState.putInt(KEY_RETAINED_POSITION, it)
        }
        outState.putBoolean(KEY_IS_EDITING, isEditing)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private fun displaySongs() {
        val layoutMan = recvSongs.layoutManager as LinearLayoutManager?
        val lastPosition = retainedPosition
            ?: layoutMan?.findFirstVisibleItemPosition()
            ?: 0

        recvSongs.layoutManager = layoutMan
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = SongsCollectionAdapter(super.requireContext(), isEditing)
        recvSongs.swapAdapter(newAdapter, true)

        // Preserve the previously scrolled position:
        recvSongs.post {
            retainedPosition = null
            recvSongs.scrollToPosition(lastPosition)
        }
    }

    private fun showSyncErrorDialog(sourceName: String) {
        val context = super.requireContext()
        AlertDialog.Builder(context)
            .setTitle(R.string.error)
            .setMessage( context.getString(R.string.sync_error_f, sourceName) )
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun locateFabs() {
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

        val paramsEdit = fabEdit.layoutParams as CoordinatorLayout.LayoutParams
        paramsEdit.marginEnd = params.marginEnd
        fabCurrent.measure(0, 0)
        paramsEdit.bottomMargin = params.bottomMargin + small + fabCurrent.measuredHeight
        fabEdit.layoutParams = paramsEdit
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

    private fun updateFabEdit() {
        fabEdit.isVisible =
            (SongsCollectionEditor.playlistSongs != SongsCollectionEditor.allSongs)
        fabEdit.setImageResource(if(isEditing) R.drawable.ic_apply else R.drawable.ic_edit)
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // CONTENT EDITING:

    private lateinit var onDismissedSubscribtion: Disposable

    private fun swapEditMode() {
        isEditing = !isEditing
        if(isEditing)
            SongsCollectionEditor.userChecks.clear()

        displaySongs()
        FabDrawableAnimator(fabEdit)
            .start(if(isEditing) R.drawable.ic_apply else R.drawable.ic_edit)
    }

    private fun subscribeOnUpdateDialogDismiss()
        = UpdatePlistContentDialog
            .observableDismissed()
            .doOnNext { dismissReason ->
                if(dismissReason != UpdatePlistContentDialog.DismissReason.OUTSIDE_TAPPED)
                    swapEditMode()
            }.subscribe()

    private fun confirmUpdatePlaylistContent() {
        val updateInfo = UpdatePlistContentDialog.UpdateInfo(
            playlistID, "[TODO: FETCH NAME]", SongsCollectionEditor.userChecks )
        UpdatePlistContentDialog.show(updateInfo)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.songs_collection_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        restoreState(savedInstanceState)
        locateFabs()
        fabCurrent.setOnClickListener { navigateCurrentSong() }
        fabEdit.setOnClickListener    {
            if(isEditing)
                confirmUpdatePlaylistContent()
            else
                swapEditMode()
        }
    }

    override fun onStart() {
        super.onStart()
        val serviceSource = super.requireActivity() as ServiceSource
        disposable.addAll(subscribeService(serviceSource))
        UpdatePlistContentDialog.fragment = this
        onDismissedSubscribtion = subscribeOnUpdateDialogDismiss()
    }

    override fun onStop() {
        disposable.clear(); onDismissedSubscribtion.dispose()
        UpdatePlistContentDialog.clear()
        presenter.unbind(); contentPresenter.unbind()
        super.onStop()
        UpdatePlistContentDialog.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    override fun onRequestPermissionsResult
    (requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        syncPermsRequester.handleResult(requestCode, grantResults)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI PRESENTER:

    private val presenter = SongsCollectionPresenter()
    private val contentPresenter = PlaylistContentModifPresenter()
    private val disposable = CompositeDisposable()

    private fun subscribeService(source: ServiceSource)
        = source.observableService()
            .doOnNext { service ->
                presenter.bind(service, this)
                contentPresenter.bind(service, this)
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

    override fun intentUpdateContent(): Observable<PlaylistContentModifAction.UpdateContent>
        = SongsCollectionSubjects.UPDATE_PLAYLIST_CONTENT


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI RENDER:

    private val syncPermsRequester by lazy {
        SyncPermissionsRequester(this)
    }
    private var isPreparingCollection = false
    private var isSyncing = false
    private var isUpdatingContent = false

    private fun updatePrBarVisibility() {
        prBar.isVisible = isPreparingCollection || isSyncing || isUpdatingContent
    }


    override fun render(state: SongsCollectionState) {
        when(state) {
            is SongsCollectionState.Preparing -> {
                isPreparingCollection = true; updatePrBarVisibility()
            }

            is SongsCollectionState.CollectionPrepared -> {
                // If all songs are being displayed, editing is not allowed.
                isEditing = isEditing && (state.allSongs != null)

                playlistID = state.playlistID
                SongsCollectionEditor.playlistSongs = state.songs
                SongsCollectionEditor.allSongs = state.allSongs ?: state.songs
                updateFabEdit(); displaySongs()
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
                    showSyncErrorDialog(state.sourceName)
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

    override fun render(state: PlaylistContentModifState) {
        when(state) {
            is PlaylistContentModifState.Processing -> {
                isUpdatingContent = true
            }

            is PlaylistContentModifState.Updated -> {
                isUpdatingContent = false
                android.util.Log.v("HbContent", "Updated")
                // TODO: Show a brief message.
            }
        }
        updatePrBarVisibility()
    }
}