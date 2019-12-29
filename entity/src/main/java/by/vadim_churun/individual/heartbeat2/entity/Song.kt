package by.vadim_churun.individual.heartbeat2.entity


/** Represents a song by itself (not in playlist or any other context). **/
abstract class Song(
    val ID: Int,
    val title: String,
    val artist: String?,
    val duration: Long,
    val filename: String?,
    val contentUri: String
)