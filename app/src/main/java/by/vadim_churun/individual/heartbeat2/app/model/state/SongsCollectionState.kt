package by.vadim_churun.individual.heartbeat2.app.model.state

import android.graphics.Bitmap
import by.vadim_churun.individual.heartbeat2.app.model.obj.SongsList


/** MVI states for a list of songs. **/
sealed class SongsCollectionState {
    object Preparing: SongsCollectionState()
    class CollectionPrepared(
        /** The songs to be displayed now, either all songs or from a particular playlist. **/
        val songs: SongsList,

        /** All the songs. If null, than [songs] refer to the full list of songs. **/
        val allSongs: SongsList?,

        /** ID of the playlist these songs come from.
          * If [allSongs] is null, this ID is invalid. **/
        val playlistID: Int
    ): SongsCollectionState()
    class ArtDecoded(val songID: Int, val art: Bitmap): SongsCollectionState()
    class ArtDecodeFailed(
        val songID: Int
    ): SongsCollectionState() { var consumed = false }
}