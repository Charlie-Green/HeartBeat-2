package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import by.vadim_churun.individual.heartbeat2.app.model.obj.PlaylistHeader
import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import javax.inject.Inject


class SearchManager @Inject constructor(
    private val songsSearcher: Searcher<SongWithSettings>,
    private val plistsSearcher: Searcher<PlaylistHeader>
) {
    //////////////////////////////////////////////////////////////////////////////////////////
    // INTERNAL:

    private var futureSongs:  Future<Unit>? = null
    private var futurePlists: Future<Unit>? = null

    private val CharSequence.tokens
        get() = this.split(' ').filter { it.isNotEmpty() }

    private fun <ItemType> prepareAsync(
        oldFuture: Future<Unit>?,
        searcher: Searcher<ItemType>,
        dataSource: List<ItemType>,
        tokensForItem: (ItemType) -> List<String>
    ): Future<Unit> {
        oldFuture?.cancel(true)
        return FutureTask {
            searcher.prepare(dataSource, tokensForItem)
        }.apply { run() }
    }

    private fun <ItemType> search(
        future: Future<Unit>?,
        searcher: Searcher<ItemType>,
        searchQuery: CharSequence
    ): List<ItemType> {
        future ?: throw IllegalStateException("Data source hasn't been prepared")
        future.get()  // Wait for the searcher's prepare() method to finish.
        return searcher.search(searchQuery)
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // API:

    fun prepareSongsAsync(songs: List<SongWithSettings>) {
        futureSongs = prepareAsync(futureSongs, songsSearcher, songs) { song ->
            val tokens = mutableListOf<String>()
            tokens.addAll(song.title.tokens)
            song.artist?.also { tokens.addAll(it.tokens) }
            song.filename?.also { tokens.addAll(it.tokens) }
            tokens
        }
    }

    fun searchSongs(query: CharSequence)
        = search(futureSongs, songsSearcher, query)



    fun preparePlaylistsAsync(playlists: List<PlaylistHeader>) {
        futurePlists = prepareAsync(futurePlists, plistsSearcher, playlists) { plist ->
            plist.title.tokens
        }
    }

    fun searchPlaylists(query: CharSequence)
        = search(futurePlists, plistsSearcher, query)


    fun dispose() {
        futureSongs?.cancel(true);  futureSongs = null
        futurePlists?.cancel(true); futurePlists = null
    }
}