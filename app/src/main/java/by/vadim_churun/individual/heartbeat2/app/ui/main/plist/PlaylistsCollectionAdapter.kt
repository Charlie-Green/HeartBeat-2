package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.graphics.Color
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.individual.heartbeat2.app.R
import kotlinx.android.synthetic.main.plist_page.view.*


class PlaylistsCollectionAdapter:
RecyclerView.Adapter<PlaylistsCollectionAdapter.PlaylistViewHolder>() {
    ////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class PlaylistViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvTitle = itemView.tvTitle
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER IMPLEMENTATION:

    /* N o t e :
     * For now, this is a "fake" implementation providing 3 fixed views.
     * Later it will be replaced with a "real" implementation
     * to browse over a collection of playlists. */

    override fun getItemCount()
        = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(parent.context)
            .inflate(R.layout.plist_page, parent, false)
            .let { PlaylistViewHolder(it) }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        when(position) {
            0    -> Color.RED
            1    -> Color.GREEN
            else -> Color.BLUE
        }.also { holder.itemView.setBackgroundColor(it) }
        holder.tvTitle.text = "Playlist ${position+1}"
    }
}