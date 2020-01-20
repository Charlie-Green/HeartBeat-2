package by.vadim_churun.individual.heartbeat2.app.db.dao

import androidx.room.*
import by.vadim_churun.individual.heartbeat2.app.db.entity.PlaylistEntity
import by.vadim_churun.individual.heartbeat2.app.db.view.PlaylistHeaderView
import io.reactivex.Observable


@Dao
abstract class PlaylistsDao {
    @Query("select P.id as p_id, " +
                  "P.title as p_title, " +
                  "P.art as p_art, " +
                  "count(*) as songs, " +
                  "sum(S.dur) as dur " +
           "from Playlists as P " +
                "inner join PlaylistItems as I on I.playlist=P.id " +
                "inner join Songs as S on I.song=S.id " +
           "group by P.id, P.title, P.art" )
    abstract fun headersRx(): Observable< List<PlaylistHeaderView> >

    @Query("select count(*) from Playlists where title=:title")
    abstract fun countByTitle(title: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addOrUpdate(playlist: PlaylistEntity): Long

    @Query("delete from Playlists where id=:playlistID")
    abstract fun delete(playlistID: Int)
}