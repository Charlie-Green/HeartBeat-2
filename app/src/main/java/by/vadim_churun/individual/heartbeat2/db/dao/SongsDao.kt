package by.vadim_churun.individual.heartbeat2.db.dao

import androidx.room.*
import by.vadim_churun.individual.heartbeat2.db.DatabaseConverters
import by.vadim_churun.individual.heartbeat2.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import io.reactivex.Observable


@Dao
abstract class SongsDao {
    private val converters by lazy { DatabaseConverters() }


    @Query("select * from Songs")
    abstract fun getRx(): Observable< List<SongEntity> >

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addOrUpdate(songs: List<SongEntity>)

    @Query("delete from Songs where source=:sourceCode")
    protected abstract fun deleteFromSource(sourceCode: Byte)


    /** Replaces all data from the specified [SongsSource] with the given new list **/
    @Transaction
    open fun updateSource(sourceClass: Class<out SongsSource>, newSongs: List<SongEntity>) {
        deleteFromSource(converters.sourceClassToByte(sourceClass))
        addOrUpdate(newSongs)
    }
}