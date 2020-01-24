package by.vadim_churun.individual.heartbeat2.shared

import android.graphics.Bitmap


/** An interface for any class which can provide a list of songs from some source. **/
interface SongsSource {
    /** Integer identifier for this source.
      * Any number, just needs to be unique among all [SongsSource]s. **/
    val ID: Byte

    /** User-readable name for this source. **/
    val name: String

    /** How often synchronization with this source should occur, in seconds.
      * The time taken by sync itself should not be encountered. **/
    val recommendedSyncPeriod: Int

    /** The full list of Android permissions needed by this source to work. **/
    val permissions: List<String>

    /** Performs the actual work of retrieving songs. No threading should be handled. **/
    fun fetch(): List<Song>

    /** Retrieves art associated with the given song. No threading should be handled. **/
    fun artFor(song: Song): Bitmap?
}