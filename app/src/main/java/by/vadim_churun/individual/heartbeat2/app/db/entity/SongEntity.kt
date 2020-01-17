package by.vadim_churun.individual.heartbeat2.app.db.entity

import androidx.room.*
import by.vadim_churun.individual.heartbeat2.shared.Song
import by.vadim_churun.individual.heartbeat2.shared.SongsSource


@Entity(tableName = "Songs")
class SongEntity(
    @PrimaryKey
    @ColumnInfo(name="id")         val ID: Int,

    @ColumnInfo(name="title")      val title: String,

    @ColumnInfo(name="artist")     val artist: String?,

    @ColumnInfo(name="dur")        val duration: Long,

    @ColumnInfo(name="filename")   val filename: String?,

    @ColumnInfo(name="contentUri") val contentUri: String,

    /** Knowing what source a song came from is needed to request its art. **/
    @ColumnInfo(name="source")     val sourceClass: Class<out SongsSource>
) {
    @Ignore private var correspondingSong: Song? = null

    fun toSong()
        = correspondingSong ?:
            Song(ID, title, artist, duration, filename, contentUri)
            .also { correspondingSong = it }

    companion object {
        fun fromSong(song: Song, sourceClass: Class<out SongsSource>)
            = SongEntity(
                song.ID,
                song.title,
                song.artist,
                song.duration,
                song.filename,
                song.contentUri,
                sourceClass
            ).apply { correspondingSong = song }
    }
}