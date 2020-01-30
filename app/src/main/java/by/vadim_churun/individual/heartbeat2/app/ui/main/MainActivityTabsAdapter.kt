package by.vadim_churun.individual.heartbeat2.app.ui.main

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.ui.common.SearchViewOwner
import by.vadim_churun.individual.heartbeat2.app.ui.main.plist.PlaylistsCollectionFragment
import by.vadim_churun.individual.heartbeat2.app.ui.main.song.SongsCollectionFragment


internal class MainActivityTabsAdapter(
    mainActivity: FragmentActivity
): FragmentStateAdapter(mainActivity) {
    companion object {
        private val searches = Array<CharSequence>(2) { "" }
    }

    /** Returns the value associated with [SearchViewOwner.KEY_COMPONENT_ID]
      * for the given page. **/
    fun searchViewOwnerIdAt(position: Int)
        = position

    fun lastSearchAt(position: Int)
        = searches[position]
    fun saveSearchAt(position: Int, query: CharSequence) {
        searches[position] = query
    }

    /** Returns label for the given tab. **/
    fun labelAt(res: Resources, position: Int)
        = when(position) {
            0 -> R.string.songs_tab
            1 -> R.string.plists_tab
            else -> 0
        }.let { res.getString(it) }


    override fun getItemCount()
        = searches.size

    override fun createFragment(position: Int)
        = when(position) {
            0 -> SongsCollectionFragment()
            1 -> PlaylistsCollectionFragment()
            else -> throw IllegalArgumentException("position = $position")
        }.apply {
            arguments = Bundle()
            arguments!!.putInt(
                SearchViewOwner.KEY_COMPONENT_ID, searchViewOwnerIdAt(position) )
        }
}