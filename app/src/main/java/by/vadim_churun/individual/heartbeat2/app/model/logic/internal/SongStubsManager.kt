package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import android.content.Context
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.model.obj.SongStub
import by.vadim_churun.individual.heartbeat2.shared.Song
import javax.inject.Inject


/** Responsible for mapping [Song]s to [SongStub]s
  * and saving user settings for the rule for this mapping. **/
class SongStubsManager @Inject constructor(private val appContext: Context) {
    fun stubFrom(song: Song)
    // TODO: Provide a more complex implementation here.
        = SongStub(
            song.title,
            song.artist ?: appContext.getString(R.string.unknown_artist)
        )
}