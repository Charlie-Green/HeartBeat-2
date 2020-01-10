package by.vadim_churun.individual.heartbeat2.model.logic.internal

import android.content.Context
import by.vadim_churun.individual.heartbeat2.db.HeartBeatDatabase
import by.vadim_churun.individual.heartbeat2.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import javax.inject.Inject


/** This class is responsible for communicating with the database. **/
class DatabaseManager @Inject constructor(val appContext: Context) {
    private val songsDAO
        get() = HeartBeatDatabase.get(appContext).songsDao

    fun observableSongs()
        = this.songsDAO.getRx()

    fun updateSource(sourceClass: Class<out SongsSource>, newSongs: List<SongEntity>)
        = this.songsDAO.updateSource(sourceClass, newSongs)
}