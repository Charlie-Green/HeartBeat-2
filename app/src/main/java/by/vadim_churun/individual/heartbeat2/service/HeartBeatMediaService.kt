package by.vadim_churun.individual.heartbeat2.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder


class HeartBeatMediaService: Service() {
    /////////////////////////////////////////////////////////////////////////////////////////
    // BINDING:

    inner class MediaBinder: Binder() {
        val service
            get() = this@HeartBeatMediaService
    }
    private val BINDER = MediaBinder()

    override fun onBind(p0: Intent?): IBinder?
        = BINDER


    /////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreate() {
        super.startForeground(
            MediaNotificationFactory.notificationID,
            MediaNotificationFactory.createNotification(super.getApplicationContext())
        )
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        android.util.Log.v("Service", "Start: ${intent.action}")
        return START_STICKY
    }
}