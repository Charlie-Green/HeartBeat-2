package by.vadim_churun.individual.heartbeat2.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class MediaServiceModule(private val appContext: Context) {
    @Provides
    fun provideAppContext(): Context
        = appContext
}