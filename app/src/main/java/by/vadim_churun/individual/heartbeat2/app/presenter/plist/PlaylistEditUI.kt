package by.vadim_churun.individual.heartbeat2.app.presenter.plist

import io.reactivex.Observable


interface PlaylistEditUI {
    fun intentAdd(): Observable<PlaylistEditAction.Add>
    fun intentUpdate(): Observable<PlaylistEditAction.Update>
}