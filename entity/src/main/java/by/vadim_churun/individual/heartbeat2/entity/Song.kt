package by.vadim_churun.individual.heartbeat2.entity


/** Represents a song by itself (not in playlist or any other context).
  * Song's duration is not a part of this class because it is determined dynamically
  * when the song gets played.**/
abstract class Song(
    val title: String,
    val artist: String?,
    val filename: String?,
    val contentUri: String
)