package by.vadim_churun.individual.heartbeat2.model.logic.internal

import by.vadim_churun.individual.heartbeat2.model.obj.SongsList
import by.vadim_churun.individual.heartbeat2.shared.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton


/** Keeps current collection of songs, and defines which song is the "previous"
  * and which one is the "next" according to current [SongsOrder]. **/
@Singleton
class SongsCollectionManager @Inject constructor(
    private val shuffler: SongsShuffler ) {
    /////////////////////////////////////////////////////////////////////////////////////////
    // ASYNC:

    private var preparator = Executors.newSingleThreadExecutor()
    private var prepareFuture: Future<*>? = null

    private fun assertNotReleased() {
        if(preparator.isShutdown)
            throw IllegalStateException("${this.javaClass.simpleName} has been released")
    }

    private fun prepareShuffler(collection: SongsList) {
        assertNotReleased()
        prepareFuture = preparator.submit {
            shuffler.prepare(collection)
        }
    }

    private fun waitShufflerPrepared() {
        assertNotReleased()
        prepareFuture?.get()
    }

    fun dispose() {
        preparator.shutdown()
        prepareFuture = null
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // API:

    private val history = Stack<SongWithSettings>()

    private var mCollection: SongsList? = null
    var collection: SongsList?
        get() = mCollection
        set(value) {
            val newCollection = value?.let { if(it.size == 0) null else it }
            newCollection?.also { prepareShuffler(it) }
            mCollection = newCollection
        }

    private var mOrder = SongsOrder.SEQUENTIAL
    var order: SongsOrder
        get() = mOrder
        set(value) {
            mOrder = value
        }


    val previous: SongWithSettings?
        get() {
            if(history.size < 2) {
                val songs = mCollection
                if(songs == null || songs.size < 2 || history.empty())
                    return null

                val curIndex = songs.indexOf(history.pop().ID) ?: return null
                val newIndex =
                    if(curIndex == 0) songs.size-1 else curIndex.dec() % songs.size
                return songs[newIndex].song.also { history.push(it) }
            }

            // Return the previous song in the history.
            history.pop()  // Pop the current song.
            return history.peek()
    }

    val next: SongWithSettings?
        get() {
            val songs = mCollection ?: return null
            return when(mOrder) {
                SongsOrder.SEQUENTIAL, SongsOrder.LOOP -> {
                    val curIndex =
                        if(history.empty()) -1
                        else songs.indexOf(history.peek().ID) ?: -1
                    val newIndex = curIndex.inc() % songs.size
                    songs[newIndex].song
                }

                SongsOrder.SHUFFLE -> {
                    waitShufflerPrepared()
                    val nextIndex = shuffler.nextIndex
                    songs[nextIndex].song
                }

                else -> throw IllegalArgumentException(
                    "Unknown ${mOrder.javaClass.simpleName}: ${mOrder.name}" )
            }.also { history.push(it) }
    }
}