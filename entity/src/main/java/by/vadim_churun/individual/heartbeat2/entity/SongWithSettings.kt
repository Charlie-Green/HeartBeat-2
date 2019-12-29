package by.vadim_churun.individual.heartbeat2.entity


/** Represents a song for which settings are applicable.
  * These may be settings in a playlist or the default settings. **/
abstract class SongWithSettings(
    ID: Int,
    title: String,
    artist: String?,
    duration: Long,
    filename: String?,
    contentUri: String,
    val rate: Float,
    val volume: Float,
    val priority: Byte
): Song(ID, title, artist, duration, filename, contentUri)