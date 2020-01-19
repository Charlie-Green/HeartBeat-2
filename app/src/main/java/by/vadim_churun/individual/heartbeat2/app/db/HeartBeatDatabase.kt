package by.vadim_churun.individual.heartbeat2.app.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import by.vadim_churun.individual.heartbeat2.app.db.dao.*
import by.vadim_churun.individual.heartbeat2.app.db.entity.*


@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class HeartBeatDatabase: RoomDatabase() {
    abstract val songsDao: SongsDao
    abstract val playlistsDao: PlaylistsDao
    abstract val playlistItemsDao: PlaylistItemsDao


    companion object {
        private var instance: HeartBeatDatabase? = null

        private fun buildInstance(appContext: Context)
            = Room.databaseBuilder(appContext, HeartBeatDatabase::class.java, "heartbeat2.db")
                .addMigrations(/* TODO */)
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