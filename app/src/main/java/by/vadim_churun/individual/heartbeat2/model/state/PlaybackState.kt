package by.vadim_churun.individual.heartbeat2.model.state

import by.vadim_churun.individual.heartbeat2.model.obj.SongStub
import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings
import by.vadim_churun.individual.heartbeat2.shared.SongsOrder


/** MVI States for currently playing media. **/
sealed class PlaybackState {
    class Preparing(
        val stub: SongStub
    ): PlaybackState()

    class Playing(
        val song: SongWithSettings,
        val stub: SongStub,
        val position: Long,
        val order: SongsOrder
    ): PlaybackState()

    class Paused(
        val song: SongWithSettings,
        val stub: SongStub,
        val position: Long,
        val order: SongsOrder
    ): PlaybackState()

    class Stopped(
        val lastSong: SongWithSettings?,
        val lastStub: SongStub?,
        val order: SongsOrder
    ): PlaybackState()

    class PlayFailed(
        val lastSong: SongWithSettings?,
        val lastStub: SongStub?,
        val requestedSongStub: SongStub,
        val order: SongsOrder
    ): PlaybackState() {
        var consumed = false
    }
}