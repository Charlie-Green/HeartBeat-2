package by.vadim_churun.individual.heartbeat2.storage

import by.vadim_churun.individual.heartbeat2.shared.*
import android.Manifest
import android.content.*
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore


class ExternalStorageSongsSource(
    val resources: Resources,
    val resolver: ContentResolver
): SongsSource {
    constructor(appContext: Context):
        this(appContext.resources, appContext.contentResolver)

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
    private val COLUMN_NAME_IS_SONG = MediaStore.Audio.Media.IS_MUSIC

    // Use of the deprecated field is needed so that filename can be displayed to the user.
    // It's not going to be used for playback.
    @Suppress("DEPRECATION")
    private val COLUMN_NAME_FILENAME = MediaStore.Audio.Media.DATA


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

    override val ID: Byte
        get() = 0

    override val name: String
        get() = resources.getString(R.string.source_name)

    override val recommendedSyncPeriod: Int
        get() = 9    // Synchronize each 9 seconds.

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
                    contentUri.toString(),
                    this.ID
                )
                songs.add(song)
            }
        }

        return songs
    }
}