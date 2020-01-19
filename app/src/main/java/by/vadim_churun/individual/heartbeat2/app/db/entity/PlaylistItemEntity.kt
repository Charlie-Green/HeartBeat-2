package by.vadim_churun.individual.heartbeat2.app.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(tableName = "PlaylistItems", primaryKeys = ["song", "playlist"])
class PlaylistItemEntity(
    @ColumnInfo(name="song")     val songID: Int,
    @ColumnInfo(name="playlist") val playlistID: Int,
    @ColumnInfo(name="rate")     val rate: Float,
    @ColumnInfo(name="volume")   val volume: Float,
    @ColumnInfo(name="prio")     val priority: Byte
)