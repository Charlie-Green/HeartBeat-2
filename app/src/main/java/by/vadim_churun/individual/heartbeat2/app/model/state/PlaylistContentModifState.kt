package by.vadim_churun.individual.heartbeat2.app.model.state


/** MVI states for a process of playlist content modification. **/
sealed class PlaylistContentModifState {
    object Processing: PlaylistContentModifState()
    object Updated: PlaylistContentModifState()
}