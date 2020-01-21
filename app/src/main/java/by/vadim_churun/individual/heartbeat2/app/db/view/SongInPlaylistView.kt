package by.vadim_churun.individual.heartbeat2.app.db.view

import androidx.room.Embedded
import by.vadim_churun.individual.heartbeat2.app.db.entity.PlaylistItemEntity
import by.vadim_churun.individual.heartbeat2.app.db.entity.SongEntity


class SongInPlaylistView(
    @Embedded(prefix="s_") val song: SongEntity,
    @Embedded(prefix="i_") val plistItem: PlaylistItemEntity
)