package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/** A helper object to persist user input till it is commited to the database. **/
internal class PlaylistDraftsAccessor(
    val activity: Activity
) {
    companion object {
        private const val KEY_UPDATED_ID    = "uID"
        private const val KEY_UPDATED_TITLE = "uTitle"
        private const val KEY_NEW_TITLE     = "title"
    }

    private val prefs
        get() = activity.getSharedPreferences("playlist_drafts.dat", Context.MODE_PRIVATE)


    /** A helper method to construct Maybe's. **/
    private fun maybe(titleKey: String, idKey: String?, id: Int): Maybe<String>
        = Maybe.create<String> { emitter ->
            val sp = this.prefs
            if(idKey != null) {
                // Make sure that persisted ID matches the requested value.
                if( !sp.contains(idKey) || sp.getInt(idKey, 0) != id ) {
                    emitter.onComplete()
                    return@create
                }
            }

            if(sp.contains(titleKey))
                emitter.onSuccess( sp.getString(KEY_NEW_TITLE, "")!! )
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())


    fun maybeNew(): Maybe<String>
        = maybe(KEY_NEW_TITLE, null, 0)

    fun persistNew(title: String) {
        this.prefs.edit {
            putString(KEY_NEW_TITLE, title)
        }
    }


    fun maybeUpdated(id: Int): Maybe<String>
        = maybe(
        KEY_UPDATED_TITLE,
        KEY_UPDATED_ID, id)

    fun persistUpdated(id: Int, title: String) {
        this.prefs.edit {
            putInt(KEY_UPDATED_ID, id)
            putString(KEY_UPDATED_TITLE, title)
        }
    }
}