package by.vadim_churun.individual.heartbeat2.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import by.vadim_churun.individual.heartbeat2.R
import javax.inject.Inject


class MediaNotificationFactory @Inject constructor(
    val appContext: Context, val actions: MediaActions ) {
    /////////////////////////////////////////////////////////////////////////////////////////////
    // PENDING INTENT:

    private val REQCODE_PLAY_PAUSE = 17
    private val REQCODE_PREVIOUS   = 34
    private val REQCODE_NEXT       = 51

    private fun createPendingIntent
    (requestCode: Int, action: String): PendingIntent {
        val int = Intent(appContext, HeartBeatMediaService::class.java)
        int.action = action
        return PendingIntent.getService(
            appContext, requestCode, int, PendingIntent.FLAG_UPDATE_CURRENT )
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    // BUILDING A NOTIFICATION:

    private val CHANNEL_ID = "mediaNotif"
    val notificationID = 105

    fun createNotification(): Notification {
        if(Build.VERSION.SDK_INT >= 26) {
            val notifMan = ContextCompat.getSystemService(
                appContext, NotificationManager::class.java )!!
            val channel = NotificationChannel(
                CHANNEL_ID,
                appContext.getString(R.string.media_service_notifchannel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notifMan.createNotificationChannel(channel)
        }

        val contentViews = RemoteViews(appContext.packageName, R.layout.media_service_notification)
        var pint = createPendingIntent(REQCODE_PREVIOUS, actions.previous())
        contentViews.setOnClickPendingIntent(R.id.imgvPrevious, pint)
        pint = createPendingIntent(REQCODE_PLAY_PAUSE, actions.playpause())
        contentViews.setOnClickPendingIntent(R.id.imgvPlayPause, pint )
        pint = createPendingIntent(REQCODE_NEXT, actions.next())
        contentViews.setOnClickPendingIntent(R.id.imgvNext, pint )

        return NotificationCompat
            .Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(contentViews)
            .build()
    }
}