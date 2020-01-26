package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import android.content.Context
import by.vadim_churun.individual.heartbeat2.app.db.HeartBeatDatabase
import by.vadim_churun.individual.heartbeat2.app.db.entity.*
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

    private fun transaction(body: () -> Unit)
        = HeartBeatDatabase.get(appContext).runInTransaction(body)


    ///////////////////////////////////////////////////////////////////////////////////////
    // SONGS:

    fun rawSongs()
        = this.songsDAO.get()

    fun observableSongs()
        = this.songsDAO.getRx()

    fun updateSongs(removedIds: List<Int>, added: List<SongEntity>) {
        if(removedIds.isEmpty() && added.isEmpty()) return
        transaction {
            if(removedIds.isNotEmpty()) {
                this.playlistItemsDAO.deleteForSongs(removedIds)
                this.songsDAO.delete(removedIds)
            }
            this.songsDAO.addOrUpdate(added)
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    // PLAYLISTS:

    fun observablePlaylist(playlistID: Int)
        = this.playlistsDAO.getRx(playlistID)

    fun observablePlaylistHeaders()
        = this.playlistsDAO.headersRx()

    fun addPlaylistIfNew(playlist: PlaylistEntity): Boolean
        = HeartBeatDatabase.get(appContext).runInTransaction<Boolean> {
            if(this.playlistsDAO.countByTitle(playlist.title) > 0)
                return@runInTransaction false
            this.playlistsDAO.addOrUpdate(playlist)
            return@runInTransaction true
        }

    fun addOrUpdatePlaylist(playlist: PlaylistEntity)
        = this.playlistsDAO.addOrUpdate(playlist)


    ///////////////////////////////////////////////////////////////////////////////////////
    // PLAYLIST ITEMS:

    fun observablePlaylistContent(playlistID: Int)
        = this.playlistItemsDAO.playlistContentRx(playlistID)

    fun updatePlaylistContent
    (playlistID: Int, removedSongIDs: List<Int>, addedSongIDs: List<Int>) {
        val addedSongs = this.songsDAO.get(addedSongIDs)
        val addedItems = addedSongs.map { song ->
            // Initially, songs are added with default settings.
            // The settings can be modified later.
            PlaylistItemEntity(song.ID, playlistID, 1f, 1f, 3)
        }
        transaction {
            this.playlistItemsDAO.deleteFromPlaylist(playlistID, removedSongIDs)
            this.playlistItemsDAO.add(addedItems)
        }
    }
}