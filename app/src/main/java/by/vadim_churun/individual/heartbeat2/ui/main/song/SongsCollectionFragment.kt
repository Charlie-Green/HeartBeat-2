package by.vadim_churun.individual.heartbeat2.ui.main.song

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.model.obj.SongsList
import by.vadim_churun.individual.heartbeat2.model.state.SongsCollectionState
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
        // TODO
    }
}