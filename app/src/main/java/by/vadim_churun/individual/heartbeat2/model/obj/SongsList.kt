package by.vadim_churun.individual.heartbeat2.model.obj

import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings


class SongsList private constructor(
    private val entries: List<Entry>,      // Position -> Entry
    private val idToPositionMap: HashMap<Int, Int>   // Song ID -> Position
) {
    class Entry(
        val song: by.vadim_churun.individual.heartbeat2.shared.SongWithSettings,
        val stub: SongStub
    )


    companion object {
        fun from
        (songs: List<by.vadim_churun.individual.heartbeat2.shared.SongWithSettings>, stubFor: (song: by.vadim_churun.individual.heartbeat2.shared.SongWithSettings) -> SongStub): SongsList {
            val entries = songs.map { song ->
                Entry(
                    song,
                    stubFor(song)
                )
            }.sortedBy { it.stub.displayTitle }

            val idToPositionMap = HashMap<Int, Int>(entries.size)
            for(index in entries.indices) {
                idToPositionMap[entries[index].song.ID] = index
            }

            return SongsList(
                entries,
                idToPositionMap
            )
        }
    }


    val size: Int
        get() = entries.size
    operator fun get(position: Int): Entry
        = entries[position]
    fun indexOf(songID: Int): Int?
        = idToPositionMap[songID]
}