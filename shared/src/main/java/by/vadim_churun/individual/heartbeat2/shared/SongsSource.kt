package by.vadim_churun.individual.heartbeat2.shared

import android.graphics.Bitmap
import by.vadim_churun.individual.heartbeat2.shared.Song


/** An interface for any class which can provide a list of songs from some source. **/
interface SongsSource {
    /** The full list of Android permissions needed by this source to work. **/
    val permissions: List<String>

    /** Performs the actual work of retrieving songs. No threading should be handled. **/
    fun fetch(): List<Song>

    /** Retrieves art associated with the given song. No threading should be handled. **/
    fun artFor(song: Song): Bitmap?
}