package by.vadim_churun.individual.heartbeat2.model.logic

import by.vadim_churun.individual.heartbeat2.shared.Song
import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


/** One of the top-level classes of MVI's "model" layer,
  * the one which is for a collection of songs. **/
class SongsRepository @Inject constructor(
    private val sources: List<@JvmSuppressWildcards SongsSource>
) {
    fun observableSongs()
        = Observable.create< List<Song> > { emitter ->
            val songs = mutableListOf<Song>()
            for(source in sources) {
                songs.addAll(source.fetch())
            }
            emitter.onNext(songs)
        }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}