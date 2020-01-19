package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.model.obj.*
import by.vadim_churun.individual.heartbeat2.app.model.state.PlaylistsCollectionState
import by.vadim_churun.individual.heartbeat2.app.presenter.plist.*
import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import by.vadim_churun.individual.heartbeat2.app.ui.common.ServiceDependent
import by.vadim_churun.individual.heartbeat2.app.ui.common.VerticalCubePageTransformer
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.plists_collection_fragment.*


class PlaylistsCollectionFragment: Fragment(), PlaylistsCollectionUI, ServiceDependent {
    /////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var plistsTransformerSet = false

    private val currentAdapter
        = pagerPlists?.adapter as PlaylistsCollectionAdapter?

    private fun displayPlaylists(playlists: PlaylistsCollection) {
        pagerPlists.adapter = PlaylistsCollectionAdapter(playlists, actionSubject)
        if(!plistsTransformerSet) {
            plistsTransformerSet = true
            pagerPlists.setPageTransformer(VerticalCubePageTransformer())
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // PRESENTER:

    private val presenter = PlaylistsCollectionPresenter()

    /* ServiceDependent */
    override fun useBoundService(service: HeartBeatMediaService) {
        presenter.bind(service, this)
    }

    /* ServiceDependent */
    override fun notifyServiceUnbound() {
        presenter.unbind()
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // MVI:

    private val actionSubject = PublishSubject.create<PlaylistsCollectionAction>()

    override fun decodeArtIntent(): Observable<PlaylistsCollectionAction.DecodeArt>
        = actionSubject
            .filter { it is PlaylistsCollectionAction.DecodeArt }
            .map    { it as PlaylistsCollectionAction.DecodeArt }


    override fun render(state: PlaylistsCollectionState) {
        when(state) {
            PlaylistsCollectionState.Preparing -> {
                TODO()
            }

            is PlaylistsCollectionState.Prepared -> {
                displayPlaylists(state.collection)
            }

            is PlaylistsCollectionState.ArtDecoded -> {
                this.currentAdapter?.setArt(state.plistID, state.art)
            }

            is PlaylistsCollectionState.ArtDecodeFailed -> {
                TODO()
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
        = inflater.inflate(R.layout.plists_collection_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // TODO
    }
}