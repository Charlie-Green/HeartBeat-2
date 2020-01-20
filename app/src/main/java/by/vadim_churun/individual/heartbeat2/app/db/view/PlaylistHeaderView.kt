package by.vadim_churun.individual.heartbeat2.app.db.view

import androidx.room.ColumnInfo
import androidx.room.Embedded
import by.vadim_churun.individual.heartbeat2.app.db.entity.PlaylistEntity


class PlaylistHeaderView(
    @Embedded(prefix = "p_")     val playlist: PlaylistEntity,
    @ColumnInfo(name="songs")    val songsCount: Int,
    @ColumnInfo(name="dur")      val duration: Long
)