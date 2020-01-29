package by.vadim_churun.individual.heartbeat2.app.ui.main.song

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.presenter.song.PlaylistContentModifAction
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


internal object UpdatePlistContentDialog {
    //////////////////////////////////////////////////////////////////////////////////////////
    // CLASSES:

    class UpdateInfo(
        val playlistID: Int,
        val playlistName: String,
        val userChecks: HashMap<Int, Boolean>
    )

    private class UpdateStatistics(
        val playlistID: Int,
        val playlistName: String,
        val userChecks: HashMap<Int, Boolean>,
        val countAdded: Int,
        val countRemoved: Int
    ) { var dialogDismissed = false }

    enum class DismissReason {
        NOTHING_TO_UPDATE,
        CONFIRMED,
        DENIED,
        OUTSIDE_TAPPED
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private var mFragment: Fragment? = null
    var fragment: Fragment
        get() = mFragment ?: throw IllegalStateException("Fragment not set")
        set(newFragment) {
            mFragment = newFragment

            // Resubscribe for the new Fragment.
            // If there is a dialog shown but not dismissed before,
            // it will be re-displayed since subjectShow is a BehaviorSubject.
            disposable.clear()
            disposable.addAll(
                subscribeComputeStatistics(),
                subscribeShow()
            )
        }

    private val disposable = CompositeDisposable()
    private val subjectComputeStatistics = PublishSubject.create<UpdateInfo>()
    private val subjectShow = BehaviorSubject.create<UpdateStatistics>()
    private val subjectDismissed = PublishSubject.create<DismissReason>()

    private fun subscribeComputeStatistics()
        = subjectComputeStatistics
            .observeOn(Schedulers.computation())
            .doOnNext { info ->
                var added = 0; var removed = 0
                for(entry in info.userChecks) {
                    if(entry.value == true)
                        ++added
                    else
                        ++removed
                }

                UpdateStatistics(
                    info.playlistID,
                    info.playlistName,
                    info.userChecks,
                    added,
                    removed
                ).also { subjectShow.onNext(it) }
            }.subscribe()

    private fun subscribeShow()
        = subjectShow
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { statistics ->
                if(statistics.dialogDismissed || !fragment.isAdded)
                    return@doOnNext
                if(statistics.countAdded + statistics.countRemoved > 0)
                    showDialog(statistics)
                else
                    subjectDismissed.onNext(DismissReason.NOTHING_TO_UPDATE)
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////
    // DIALOG BUILDER:

    private fun showDialog(statistics: UpdateStatistics) {
        val title = fragment.getString(
            R.string.update_playlist_content_suggestion_f, statistics.playlistName )
        val strAdded = fragment.getString(
            R.string.will_be_added_to_playlist_f, statistics.countAdded )
        val strRemoved = fragment.getString(
            R.string.will_be_removed_from_playlist_f, statistics.countRemoved )

        var dismissReason = DismissReason.OUTSIDE_TAPPED
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(title)
            .setMessage("${strAdded}\n${strRemoved}")
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dismissReason = DismissReason.CONFIRMED
                propagateUpdate(statistics)
                dialog.dismiss()
            }.setNegativeButton(R.string.no) { dialog, _ ->
                dismissReason = DismissReason.DENIED
                dialog.dismiss()
            }.setOnDismissListener { _ ->
                statistics.dialogDismissed = true
                subjectDismissed.onNext(dismissReason)
            }.show()
    }

    private fun propagateUpdate(statistics: UpdateStatistics) {
        PlaylistContentModifAction.UpdateContent(
            statistics.playlistID, statistics.userChecks
        ).also {
            SongsCollectionSubjects.UPDATE_PLAYLIST_CONTENT.onNext(it)
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // API:

    fun show(info: UpdateInfo) {
        subjectComputeStatistics.onNext(info)
    }

    fun observableDismissed(): Observable<DismissReason>
        = subjectDismissed

    fun clear() {
        disposable.clear()
    }
}