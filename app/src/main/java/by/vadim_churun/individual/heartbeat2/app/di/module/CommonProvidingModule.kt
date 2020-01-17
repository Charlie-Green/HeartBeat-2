package by.vadim_churun.individual.heartbeat2.app.di.module

import android.content.Context
import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import by.vadim_churun.individual.heartbeat2.storage.ExternalStorageSongsSource
import dagger.Module
import dagger.Provides


/** Provides dependencies which remain the same for the app
 * whatever implementations of particular interfaces are chosen. **/
@Module
class CommonProvidingModule(private val appContext: Context) {
    @Provides
    fun provideAppContext(): Context
        = appContext

    @Provides
    fun provideSongsSources(): List<@JvmSuppressWildcards SongsSource> {
        val sources = mutableListOf<SongsSource>() //mutableListOf<SongsSource>()
        sources.add( ExternalStorageSongsSource(appContext.contentResolver) )
        // TODO: Add other sources if there are such.
        return sources
    }
}