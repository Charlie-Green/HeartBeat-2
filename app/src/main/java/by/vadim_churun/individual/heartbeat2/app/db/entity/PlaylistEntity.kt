package by.vadim_churun.individual.heartbeat2.app.db.entity

import androidx.room.*


@Entity(tableName = "Playlists")
class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")    val ID: Int,

    @ColumnInfo(name="title") val title: String,

    @ColumnInfo(name="art")   val artUri: String?
)