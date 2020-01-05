package by.vadim_churun.individual.heartbeat2.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder


/** A help class to safely bind/unbind [HeartBeatMediaService]. **/
class MediaServiceBinder {
    private var service: HeartBeatMediaService? = null

    private val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as HeartBeatMediaService.MediaBinder).service
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
        }
    }


    fun bind(appContext: Context) {
        if(service != null) return
        val int = Intent(appContext, HeartBeatMediaService::class.java)
        appContext.startService(int)
        appContext.bindService(int, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind(appContext: Context) {
        if(service == null) return
        service = null
        appContext.unbindService(connection)
    }
}