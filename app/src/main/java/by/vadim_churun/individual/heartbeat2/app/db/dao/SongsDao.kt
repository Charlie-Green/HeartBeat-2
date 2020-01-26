package by.vadim_churun.individual.heartbeat2.app.db.dao

import androidx.room.*
import by.vadim_churun.individual.heartbeat2.app.db.entity.SongEntity
import io.reactivex.Observable


@Dao
abstract class SongsDao {
    @Query("select * from Songs")
    abstract fun get(): List<SongEntity>

    @Query("select * from Songs where id in (:ids)")
    abstract fun get(ids: List<Int>): List<SongEntity>

    @Query("select * from Songs")
    abstract fun getRx(): Observable< List<SongEntity> >

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addOrUpdate(songs: List<SongEntity>)

    @Query("delete from Songs where id in (:songIds)")
    abstract fun delete(songIds: List<Int>)
}