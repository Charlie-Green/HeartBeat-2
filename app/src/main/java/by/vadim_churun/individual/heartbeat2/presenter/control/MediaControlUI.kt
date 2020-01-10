package by.vadim_churun.individual.heartbeat2.presenter.control

import by.vadim_churun.individual.heartbeat2.model.state.PlaybackState
import io.reactivex.Observable


interface MediaControlUI {
    fun stopIntent():            Observable<MediaControlAction.Stop>
    fun seekIntent():            Observable<MediaControlAction.Seek>
    fun replayIntent():          Observable<MediaControlAction.Replay>
    fun setRateIntent():         Observable<MediaControlAction.SetRate>
    fun setVolumeIntent():       Observable<MediaControlAction.SetVolume>
    fun playPauseIntent():       Observable<MediaControlAction.PlayPause>
    fun requestNextIntent():     Observable<MediaControlAction.RequestNext>
    fun setPriorityIntent():     Observable<MediaControlAction.SetPriority>
    fun setSongsOrderIntent():   Observable<MediaControlAction.SetSongsOrder>
    fun requestPreviousIntent(): Observable<MediaControlAction.RequestPrevious>
    fun render(state: PlaybackState)
}