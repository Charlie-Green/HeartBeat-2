package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.individual.heartbeat2.app.R
import kotlinx.android.synthetic.main.plist_page.view.*


class PlaylistsCollectionAdapter:
RecyclerView.Adapter<PlaylistsCollectionAdapter.PlaylistViewHolder>() {
    ////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class PlaylistViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imgvArt     = itemView.imgvArt
        val tvTitle     = itemView.tvTitle
        val tvDuration  = itemView.tvDuration
        val tvSongCount = itemView.tvSongCount
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // ARTS:

    private var colorPrimary = 0

    private fun getPrimaryColor(context: Context)
        = if(colorPrimary == 0 /* Hasn't been resolved */) {
            TypedValue().also {
                context.theme.resolveAttribute(android.R.attr.colorPrimary, it, true)
            }.data
            .also { colorPrimary = it }
        } else {
            colorPrimary
        }


    ////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER IMPLEMENTATION:

    override fun getItemCount()
        = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(parent.context)
            .inflate(R.layout.plist_page, parent, false)
            .let { PlaylistViewHolder(it) }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.imgvArt.setImageResource(R.drawable.ic_playlist)
        val plistDrawable = holder.imgvArt.drawable
        plistDrawable.setTint(getPrimaryColor(holder.itemView.context))
        holder.imgvArt.setImageDrawable(plistDrawable)
        holder.tvTitle.text = "Playlist ${position+1} ".repeat(8)
        holder.tvTitle.isSelected = true  // Let the title marquee.
    }
}