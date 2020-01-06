package by.vadim_churun.individual.heartbeat2.service

import android.content.*
import android.os.IBinder
import android.os.RemoteException


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

    /** Attempts to execute the given callback on a [HeartBeatMediaService] instance.
      * @return whether interaction was successful. **/
    fun interact(action: (service: HeartBeatMediaService) -> Unit): Boolean {
        val mService = service ?: return false
        try {
            action(mService)
            return true
        } catch(exc: RemoteException) {
            return false
        }
    }
}