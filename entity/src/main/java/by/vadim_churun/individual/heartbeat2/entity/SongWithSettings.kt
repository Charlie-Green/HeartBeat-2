package by.vadim_churun.individual.heartbeat2.entity


/** Represents a song for which settings are applicable.
  * These may be settings in a playlist or the default settings. **/
abstract class SongWithSettings(
    title: String,
    artist: String?,
    filename: String?,
    contentUri: String,
    val rate: Float,
    val volume: Float,
    val priority: Byte
): Song(title, artist, filename, contentUri)
