package by.vadim_churun.individual.heartbeat2.model.state

import by.vadim_churun.individual.heartbeat2.entity.Song
import by.vadim_churun.individual.heartbeat2.entity.SongWithSettings
import by.vadim_churun.individual.heartbeat2.entity.SongsOrder


/** MVI States for currently playing media. **/
sealed class MediaState {
    class Preparing(
        val song: Song
    ): MediaState()

    class Playing(
        val song: SongWithSettings,
        val position: Long,
        val duration: Long,
        val order: SongsOrder
    ): MediaState()

    class Paused(
        val song: SongWithSettings,
        val position: Long,
        val duration: Long,
        val order: SongsOrder
    ): MediaState()

    class Stopped(
        val lastSong: SongWithSettings?,
        val lastDuration: Long?,
        val order: SongsOrder
    ): MediaState()

    class PlayFailed(
        val lastSong: SongWithSettings?,
        val lastDuration: Long?,
        val requestedSong: Song,
        val order: SongsOrder
    ): MediaState() {
        var consumed = false
    }
}