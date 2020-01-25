package by.vadim_churun.individual.heartbeat2.app.model.state

import by.vadim_churun.individual.heartbeat2.app.model.obj.Playlist


/** MVI states for CRUD operations on playlists. **/
sealed class PlaylistEditState {
    object Processing: PlaylistEditState()
    class Added(val title: String): PlaylistEditState()
    class AddRefused(
        val title: String,
        val reason: AddRefused.Reason
    ): PlaylistEditState() {
        enum class Reason {
            TITLE_EXISTS
        }
    }
    class Updated(val newTitle: String): PlaylistEditState()
    class LastPlaylistAvailable(val oldPlaylist: Playlist): PlaylistEditState()
}