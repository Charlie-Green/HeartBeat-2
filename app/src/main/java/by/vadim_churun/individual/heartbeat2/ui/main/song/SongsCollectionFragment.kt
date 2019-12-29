package by.vadim_churun.individual.heartbeat2.ui.main.song

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.model.state.SongsCollectionState
import by.vadim_churun.individual.heartbeat2.presenter.song.SongsCollectionAction
import by.vadim_churun.individual.heartbeat2.presenter.song.SongsCollectionPresenter
import by.vadim_churun.individual.heartbeat2.presenter.song.SongsCollectionUI
import io.reactivex.Observable


class SongsCollectionFragment: DialogFragment(), SongsCollectionUI {
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
        = TODO()

    override fun decodeArtIntent(): Observable<SongsCollectionAction.DecodeArt>
        = TODO()

    override fun setPriorityIntent(): Observable<SongsCollectionAction.SetPriority>
        = TODO()


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI RENDER:

    override fun render(state: SongsCollectionState) {

    }
}