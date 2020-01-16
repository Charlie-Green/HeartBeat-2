package by.vadim_churun.individual.heartbeat2.ui.main.song

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.model.obj.SongsList
import by.vadim_churun.individual.heartbeat2.presenter.song.SongsCollectionAction
import by.vadim_churun.individual.heartbeat2.ui.common.UiUtils
import kotlinx.android.synthetic.main.song_listitem.view.*


class SongsCollectionAdapter(
    val context: Context,
    val songs: SongsList
): RecyclerView.Adapter<SongsCollectionAdapter.SongViewHolder>() {
    ////////////////////////////////////////////////////////////////////////////////////////
    // ARTS

    private val primaryColor by lazy {
        TypedValue().also {
            context.theme.resolveAttribute(android.R.attr.colorPrimary, it, true)
        }.data
    }
    private val arts = MutableList<Bitmap?>(songs.size) { null }

    fun applySongArt(songID: Int, art: Bitmap) {
        val position = songs.indexOf(songID) ?: return
        arts[position] = art
        super.notifyItemChanged(position)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    // HIGHLIGHTING A SONG:

    private var indexHighlight = -1

    val highlightedPosition
        get() = if(indexHighlight >= 0) indexHighlight else null

    fun highlightSong(songID: Int) {
        val newIndex = songs.indexOf(songID)
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
    }


    override fun getItemCount(): Int
        = songs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(context)
            .inflate(R.layout.song_listitem, parent, false)
            .let { SongViewHolder(it) }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        // Potentially highlights this item:
        holder.itemView.isSelected = (position == indexHighlight)

        val entry = songs[position]
        holder.tvTitle.text = entry.stub.displayTitle
        holder.tvArtist.text = entry.stub.displayArtist
        holder.tvDuration.text = UiUtils.timeString(entry.song.duration)
        holder.priorityIndicator.alpha = 0.2f*entry.song.priority

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