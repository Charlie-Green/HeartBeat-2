package by.vadim_churun.individual.heartbeat2.app.model.obj

import by.vadim_churun.individual.heartbeat2.app.db.view.PlaylistHeaderView


class PlaylistHeader(
    val ID: Int,
    val title: String,
    val artUri: String?,
    val songCount: Int,
    val totalDuration: Long
) {
    companion object {
        fun fromEntity(entity: PlaylistHeaderView)
            = PlaylistHeader(
                entity.playlist.ID,
                entity.playlist.title,
                entity.playlist.artUri,
                entity.songsCount,
                entity.duration
            )
    }
}