package by.vadim_churun.individual.heartbeat2.model.logic

import android.content.Context
import by.vadim_churun.individual.heartbeat2.R
import by.vadim_churun.individual.heartbeat2.model.obj.SongStub
import by.vadim_churun.individual.heartbeat2.model.obj.SongsList
import by.vadim_churun.individual.heartbeat2.model.state.SongsCollectionState
import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings
import javax.inject.Inject


/** Maps different "raw" objects to appropriate MVI states. **/
class StatesManager @Inject constructor(private val appContext: Context) {
    fun songsCollectionPrepared
    (collection: List<SongWithSettings>): SongsCollectionState.CollectionPrepared {
        val songsList = SongsList.from(collection) { song ->
            // TODO: Provide a more complex implementation here.
            SongStub(
                song.title,
                song.artist ?: appContext.getString(R.string.unknown_artist)
            )
        }
        return SongsCollectionState.CollectionPrepared(songsList)
    }
}