package by.vadim_churun.individual.heartbeat2.db.dao

import androidx.room.*
import by.vadim_churun.individual.heartbeat2.db.entity.SongEntity
import io.reactivex.Observable


@Dao
interface SongsDao {
    @Query("select * from Songs")
    fun getRx(): Observable< List<SongEntity> >

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrUpdate(songs: List<SongEntity>)
}