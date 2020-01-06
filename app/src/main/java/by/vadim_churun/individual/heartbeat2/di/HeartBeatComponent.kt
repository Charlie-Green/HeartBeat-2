package by.vadim_churun.individual.heartbeat2.di

import by.vadim_churun.individual.heartbeat2.di.module.CommonProvidingModule
import by.vadim_churun.individual.heartbeat2.di.module.ExoPlayerProvidingModule
import by.vadim_churun.individual.heartbeat2.service.HeartBeatMediaService
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