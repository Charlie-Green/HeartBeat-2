package by.vadim_churun.individual.heartbeat2

import android.app.Application
import by.vadim_churun.individual.heartbeat2.di.*
import by.vadim_churun.individual.heartbeat2.di.module.*


/** A custom [Application] class is used for dependency injection. **/
class HeartBeatApplication: Application() {
    lateinit var diComponent: HeartBeatComponent

    private fun daggerItOut() {
        diComponent = DaggerHeartBeatComponent
            .builder()
            .commonProvidingModule( CommonProvidingModule(super.getApplicationContext()) )
            .exoPlayerProvidingModule( ExoPlayerProvidingModule(super.getApplicationContext()) )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        daggerItOut()
    }
}