package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import android.content.Context
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import by.vadim_churun.individual.heartbeat2.storage.ExternalStorageSongsSource
import javax.inject.Inject


/** This class is responsible to keep the list of [SongsSource]s available and the app
  * and manage interaction with them. **/
class SongsSourcesManager @Inject constructor(
    private val appContext: Context,
    sources: List<@JvmSuppressWildcards SongsSource>
) {
    private fun throwUnknownSource(unknownClass: Class<out SongsSource>): Nothing
        = throw Exception(
            "Unknown ${SongsSource::class.java.simpleName}: ${unknownClass.simpleName}" )

    /** Wraps one [SongsSource]. Provides additional information about this source  **/
    inner class SongsSourceMeta constructor(
        val source: SongsSource
    ) {
        val name by lazy {
            when(source) {
                is ExternalStorageSongsSource -> R.string.songs_source_external_storage
                else -> throwUnknownSource(source::class.java)
            }.let { appContext.getString(it) }
        }
    }

    private val metas = sources.map { source ->
        SongsSourceMeta(source)
    }

    fun forEachSource(action: (SongsSourceMeta) -> Unit)
        = metas.forEach { action(it) }

    fun metaFor(klass: Class<out SongsSource>): SongsSourceMeta {
        for(meta in metas) {
            if(klass.isInstance(meta.source))
                return meta
        }
        throwUnknownSource(klass)
    }
}