package by.vadim_churun.individual.heartbeat2.app.model.state

import android.graphics.Bitmap
import by.vadim_churun.individual.heartbeat2.app.model.obj.SongsList


/** MVI states for a list of songs. **/
sealed class SongsCollectionState {
    class CollectionPrepared(val songs: SongsList): SongsCollectionState()
    class ArtDecoded(val songID: Int, val art: Bitmap): SongsCollectionState()
    class ArtDecodeFailed(val songID: Int): SongsCollectionState()
}