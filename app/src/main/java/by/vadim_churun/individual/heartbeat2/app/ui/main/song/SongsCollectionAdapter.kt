package by.vadim_churun.individual.heartbeat2.app.ui.main.song

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.*
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.presenter.song.SongsCollectionAction
import by.vadim_churun.individual.heartbeat2.app.ui.common.UiUtils
import kotlinx.android.synthetic.main.song_listitem.view.*


internal class SongsCollectionAdapter(
    val context: Context,
    val modeEdit: Boolean
): RecyclerView.Adapter<SongsCollectionAdapter.SongViewHolder>() {
    ////////////////////////////////////////////////////////////////////////////////////////
    // HELP:

    val displayedSongs
        get() = if(modeEdit) SongsCollectionEditor.allSongs
            else SongsCollectionEditor.playlistSongs


    ////////////////////////////////////////////////////////////////////////////////////////
    // ARTS

    private val primaryColor by lazy {
        TypedValue().also {
            context.theme.resolveAttribute(android.R.attr.colorPrimary, it, true)
        }.data
    }
    private val arts = MutableList<Bitmap?>(this.displayedSongs.size) { null }

    fun applySongArt(songID: Int, art: Bitmap) {
        val position = this.displayedSongs.indexOf(songID) ?: return
        arts[position] = art
        super.notifyItemChanged(position)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // HIGHLIGHTING A SONG:

    private var indexHighlight = -1

    val highlightedPosition
        get() = if(indexHighlight >= 0) indexHighlight else null

    fun highlightSong(songID: Int) {
        val newIndex = this.displayedSongs.indexOf(songID)
        when(newIndex) {
            indexHighlight -> { /* Do nothing. */ }

            null -> {
                // Set no highlighting.
                val oldIndex = indexHighlight
                indexHighlight = -1
                if(oldIndex >= 0)
                    super.notifyItemChanged(oldIndex)  // Remove selection here.
            }

            else -> {
                // Highlight the new item.
                val oldIndex = indexHighlight
                indexHighlight = newIndex
                super.notifyItemChanged(oldIndex)  // Remove the previous selection.
                super.notifyItemChanged(newIndex)  // Set the new one.
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER IMPLEMENTATION:

    class SongViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imgvArt           = itemView.imgvArt
        val tvTitle           = itemView.tvTitle
        val tvArtist          = itemView.tvArtist
        val priorityIndicator = itemView.priorityIndicator
        val tvDuration        = itemView.tvDuration
        val chbIsInPlaylist   = itemView.chbIsInPlaylist
    }


    override fun getItemCount(): Int
        = this.displayedSongs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(context)
            .inflate(R.layout.song_listitem, parent, false)
            .let { SongViewHolder(it) }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        // Potentially highlights this item:
        holder.itemView.isSelected = (position == indexHighlight)

        val entry = this.displayedSongs[position]
        if(modeEdit) {
            holder.priorityIndicator.visibility = View.GONE
            holder.chbIsInPlaylist.visibility = View.VISIBLE

            holder.chbIsInPlaylist.isChecked =
                SongsCollectionEditor.mustCheckSong(entry.song.ID)
            holder.chbIsInPlaylist.setOnClickListener { v ->
                val isChecked = (v as CheckBox).isChecked
                SongsCollectionEditor.applyUserCheck(entry.song.ID, isChecked)
            }
        } else {
            holder.chbIsInPlaylist.visibility = View.GONE
            holder.priorityIndicator.visibility = View.VISIBLE
            holder.priorityIndicator.alpha = 0.2f*entry.song.priority
        }

        holder.tvTitle.text = entry.stub.displayTitle
        holder.tvArtist.text = entry.stub.displayArtist
        holder.tvDuration.text = UiUtils.timeString(entry.song.duration)

        arts[position]?.also {
            holder.imgvArt.setImageBitmap(it)
        } ?: holder.imgvArt.apply {
            setImageResource(R.drawable.song_art_default)
            val noArtDrawable = this.drawable!!
            noArtDrawable.setTint(primaryColor)
            setImageDrawable(noArtDrawable)
        }.also {
            val action = SongsCollectionAction.DecodeArt(entry.song)
            SongsCollectionSubjects.DECODE_ART.onNext(action)
        }

        holder.itemView.setOnClickListener {
            val action = SongsCollectionAction.Play(entry.song)
            SongsCollectionSubjects.PLAY.onNext(action)
        }
    }
}