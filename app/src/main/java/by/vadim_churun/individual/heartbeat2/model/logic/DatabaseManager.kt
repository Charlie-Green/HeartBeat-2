package by.vadim_churun.individual.heartbeat2.model.logic

import android.content.Context
import by.vadim_churun.individual.heartbeat2.db.HeartBeatDatabase
import by.vadim_churun.individual.heartbeat2.db.entity.SongEntity
import javax.inject.Inject


/** This class is responsible for communicating with the database. **/
class DatabaseManager @Inject constructor(val appContext: Context) {
    private val songsDAO
        get() = HeartBeatDatabase.get(appContext).songsDao

    fun observableSongs()
        = this.songsDAO.getRx()

    fun addOrUpdateSongs(songs: List<SongEntity>)
        = this.songsDAO.addOrUpdate(songs)
}