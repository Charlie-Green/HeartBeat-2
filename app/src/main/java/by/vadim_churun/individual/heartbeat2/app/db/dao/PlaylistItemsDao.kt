package by.vadim_churun.individual.heartbeat2.app.db.dao

import androidx.room.Dao
import androidx.room.Query
import by.vadim_churun.individual.heartbeat2.app.db.view.SongInPlaylistView
import io.reactivex.Observable


@Dao
abstract class PlaylistItemsDao {
    @Query("select S.id as s_id, " +
                  "S.title as s_title, " +
                  "S.artist as s_artist, " +
                  "S.dur as s_dur, " +
                  "S.filename as s_filename, " +
                  "S.contentUri as s_contentUri, " +
                  "S.source as s_source, " +
                  "S.id as i_song, " +
                  ":playlistID as i_playlist, " +
                  "I.rate as i_rate, " +
                  "I.volume as i_volume, " +
                  "I.prio as i_prio " +
           "from Songs as S " +
                "inner join PlaylistItems as I on S.id=I.song " +
           "where I.playlist=:playlistID" )
    abstract fun playlistContentRx(playlistID: Int): Observable< List<SongInPlaylistView> >

    @Query("delete from PlaylistItems where playlist=:playlistID")
    abstract fun deleteFromPlaylist(playlistID: Int)


    @Query("delete from PlaylistItems where song in (:songIds)")
    abstract fun deleteForSongs(songIds: List<Int>)
}