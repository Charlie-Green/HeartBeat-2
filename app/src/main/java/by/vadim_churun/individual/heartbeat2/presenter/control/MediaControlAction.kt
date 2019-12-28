package by.vadim_churun.individual.heartbeat2.presenter.control

import by.vadim_churun.individual.heartbeat2.media.SongsOrder


sealed class MediaControlAction {
    object PlayPause: MediaControlAction()
    object Stop: MediaControlAction()
    object Replay: MediaControlAction()
    object RequestNext: MediaControlAction()
    object RequestPrevious: MediaControlAction()
    class Seek(position: Long): MediaControlAction()
    class SetRate(rate: Float): MediaControlAction()
    class SetVolume(volume: Float): MediaControlAction()
    class SetPriority(priority: Byte): MediaControlAction()
    class SetSongsOrder(order: SongsOrder): MediaControlAction()
}