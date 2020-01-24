package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import by.vadim_churun.individual.heartbeat2.app.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.app.db.view.SongInPlaylistView
import by.vadim_churun.individual.heartbeat2.shared.Song
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
            view.song.sourceCode,
            view.plistItem.rate,
            view.plistItem.volume,
            view.plistItem.priority
        )

    fun songWithSettings(
        entity: SongEntity,
        rate: Float = 1f,
        volume: Float = 1f,
        priority: Byte = 3 )
        = SongWithSettings(
            entity.ID,
            entity.title,
            entity.artist,
            entity.duration,
            entity.filename,
            entity.contentUri,
            entity.sourceCode,
            rate,
            volume,
            priority
        )

    fun withNewSettings
    (song: Song, newRate: Float, newVolume: Float, newPriority: Byte)
        = SongWithSettings(
            song.ID,
            song.title,
            song.artist,
            song.duration,
            song.filename,
            song.contentUri,
            song.sourceCode,
            newRate,
            newVolume,
            newPriority
        )
}