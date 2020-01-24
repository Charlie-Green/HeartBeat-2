package by.vadim_churun.individual.heartbeat2.shared


/** Represents a song for which settings are applicable.
 * These may be settings in a playlist or the default settings. **/
open class SongWithSettings(
    ID: Int,
    title: String,
    artist: String?,
    duration: Long,
    filename: String?,
    contentUri: String,
    sourceCode: Byte,
    val rate: Float,
    val volume: Float,
    val priority: Byte
): Song(ID, title, artist, duration, filename, contentUri, sourceCode)