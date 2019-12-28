package by.vadim_churun.individual.heartbeat2.presenter.control

import io.reactivex.Observable


interface MediaControlUI {
    // TODO: Add a renderState method
    fun stopIntent():            Observable<MediaControlAction.Stop>
    fun replayIntent():          Observable<MediaControlAction.Replay>
    fun seekIntent():            Observable<MediaControlAction.Seek>
    fun setRateIntent():         Observable<MediaControlAction.SetRate>
    fun setVolumeIntent():       Observable<MediaControlAction.SetVolume>
    fun playPauseIntent():       Observable<MediaControlAction.PlayPause>
    fun requestNextIntent():     Observable<MediaControlAction.RequestNext>
    fun setPriorityIntent():     Observable<MediaControlAction.SetPriority>
    fun setSongsOrderIntent():   Observable<MediaControlAction.SetSongsOrder>
    fun requestPreviousIntent(): Observable<MediaControlAction.RequestPrevious>
}