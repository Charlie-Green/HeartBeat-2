package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import android.content.Context
import by.vadim_churun.individual.heartbeat2.app.db.HeartBeatDatabase
import by.vadim_churun.individual.heartbeat2.app.db.entity.SongEntity
import javax.inject.Inject


/** This class is responsible for communicating with the database. **/
class DatabaseManager @Inject constructor(val appContext: Context) {
    ///////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE:

    private val songsDAO
        get() = HeartBeatDatabase.get(appContext).songsDao

    private val playlistsDAO
        get() = HeartBeatDatabase.get(appContext).playlistsDao

    private val playlistItemsDAO
        get() = HeartBeatDatabase.get(appContext).playlistItemsDao


    ///////////////////////////////////////////////////////////////////////////////////////
    // SONGS:

    fun rawSongs()
        = this.songsDAO.get()

    fun observableSongs()
        = this.songsDAO.getRx()

    fun updateSongs(removedIds: List<Int>, added: List<SongEntity>) {
        if(removedIds.isEmpty() && added.isEmpty()) return
        HeartBeatDatabase.get(appContext).runInTransaction {
            if(removedIds.isNotEmpty()) {
                this.playlistItemsDAO.deleteForSongs(removedIds)
                this.songsDAO.delete(removedIds)
            }
            this.songsDAO.addOrUpdate(added)
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    // PLAYLISTS:

    fun observablePlaylistHeaders()
        = this.playlistsDAO.headersRx()
}