package by.vadim_churun.individual.heartbeat2.ui.main.song

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.model.obj.SongsList
import by.vadim_churun.individual.heartbeat2.model.state.SongsCollectionState
import by.vadim_churun.individual.heartbeat2.model.state.SyncState
import by.vadim_churun.individual.heartbeat2.presenter.song.*
import io.reactivex.Observable
import kotlinx.android.synthetic.main.songs_collection_fragment.*


class SongsCollectionFragment: DialogFragment(), SongsCollectionUI {
    ////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private fun displaySongs(songs: SongsList) {
        recvSongs.layoutManager = recvSongs.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val newAdapter = SongsCollectionAdapter(super.requireContext(), songs)
        recvSongs.swapAdapter(newAdapter, true)
    }

    private fun showSyncErrorDialog(sourceName: String, cause: Throwable) {
        val context = super.requireContext()
        AlertDialog.Builder(context)
            .setTitle(R.string.error)
            .setMessage( context.getString(R.string.sync_error_f, sourceName) )
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.songs_collection_fragment, container, false)

    override fun onStart() {
        super.onStart()
        SongsCollectionPresenter.bind(super.requireContext(), this)
    }

    override fun onStop() {
        SongsCollectionPresenter.unbind(super.requireContext())
        super.onStop()
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI INTENTS:

    override fun playIntent(): Observable<SongsCollectionAction.Play>
        = SongsCollectionSubjects.PLAY

    override fun decodeArtIntent(): Observable<SongsCollectionAction.DecodeArt>
        = SongsCollectionSubjects.DECODE_ART

    override fun setPriorityIntent(): Observable<SongsCollectionAction.SetPriority>
        = SongsCollectionSubjects.SET_PRIORITY


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI RENDER:

    override fun render(state: SongsCollectionState) {
        when(state) {
            is SongsCollectionState.CollectionPrepared -> {
                displaySongs(state.songs)
            }

            is SongsCollectionState.ArtDecoded -> {
                val adapter = recvSongs.adapter as SongsCollectionAdapter?
                adapter?.applySongArt(state.songID, state.art)
            }

            is SongsCollectionState.ArtDecodeFailed -> {
                Log.w("HbArts", "Failed to decode art for song ID ${state.songID}")
            }
        }
    }

    override fun render(state: SyncState) {
        when(state) {
            is SyncState.Active -> {
                val adapter = recvSongs.adapter as SongsCollectionAdapter?
                prbSync.isVisible = (adapter == null || adapter.itemCount == 0)
            }

            is SyncState.NotSyncing -> {
                prbSync.isVisible = false
            }

            is SyncState.Error -> {
                state.consumed = true

                val srcName = state.sourceName
                val thrName = state.cause.javaClass.simpleName
                val thrMessage = state.cause.message
                Log.w("HbSync", "$thrName from $srcName: $thrMessage")
                if(state.shouldDisturbUser)
                    showSyncErrorDialog(state.sourceName, state.cause)
            }
        }
    }
}