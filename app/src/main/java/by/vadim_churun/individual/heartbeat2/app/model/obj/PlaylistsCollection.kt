package by.vadim_churun.individual.heartbeat2.app.model.obj

import android.graphics.Bitmap


class PlaylistsCollection private constructor(
    val headers: List<PlaylistHeader>,
    val idMap: HashMap<Int, Int>
) {
    private val arts = MutableList<Bitmap?>(headers.size) { null }

    val size
        get() = headers.size

    operator fun get(index: Int)
        = headers[index]

    fun indexOf(playlistID: Int)
        = idMap[playlistID]

    fun artAt(index: Int)
        = arts[index]

    fun setArtAt(index: Int, art: Bitmap) {
        arts[index] = art
    }


    companion object {
        fun from(headers: List<PlaylistHeader>): PlaylistsCollection {
            val idMap = HashMap<Int, Int>()
            for(j in headers.indices) {
                idMap[headers[j].ID] = j
            }
            return PlaylistsCollection(headers, idMap)
        }
    }
}