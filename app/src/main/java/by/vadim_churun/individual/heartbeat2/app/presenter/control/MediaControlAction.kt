package by.vadim_churun.individual.heartbeat2.app.presenter.control


/** Actions that [MediaControlUI] may request [MediaControlPresenter] to perform. **/
sealed class MediaControlAction {
    object PlayPause: MediaControlAction()
    object Stop: MediaControlAction()
    object Replay: MediaControlAction()
    object RequestNext: MediaControlAction()
    object RequestPrevious: MediaControlAction()
    class Seek(val position: Long): MediaControlAction()
    class SetRate(val rate: Float): MediaControlAction()
    class SetVolume(val volume: Float): MediaControlAction()
    class SetPriority(val priority: Byte): MediaControlAction()
    class SetSongsOrder(val order: by.vadim_churun.individual.heartbeat2.shared.SongsOrder): MediaControlAction()
}