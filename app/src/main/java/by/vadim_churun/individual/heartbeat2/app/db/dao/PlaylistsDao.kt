package by.vadim_churun.individual.heartbeat2.app.db.dao

import androidx.room.*
import by.vadim_churun.individual.heartbeat2.app.db.entity.PlaylistEntity
import io.reactivex.Observable


@Dao
abstract class PlaylistsDao {
    @Query("select * from Playlists")
    abstract fun getRx(): Observable<PlaylistEntity>

    @Query("select count(*) from Playlists where title=:title")
    abstract fun countByTitle(title: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addOrUpdate(playlist: PlaylistEntity): Long

    @Query("delete from Playlists where id=:playlistID")
    abstract fun delete(playlistID: Int)
}