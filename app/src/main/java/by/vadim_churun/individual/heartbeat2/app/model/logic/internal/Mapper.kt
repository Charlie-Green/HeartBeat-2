package by.vadim_churun.individual.heartbeat2.app.model.logic.internal

import by.vadim_churun.individual.heartbeat2.app.db.entity.PlaylistEntity
import by.vadim_churun.individual.heartbeat2.app.db.entity.SongEntity
import by.vadim_churun.individual.heartbeat2.app.db.view.SongInPlaylistView
import by.vadim_churun.individual.heartbeat2.app.model.obj.Playlist
import by.vadim_churun.individual.heartbeat2.app.model.obj.SongsList
import by.vadim_churun.individual.heartbeat2.shared.Song
import by.vadim_churun.individual.heartbeat2.shared.SongWithSettings
import javax.inject.Inject


/** This class implements mappings between database entities and views and model entities. **/
class Mapper @Inject constructor(
    private val stubMan: SongStubsManager
) {
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

    fun songWithSettings(view: SongInPlaylistView)
        = songWithSettings(
            view.song,
            view.plistItem.rate,
            view.plistItem.volume,
            view.plistItem.priority
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


    fun playlist(entity: PlaylistEntity)
        = Playlist(entity.ID, entity.title, entity.artUri)

    fun playlistEntity(title: String)
        = PlaylistEntity(0, title, null)

    fun playlistEntity(playlist: Playlist)
        = PlaylistEntity(playlist.ID, playlist.title, playlist.artUri)


    fun songsList(songs: List<SongWithSettings>)
        = SongsList.from(songs) { song ->
            stubMan.stubFrom(song)
        }

    fun songsWithSettingsFromEntities(entities: List<SongEntity>)
        = entities.map { entity ->
            songWithSettings(entity)
        }

    fun songsWithSettingsFromViews(views: List<SongInPlaylistView>)
        = views.map { view ->
            songWithSettings(view)
        }
}