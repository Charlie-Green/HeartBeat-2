package by.vadim_churun.individual.heartbeat2

import android.app.Application
import by.vadim_churun.individual.heartbeat2.di.DaggerHeartBeatComponent
import by.vadim_churun.individual.heartbeat2.di.HeartBeatComponent
import by.vadim_churun.individual.heartbeat2.di.module.MediaServiceModule


/** A custom Application class is used for dependency injection. **/
class HeartBeatApplication: Application() {
    lateinit var diComponent: HeartBeatComponent

    private fun daggerItOut() {
        diComponent = DaggerHeartBeatComponent.builder()
            .mediaServiceModule( MediaServiceModule(super.getApplicationContext()) )
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        daggerItOut()
    }
}