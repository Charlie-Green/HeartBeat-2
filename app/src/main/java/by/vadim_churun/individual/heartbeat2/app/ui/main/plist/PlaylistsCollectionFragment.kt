package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.model.obj.*
import by.vadim_churun.individual.heartbeat2.app.model.state.PlaylistsCollectionState
import by.vadim_churun.individual.heartbeat2.app.presenter.plist.*
import by.vadim_churun.individual.heartbeat2.app.ui.common.*
import com.jakewharton.rxbinding3.viewpager2.pageSelections
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.plists_collection_fragment.*


class PlaylistsCollectionFragment: Fragment(), PlaylistsCollectionUI {
    /////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var plistsTransformerSet = false

    private val currentAdapter
        get() = pagerPlists.adapter as PlaylistsCollectionAdapter?

    private val currentHeader
        get() = this.currentAdapter?.headerAt(pagerPlists.currentItem)

    private fun displayPlaylists(playlists: PlaylistsCollection) {
        pagerPlists.adapter = PlaylistsCollectionAdapter(playlists, actionSubject)
        if(!plistsTransformerSet) {
            plistsTransformerSet = true
            pagerPlists.setPageTransformer(VerticalCubePageTransformer())
        }
    }

    private fun showEditDialog(playlistID: Int) {
        val dialog = PlaylistEditDialog()
        if(playlistID != 0) {
            dialog.arguments = Bundle().apply {
                putInt(PlaylistEditDialog.KEY_PLAYLIST_ID, playlistID)
            }
        }
        dialog.show(super.requireFragmentManager(), null)
    }

    private fun View.hideIf(condition: Boolean) {
        isVisible = !condition

        // For some views, this property gets automatically
        // set to false when the view is hidden.
        isEnabled = true
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // MVI PRESENTER:

    private val presenter = PlaylistsCollectionPresenter()
    private val disposable = CompositeDisposable()

    private fun subscribeService(source: ServiceSource)
            = source.observableService()
        .doOnNext { service ->
            presenter.bind(service, this)
        }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////
    // MVI:

    private val actionSubject = PublishSubject.create<PlaylistsCollectionAction>()

    override fun openPlaylistIntent(): Observable<PlaylistsCollectionAction.OpenPlaylist>
        = pagerPlists.pageSelections()
            .doOnNext { position ->
                fabEdit.hideIf(position == 0)
                fabDelete.hideIf(position == 0)
            }.map { position ->
                val plist = this.currentHeader
                android.util.Log.v("HbPlist", "Want to open playlist ${plist?.ID}")
                PlaylistsCollectionAction.OpenPlaylist( OptionalID.wrap(plist?.ID) )
            }

    override fun decodeArtIntent(): Observable<PlaylistsCollectionAction.DecodeArt>
        = actionSubject
            .filter { it is PlaylistsCollectionAction.DecodeArt }
            .map    { it as PlaylistsCollectionAction.DecodeArt }


    override fun render(state: PlaylistsCollectionState) {
        when(state) {
            PlaylistsCollectionState.Preparing -> {
                prBar.visibility = View.VISIBLE
                displayPlaylists( PlaylistsCollection.from(listOf()) )
            }

            is PlaylistsCollectionState.Prepared -> {
                displayPlaylists(state.collection)
                prBar.visibility = View.GONE
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
        fabAdd.setOnClickListener {
            showEditDialog(0)
        }
        fabEdit.setOnClickListener {
            this.currentHeader?.ID?.also { showEditDialog(it) }
        }
        fabDelete.setOnClickListener {
            /* TODO */
        }
    }

    override fun onStart() {
        super.onStart()
        val serviceSource = super.requireActivity() as ServiceSource
        disposable.add(subscribeService(serviceSource))
    }

    override fun onStop() {
        presenter.unbind()
        disposable.clear()
        super.onStop()
    }
}