package by.vadim_churun.individual.heartbeat2.app.di.module

import android.content.Context
import by.vadim_churun.individual.heartbeat2.app.player.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/** The module which provides [HeartBeatExoPlayer]
  * as the implementation of [HeartBeatPlayer]. **/
@Module
class ExoPlayerProvidingModule(private val appContext: Context) {
    @Provides
    @Singleton
    fun providePlayer(): HeartBeatPlayer
        = HeartBeatExoPlayer(appContext)
}