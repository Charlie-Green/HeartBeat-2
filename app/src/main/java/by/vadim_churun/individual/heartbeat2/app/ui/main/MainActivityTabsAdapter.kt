package by.vadim_churun.individual.heartbeat2.app.ui.main

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.ui.main.plist.PlaylistsCollectionFragment
import by.vadim_churun.individual.heartbeat2.app.ui.main.song.SongsCollectionFragment


class MainActivityTabsAdapter(mainActivity: FragmentActivity):
FragmentStateAdapter(mainActivity) {
    /** Returns label for the given tab. **/
    fun labelAt(res: Resources, position: Int)
        = when(position) {
            0 -> R.string.songs_tab
            1 -> R.string.plists_tab
            else -> 0
        }.let { res.getString(it) }

    override fun getItemCount()
        = 2

    override fun createFragment(position: Int)
        = when(position) {
            0 -> SongsCollectionFragment()
            1 -> PlaylistsCollectionFragment()
            else -> throw IllegalArgumentException("position = $position")
        }
}