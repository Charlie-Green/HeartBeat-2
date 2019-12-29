package by.vadim_churun.individual.heartbeat2.model.state

import by.vadim_churun.individual.heartbeat2.entity.SongWithSettings
import by.vadim_churun.individual.heartbeat2.entity.SongsOrder
import by.vadim_churun.individual.heartbeat2.model.obj.SongStub


/** MVI States for currently playing media. **/
sealed class MediaState {
    class Preparing(
        val stub: SongStub
    ): MediaState()

    class Playing(
        val song: SongWithSettings,
        val stub: SongStub,
        val position: Long,
        val order: SongsOrder
    ): MediaState()

    class Paused(
        val song: SongWithSettings,
        val stub: SongStub,
        val position: Long,
        val order: SongsOrder
    ): MediaState()

    class Stopped(
        val lastSong: SongWithSettings?,
        val lastStub: SongStub?,
        val order: SongsOrder
    ): MediaState()

    class PlayFailed(
        val lastSong: SongWithSettings?,
        val lastStub: SongStub?,
        val requestedSongStub: SongStub,
        val order: SongsOrder
    ): MediaState() {
        var consumed = false
    }
}