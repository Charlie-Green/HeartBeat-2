package by.vadim_churun.individual.heartbeat2.storage

import by.vadim_churun.individual.heartbeat2.shared.Song
import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore


class ExternalStorageSongsSource(val resolver: ContentResolver):
    by.vadim_churun.individual.heartbeat2.shared.SongsSource {
    ////////////////////////////////////////////////////////////////////////////////////////
    // HELP:

    private var mMetaRetriever: MediaMetadataRetriever? = null
    private fun getMetaRetriever(songUri: Uri): MediaMetadataRetriever {
        val mmr = mMetaRetriever ?: MediaMetadataRetriever().also { mMetaRetriever = it }
        resolver.openFileDescriptor(songUri, /* For reading only */ "r")!!.use {
            mmr.setDataSource(it.fileDescriptor)
        }
        return mmr
    }

    private val COLUMN_NAME_ID     = MediaStore.Audio.Media._ID
    private val COLUMN_NAME_TITLE = MediaStore.Audio.Media.TITLE
    private val COLUMN_NAME_ARTIST = MediaStore.Audio.Media.ARTIST
    private val COLUMN_NAME_FILENAME = MediaStore.Audio.Media.DATA          // (1)
    private val COLUMN_NAME_IS_SONG = MediaStore.Audio.Media.IS_MUSIC
    // (1) Use of the deprecated field is forced by the requirement
    // to provide filename for any song it's possible for.

    private val Cursor.ID
        get() = getInt( getColumnIndex(COLUMN_NAME_ID) )
    private val Cursor.title
        get() = getString( getColumnIndex(COLUMN_NAME_TITLE) )
    private val Cursor.artist
        get() = getString( getColumnIndex(COLUMN_NAME_ARTIST) )
    private val Cursor.filename
        get() = getString( getColumnIndex(COLUMN_NAME_FILENAME) )

    private fun durationFor(songUri: Uri): Long {
        val mmr = getMetaRetriever(songUri)
        val strDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return strDuration.toLong()
    }

    private fun contentUriById(id: Int)
        = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong() )


    ////////////////////////////////////////////////////////////////////////////////////////
    // IMPLEMENTATION:

    override val permissions: List<String>
        get() = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun artFor(song: Song): Bitmap? {
        val contentUri = Uri.parse(song.contentUri)
        val mmr = getMetaRetriever(contentUri)
        val bytes = mmr.embeddedPicture ?: return null
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun fetch(): List<Song> {
        val songs = mutableListOf<Song>()

        resolver.query(
            /* uri */        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            /* projection */ arrayOf(
                COLUMN_NAME_ID,
                COLUMN_NAME_TITLE,
                COLUMN_NAME_ARTIST,
                COLUMN_NAME_FILENAME
            ),
            /* selection */  "${COLUMN_NAME_IS_SONG}=?",
            /* args */       arrayOf("1"),
            /* order */      null
        )!!.use { curs ->
            while(curs.moveToNext()) {
                val id = curs.ID
                val contentUri = contentUriById(id)
                val song = Song(
                    id,
                    curs.title,
                    curs.artist,
                    durationFor(contentUri),
                    curs.filename,
                    contentUri.toString()
                )
                songs.add(song)
            }
        }

        return songs
    }
}