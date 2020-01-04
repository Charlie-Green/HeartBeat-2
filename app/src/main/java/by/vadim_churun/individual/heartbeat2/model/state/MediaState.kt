package by.vadim_churun.individual.heartbeat2.model.state

import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings
import by.vadim_churun.individual.heartbeat2.shared.SongsOrder
import by.vadim_churun.individual.heartbeat2.model.obj.SongStub


/** MVI States for currently playing media. **/
sealed class MediaState {
    class Preparing(
        val stub: SongStub
    ): MediaState()

    class Playing(
        val song: by.vadim_churun.individual.heartbeat2.shared.SongWithSettings,
        val stub: SongStub,
        val position: Long,
        val order: by.vadim_churun.individual.heartbeat2.shared.SongsOrder
    ): MediaState()

    class Paused(
        val song: by.vadim_churun.individual.heartbeat2.shared.SongWithSettings,
        val stub: SongStub,
        val position: Long,
        val order: by.vadim_churun.individual.heartbeat2.shared.SongsOrder
    ): MediaState()

    class Stopped(
        val lastSong: by.vadim_churun.individual.heartbeat2.shared.SongWithSettings?,
        val lastStub: SongStub?,
        val order: by.vadim_churun.individual.heartbeat2.shared.SongsOrder
    ): MediaState()

    class PlayFailed(
        val lastSong: by.vadim_churun.individual.heartbeat2.shared.SongWithSettings?,
        val lastStub: SongStub?,
        val requestedSongStub: SongStub,
        val order: by.vadim_churun.individual.heartbeat2.shared.SongsOrder
    ): MediaState() {
        var consumed = false
    }
}