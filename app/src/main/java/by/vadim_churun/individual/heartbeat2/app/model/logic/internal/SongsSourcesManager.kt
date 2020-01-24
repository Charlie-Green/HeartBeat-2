package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import javax.inject.Inject


/** This class is responsible to keep the list of [SongsSource]s available and the app
  * and manage interaction with them. **/
class SongsSourcesManager @Inject constructor(
    private val sources: List<@JvmSuppressWildcards SongsSource>
) {
    private fun throwUnknownSource(sourceCode: Byte): Nothing
        = throw IllegalArgumentException(
            "Unknown ${SongsSource::class.java.simpleName} with code $sourceCode" )


    fun forEachSource(action: (SongsSource) -> Unit)
        = sources.forEach { action(it) }

    fun sourceByCode(code: Byte): SongsSource {
        for(source in sources) {
            if(source.ID == code)
                return source
        }
        throwUnknownSource(code)
    }
}