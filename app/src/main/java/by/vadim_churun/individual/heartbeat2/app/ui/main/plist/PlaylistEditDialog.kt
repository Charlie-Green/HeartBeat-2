package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import by.vadim_churun.individual.heartbeat2.app.R
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

    private var playlistID = 0

    private fun View.giveEnoughWidth() {
        val activity = this@PlaylistEditDialog.requireActivity()
        val display = activity.windowManager.defaultDisplay
        val displayWidth = DisplayMetrics().also {
            display.getMetrics(it)
        }.widthPixels
        this.minimumWidth = 0.90.times(displayWidth).toInt()
    }

    private fun adaptUiByPlaylistExistance() {
        if(playlistID == 0) {  // Creating a new playlist
            tvLastTitle.visibility = View.GONE
        } else {               // Updating an existing one
            tvHeader.setText(R.string.update_playlist_suggestion)
            buSubmit.setText(R.string.update_playlist_submit)
        }
    }

    private fun handleSubmit()  {
        val title = etTitle.text.toString()
        if(title.isEmpty()) {
            tvError.setText(R.string.plist_title_empty)
            return
        }

        if(playlistID == 0)
            subjectAdd.onNext( PlaylistEditAction.Add(title) )
        else
            subjectUpdate.onNext(
                PlaylistEditAction.Update(playlistID, title) )
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // MVI:

    private val presenter = PlaylistEditPresenter()
    private val subjectAdd
        = PublishSubject.create<PlaylistEditAction.Add>()
    private val subjectUpdate
        = PublishSubject.create<PlaylistEditAction.Update>()


    override fun intentAdd(): Observable<PlaylistEditAction.Add>
        = subjectAdd

    override fun intentUpdate(): Observable<PlaylistEditAction.Update>
        = subjectUpdate


    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()
    private lateinit var draftsAccessor: PlaylistDraftsAccessor

    private fun subscribeDraft()
        = draftsAccessor.run {
                if(playlistID == 0) maybeNew() else maybeUpdated(playlistID)
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

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(R.layout.playlist_edit_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.giveEnoughWidth()
        draftsAccessor = PlaylistDraftsAccessor(super.requireActivity())

        playlistID = super.getArguments()?.getInt(KEY_PLAYLIST_ID) ?: 0
        adaptUiByPlaylistExistance()
        disposable.add(subscribeDraft())
        buCancel.setOnClickListener { super.dismiss() }
        buSubmit.setOnClickListener { handleSubmit() }

        val serviceSource = super.requireActivity() as ServiceSource
        disposable.add(subscribeService(serviceSource))
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val title = etTitle.text.toString()
        if(playlistID == 0)
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