package by.vadim_churun.individual.heartbeat2.app.db.dao

import androidx.room.Dao
import androidx.room.Query
import by.vadim_churun.individual.heartbeat2.app.db.entity.PlaylistItemEntity
import io.reactivex.Observable


@Dao
abstract class PlaylistItemsDao {
    @Query("select * from PlaylistItems where playlist=:playlistID")
    abstract fun forPlaylistRx(playlistID: Int): Observable< List<PlaylistItemEntity> >

    @Query("delete from PlaylistItems where playlist=:playlistID")
    abstract fun deleteFromPlaylist(playlistID: Int)


    @Query("delete from PlaylistItems where song in (:songIds)")
    abstract fun deleteForSongs(songIds: List<Int>)
}