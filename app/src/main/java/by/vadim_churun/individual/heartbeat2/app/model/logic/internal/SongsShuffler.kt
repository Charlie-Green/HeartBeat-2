package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import by.vadim_churun.individual.heartbeat2.app.model.obj.SongsList
import java.util.*
import javax.inject.Inject


/** Able to peek a random song from a preset list.
  * This class is not thread-safe. **/
class SongsShuffler @Inject constructor() {
    private lateinit var songs: SongsList
    private val random = Random()
    private val indexMap = mutableListOf<Int>()

    private fun assertPrepared() {
        if(indexMap.isEmpty())
            throw IllegalStateException("${this.javaClass.simpleName} hasn't been prepared")
    }

    /** Sets this [SongsShuffler] to peek songs from this list.
      * May be time-consuming. **/
    fun prepare(source: SongsList) {
        if(source.size == 0)
            throw IllegalArgumentException("Cannot shuffle an empty list")

        songs = source
        indexMap.clear()
        for(songIndex in 0 until source.size) {
            val prio = source[songIndex].song.priority
            for(j in 1..prio) {
                indexMap.add(songIndex)
            }
        }
    }


    /** Peeks a random song index from the last [prepare]d source.
      * This operation is quick. **/
    val nextIndex: Int
        get() {
            assertPrepared()
            val randomIndex = random.nextInt(indexMap.size)
            return indexMap[randomIndex]
        }
}