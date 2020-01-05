package by.vadim_churun.individual.heartbeat2.ui.main.song

import by.vadim_churun.individual.heartbeat2.presenter.song.SongsCollectionAction
import io.reactivex.subjects.PublishSubject


internal object SongsCollectionSubjects {
    val PLAY         = PublishSubject.create<SongsCollectionAction.Play>()
    val SET_PRIORITY = PublishSubject.create<SongsCollectionAction.SetPriority>()
    val DECODE_ART   = PublishSubject.create<SongsCollectionAction.DecodeArt>()
}