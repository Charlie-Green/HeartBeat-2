package by.vadim_churun.individual.heartbeat2.app.ui.main.song

import by.vadim_churun.individual.heartbeat2.app.presenter.song.PlaylistContentModifAction
import by.vadim_churun.individual.heartbeat2.app.presenter.song.SongsCollectionAction
import io.reactivex.subjects.PublishSubject


internal object SongsCollectionSubjects {
    val PLAY         = PublishSubject.create<SongsCollectionAction.Play>()
    val SET_PRIORITY = PublishSubject.create<SongsCollectionAction.SetPriority>()
    val DECODE_ART   = PublishSubject.create<SongsCollectionAction.DecodeArt>()
    val SUBMIT_PERMISSION_RESULT
        = PublishSubject.create<SongsCollectionAction.SubmitPermissionsResult>()
    val UPDATE_PLAYLIST_CONTENT
        = PublishSubject.create<PlaylistContentModifAction.UpdateContent>()
}