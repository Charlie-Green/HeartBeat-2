package by.vadim_churun.individual.heartbeat2.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import by.vadim_churun.individual.heartbeat2.db.dao.SongsDao
import by.vadim_churun.individual.heartbeat2.db.entity.SongEntity


@Database(
    entities = [
        SongEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class HeartBeatDatabase: RoomDatabase() {
    abstract val songsDao: SongsDao

    companion object {
        private var instance: HeartBeatDatabase? = null

        private fun buildInstance(appContext: Context)
            = Room.databaseBuilder(appContext, HeartBeatDatabase::class.java, "heartbeat2.db")
                .addCallback(object: RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // TODO: Prepopulate the DB with "example" playlists,
                        //  e. g. 'Christmas Songs'.
                    }
                }).build()

        fun get(appContext: Context)
            = instance ?: synchronized(this) {
                instance ?: buildInstance(appContext).also { instance = it }
            }
    }
}