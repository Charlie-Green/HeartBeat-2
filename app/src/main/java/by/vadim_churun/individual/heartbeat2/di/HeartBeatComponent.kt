package by.vadim_churun.individual.heartbeat2.di

import by.vadim_churun.individual.heartbeat2.di.module.MediaServiceModule
import by.vadim_churun.individual.heartbeat2.service.HeartBeatMediaService
import dagger.Component


@Component(
    modules = [MediaServiceModule::class]
)
interface HeartBeatComponent {
    fun inject(service: HeartBeatMediaService)
}