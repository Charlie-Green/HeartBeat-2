package by.vadim_churun.individual.heartbeat2.app.service

import android.content.*
import android.os.IBinder
import android.os.RemoteException


/** A help class to safely bind/unbind [HeartBeatMediaService]. **/
class MediaServiceBinder {
    private var service: HeartBeatMediaService? = null
    private var connection: ServiceConnection? = null


    fun bind(context: Context, onConnected: (service: HeartBeatMediaService) -> Unit) {
        if(connection != null)
            throw Exception("${HeartBeatMediaService::class.java.simpleName} has been bound")

        val mConnection = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                service = (binder as HeartBeatMediaService.MediaBinder).service
                interact(onConnected)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                service = null; connection = null
            }
        }.also { connection = it }
        val int = Intent(context.applicationContext, HeartBeatMediaService::class.java)
        context.startService(int)
        context.bindService(int, mConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbind(context: Context) {
        connection?.also { context.unbindService(it) }
        service = null; connection = null
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