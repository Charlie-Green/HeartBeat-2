package by.vadim_churun.individual.heartbeat2.app.ui.main.song

import by.vadim_churun.individual.heartbeat2.app.model.obj.SongsList


internal object SongsCollectionEditor {
    private var mAllSongs: SongsList? = null
    private var mPlaylistSongs: SongsList? = null
    private val userChecks = HashMap<Int, Boolean>()

    var playlistSongs: SongsList
        get() = mPlaylistSongs
            ?: throw IllegalStateException("playlistSongs hasn't been set" )
        set(value) {
            mPlaylistSongs = value
        }

    var allSongs: SongsList
        get() = mAllSongs
            ?: throw IllegalStateException("allSongs hasn't been set" )
        set(value) {
            mAllSongs = value
        }

    fun mustCheckSong(songID: Int): Boolean
        = userChecks[songID] ?: (this.playlistSongs.indexOf(songID) != null)

    fun applyUserCheck(songID: Int, isChecked: Boolean) {
        userChecks[songID] = isChecked
    }
}