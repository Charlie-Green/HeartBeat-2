package by.vadim_churun.individual.heartbeat2.shared


/** Represents a song by itself (not in playlist or any other context). **/
open class Song(
    val ID: Int,
    val title: String,
    val artist: String?,
    val duration: Long,
    val filename: String?,
    val contentUri: String,
    val sourceClass: Class<out SongsSource>
)