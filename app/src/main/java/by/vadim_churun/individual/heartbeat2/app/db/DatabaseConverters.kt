package by.vadim_churun.individual.heartbeat2.app.db

import androidx.room.TypeConverter
import by.vadim_churun.individual.heartbeat2.shared.SongsSource
import by.vadim_churun.individual.heartbeat2.storage.ExternalStorageSongsSource


class DatabaseConverters {
    @TypeConverter
    fun sourceClassToByte(sourceClass: Class<out SongsSource>): Byte
        = when(sourceClass) {
            ExternalStorageSongsSource::class.java -> 0
            else -> throw IllegalArgumentException(
                "Unknown ${SongsSource::class.java.simpleName}" )
        }

    @TypeConverter
    fun byteToSourceClass(byte: Byte): Class<out SongsSource>
        = when(byte.toInt()) {
            0 -> ExternalStorageSongsSource::class.java
            else -> throw IllegalArgumentException(
                "$byte doesn't correspond to any ${SongsSource::class.java.simpleName}" )
        }
}