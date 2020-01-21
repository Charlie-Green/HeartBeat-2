package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import by.vadim_churun.individual.heartbeat2.app.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.app.db.view.SongInPlaylistView
import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings
import javax.inject.Inject


/** This class implements mappings between database entities and views and model entities. **/
class Mapper @Inject constructor() {
    fun songWithSettings(view: SongInPlaylistView)
        = SongWithSettings(
            view.song.ID,
            view.song.title,
            view.song.artist,
            view.song.duration,
            view.song.filename,
            view.song.contentUri,
            view.song.sourceClass,
            view.plistItem.rate,
            view.plistItem.volume,
            view.plistItem.priority
        )

    /** Important: [SongEntity] by itself doesn't have settings,
      * so the default settings are applied. **/
    fun songWithSettings(entity: SongEntity)
        = SongWithSettings(
            entity.ID,
            entity.title,
            entity.artist,
            entity.duration,
            entity.filename,
            entity.contentUri,
            entity.sourceClass,
            /* rate:     */ 1f,
            /* volume:   */ 1f,
            /* priority: */ 3
        )
}