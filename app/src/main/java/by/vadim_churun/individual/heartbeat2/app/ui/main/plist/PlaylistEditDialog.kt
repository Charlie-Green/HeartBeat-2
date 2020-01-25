package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.model.obj.Playlist
import by.vadim_churun.individual.heartbeat2.app.model.state.PlaylistEditState
import by.vadim_churun.individual.heartbeat2.app.presenter.plist.*
import by.vadim_churun.individual.heartbeat2.app.ui.common.ServiceSource
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.playlist_edit_dialog.*


/** Allows creating and updating of a playlist**/
internal class PlaylistEditDialog: DialogFragment(), PlaylistEditUI {
    //////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION:

    companion object {
        /** Arguments bundle key for ID of the playlist to modify.
          * Ignore the extra to create a new playlist. **/
        val KEY_PLAYLIST_ID = "id"
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var idPlaylist = 0
    private var lastPlaylist: Playlist? = null

    private fun expandDialogHorizontally() {
        val activity = this@PlaylistEditDialog.requireActivity()
        val display = activity.windowManager.defaultDisplay
        val displayWidth = DisplayMetrics().also {
            display.getMetrics(it)
        }.widthPixels
        val dialogWith = 0.90.times(displayWidth).toInt()
        super.requireView().minimumWidth = dialogWith
        layoutContent.minimumWidth = dialogWith
    }

    private fun adaptUiByPlaylistExistance() {
        if(playlistID == 0) {  // Creating a new playlist
            tvLastTitle.visibility = View.GONE
        } else {               // Updating an existing one
            tvHeader.setText(R.string.update_playlist_suggestion)
            buSubmit.setText(R.string.update_playlist_submit)
        }
    }

    private fun displayLastPlaylist() {
        tvLastTitle.text = super.getString(
            R.string.last_plist_title_f, lastPlaylist!!.title )
    }

    private fun handleSubmit()  {
        val newTitle = etTitle.text.toString()
        if(newTitle.isEmpty()) {
            tvError.setText(R.string.plist_title_empty)
            return
        }

        if(playlistID == 0) {
            PlaylistEditAction.Add(newTitle)
                .also { subjectAdd.onNext(it) }
        } else {
            val plist = lastPlaylist ?: return
            Playlist(idPlaylist, newTitle, plist.artUri)
                .let { PlaylistEditAction.Update(it) }
                .also { subjectUpdate.onNext(it) }
        }
        buSubmit.isEnabled = false
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // MVI:

    private val presenter = PlaylistEditPresenter()
    private val subjectAdd
        = PublishSubject.create<PlaylistEditAction.Add>()
    private val subjectUpdate
        = PublishSubject.create<PlaylistEditAction.Update>()


    override val playlistID: Int
        get() = idPlaylist

    override fun intentAdd(): Observable<PlaylistEditAction.Add>
        = subjectAdd

    override fun intentUpdate(): Observable<PlaylistEditAction.Update>
        = subjectUpdate

    override fun render(state: PlaylistEditState) {
        prBar.isVisible = (state is PlaylistEditState.Processing)
        when(state) {
            is PlaylistEditState.Added -> {
                draftsAccessor.persistNew("")  // This draft's been used, so clear it.
                flagPersistOnDismiss = false
                super.dismiss()
            }

            is PlaylistEditState.Updated -> {
                super.dismiss()
            }

            is PlaylistEditState.AddRefused -> {
                when(state.reason) {
                    PlaylistEditState.AddRefused.Reason.TITLE_EXISTS ->
                        R.string.plist_title_exists
                }.also { tvError.setText(it) }
                buSubmit.isEnabled = true
            }

            is PlaylistEditState.LastPlaylistAvailable -> {
                lastPlaylist = state.oldPlaylist
                displayLastPlaylist()
            }
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()
    private lateinit var draftsAccessor: PlaylistDraftsAccessor

    private fun subscribeDraft()
        = draftsAccessor.run {
                if(idPlaylist == 0) maybeNew() else maybeUpdated(playlistID)
            }.doOnSuccess { title ->
                etTitle.setText(title)
            }.subscribe()

    private fun subscribeService(source: ServiceSource)
        = source.observableService()
            .doOnNext { service ->
                presenter.bind(service, this)
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    private var flagPersistOnDismiss = true

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(R.layout.playlist_edit_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        expandDialogHorizontally()
        draftsAccessor = PlaylistDraftsAccessor(super.requireActivity())

        idPlaylist = super.getArguments()?.getInt(KEY_PLAYLIST_ID) ?: 0
        adaptUiByPlaylistExistance()
        disposable.add(subscribeDraft())
        buCancel.setOnClickListener { super.dismiss() }
        buSubmit.setOnClickListener { handleSubmit() }

        val serviceSource = super.requireActivity() as ServiceSource
        disposable.add(subscribeService(serviceSource))
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if(flagPersistOnDismiss != true) return
        val title = etTitle.text.toString()
        if(idPlaylist == 0)
            draftsAccessor.persistNew(title)
        else
            draftsAccessor.persistUpdated(playlistID, title)
    }

    override fun onDestroy() {
        presenter.unbind()
        disposable.clear()
        super.onDestroy()
    }
}