package by.vadim_churun.individual.heartbeat2.app.model.state

import android.graphics.Bitmap
import by.vadim_churun.individual.heartbeat2.app.model.obj.PlaylistsCollection


/** MVI States for collection of playlists. **/
sealed class PlaylistsCollectionState {
    object Preparing: PlaylistsCollectionState()
    class Prepared(val collection: PlaylistsCollection): PlaylistsCollectionState()
    class ArtDecoded(val plistID: Int, val art: Bitmap): PlaylistsCollectionState()
    class ArtDecodeFailed(val plistID: Int): PlaylistsCollectionState()
}