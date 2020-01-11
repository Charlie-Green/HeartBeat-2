package by.vadim_churun.individual.heartbeat2.model.logic.internal

import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings
import by.vadim_churun.individual.heartbeat2.shared.SongsOrder
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future


/** Keeps current collection of songs, and defines which song is the "preivous"
  * and which one is the "next" according to current [SongsOrder]. **/
class SongsCollectionManager {
    /////////////////////////////////////////////////////////////////////////////////////////
    // ASYNC:

    private var preparator = Executors.newSingleThreadExecutor()
    private var prepareFuture: Future<*>? = null

    private fun assertNotReleased() {
        if(preparator.isShutdown)
            throw IllegalStateException("${this.javaClass.simpleName} has been released")
    }

    private fun prepareShuffler(collection: List<SongWithSettings>) {
        assertNotReleased()
        prepareFuture = preparator.submit {
            if(android.os.Looper.myLooper() == android.os.Looper.getMainLooper())
                throw Exception("Preparing on main thread")
            android.util.Log.v("HbSongsCollection", "Preparing")
            TODO()
        }
    }

    private fun waitShufflerPrepared() {
        assertNotReleased()
        prepareFuture?.get()
    }

    fun release() {
        preparator.shutdown()
        prepareFuture = null
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // HISTORY:

    private class HistoryEntry(
        val song: SongWithSettings,
        val index: Int
    )

    private val history = Stack<HistoryEntry>()


    /////////////////////////////////////////////////////////////////////////////////////////
    // API:

    private var mCollection: List<SongWithSettings>? = null
    var collection: List<SongWithSettings>?
        get() = mCollection
        set(value) {
            val newCollection = value?.let { if(it.isEmpty()) null else it }
            newCollection?.also { prepareShuffler(it) }
            mCollection = newCollection
            history.clear()
        }

    private var mOrder = SongsOrder.SEQUENTIAL
    var order: SongsOrder
        get() = mOrder
        set(value) {
            mOrder = value
        }



    fun previous(): SongWithSettings? {
        if(history.size < 2) {
            val songs = mCollection
            if(songs == null || songs.size < 2 || history.empty())
                return null  // Nothing we can do here.

            // Return the previous song in the list.
            val curIndex = history.pop().index
            val newIndex = curIndex.dec() % songs.size
            return songs[newIndex].also {
                history.push( HistoryEntry(it, newIndex) )
            }
        }

        // Return the previous song in the history.
        history.pop()  // Pop the current song.
        return history.peek().song
    }

    fun next(): SongWithSettings? {
        val songs = mCollection ?: return null
        return when(mOrder) {
            SongsOrder.SEQUENTIAL -> {
                val curIndex = if(history.empty()) -1 else history.peek().index
                val newIndex = curIndex.inc() % songs.size
                HistoryEntry(songs[newIndex], newIndex)
            }

            SongsOrder.LOOP -> {
                if(history.empty())
                    HistoryEntry(songs[0], 0)
                history.pop()
            }

            SongsOrder.SHUFFLE -> {
                waitShufflerPrepared()
                TODO()
            }

            else -> throw IllegalArgumentException(
                "Unknown ${mOrder.javaClass.simpleName}: ${mOrder.name}" )
        }?.also {
            history.push(it)
        }?.song
    }
}