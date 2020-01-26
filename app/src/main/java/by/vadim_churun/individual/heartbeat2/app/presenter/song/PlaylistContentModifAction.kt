package by.vadim_churun.individual.heartbeat2.app.presenter.song


sealed class PlaylistContentModifAction {
    class UpdateContent(
        /** ID of the playlist to update. **/
        val playlistID: Int,

        /** Specifies songs to be added/removed from the playlist.
          * A key is a song ID. The value is whether this song is to be added. **/
        val userChecks: HashMap<Int, Boolean>
    ): PlaylistContentModifAction()
}