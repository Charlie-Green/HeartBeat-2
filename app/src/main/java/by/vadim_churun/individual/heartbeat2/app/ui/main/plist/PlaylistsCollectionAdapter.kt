package by.vadim_churun.individual.heartbeat2.app.ui.main.plist

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.model.obj.PlaylistsCollection
import by.vadim_churun.individual.heartbeat2.app.presenter.plist.PlaylistsCollectionAction
import by.vadim_churun.individual.heartbeat2.app.ui.common.UiUtils
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.plist_page.view.*


class PlaylistsCollectionAdapter(
    private val playlists: PlaylistsCollection,
    private val decodeArtSubject: Subject<in PlaylistsCollectionAction.DecodeArt>
):
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

    fun setArt(playlistID: Int, art: Bitmap) {
        val playlistIndex = playlists.indexOf(playlistID) ?: return
        playlists.setArtAt(playlistIndex, art)
        super.notifyItemChanged(playlistIndex + 1)
    }

    private fun ImageView.setInsteadOfArt(resID: Int) {
        setImageResource(resID)
        val plistDrawable = this.drawable
        plistDrawable.setTint( getPrimaryColor(this.context) )
        setImageDrawable(plistDrawable)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER IMPLEMENTATION:

    private fun bindPlaylist(holder: PlaylistViewHolder, playlistIndex: Int) {
        val plist = playlists[playlistIndex]
        holder.tvTitle.text = plist.title
        holder.tvTitle.isSelected = true  // Let the title marquee.
        holder.tvSongCount.text = "${plist.songCount}"
        holder.tvDuration.text = UiUtils.timeString(plist.totalDuration)

        val art = playlists.artAt(playlistIndex)
        if(art == null) {
            holder.imgvArt.setInsteadOfArt(R.drawable.ic_playlist)
            if(plist.artUri != null)
                decodeArtSubject.onNext(PlaylistsCollectionAction.DecodeArt(plist))
        } else {
            holder.imgvArt.setImageBitmap(art)
        }
    }

    private fun bindAllSongsItem(holder: PlaylistViewHolder) {
        holder.imgvArt.setInsteadOfArt(R.drawable.ic_all_songs)
        holder.tvTitle.setText(R.string.all_songs_label)
    }


    override fun getItemCount()
        = playlists.size + 1  // +1 for "All Songs"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(parent.context)
            .inflate(R.layout.plist_page, parent, false)
            .let { PlaylistViewHolder(it) }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        if(position == 0)
            bindAllSongsItem(holder)          // Position 0 is for the "All Songs" item.
        else
            bindPlaylist(holder, position-1)  // Position J is for the playlist #(J-1), J >= 1.
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // OTHER:

    fun headerAt(adapterPosition: Int)
        = if(adapterPosition >= 1) playlists[adapterPosition-1]
            else null
}