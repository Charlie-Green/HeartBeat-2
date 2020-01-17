package by.vadim_churun.individual.heartbeat2.app.di

import by.vadim_churun.individual.heartbeat2.app.di.module.CommonProvidingModule
import by.vadim_churun.individual.heartbeat2.app.di.module.ExoPlayerProvidingModule
import by.vadim_churun.individual.heartbeat2.app.service.HeartBeatMediaService
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        CommonProvidingModule::class,
        ExoPlayerProvidingModule::class
    ]
)
interface HeartBeatComponent {
    fun inject(service: HeartBeatMediaService)
}