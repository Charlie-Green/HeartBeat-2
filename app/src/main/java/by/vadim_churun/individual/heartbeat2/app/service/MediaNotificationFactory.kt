package by.vadim_churun.individual.heartbeat2.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import by.vadim_churun.individual.heartbeat2.app.R
import by.vadim_churun.individual.heartbeat2.app.model.obj.SongStub
import by.vadim_churun.individual.heartbeat2.app.model.state.PlaybackState
import by.vadim_churun.individual.heartbeat2.app.ui.main.HeartBeatMainActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MediaNotificationFactory @Inject constructor(
    val appContext: Context, val actions: MediaActions ) {
    /////////////////////////////////////////////////////////////////////////////////////////////
    // PENDING INTENT:

    private val REQCODE_PLAY_PAUSE = 17
    private val REQCODE_PREVIOUS   = 34
    private val REQCODE_NEXT       = 51
    private val REQCODE_ACTIVITY   = 117

    private fun createPendingIntent
    (requestCode: Int, action: String): PendingIntent {
        val int = Intent(appContext, HeartBeatMediaService::class.java)
        int.action = action
        return PendingIntent.getService(
            appContext, requestCode, int, PendingIntent.FLAG_UPDATE_CURRENT )
    }

    private fun createPendingIntent
    (requestCode: Int, activityClass: Class<out Activity>): PendingIntent {
        val int = Intent(appContext, activityClass)
        return PendingIntent.getActivity(
            appContext, requestCode, int, PendingIntent.FLAG_UPDATE_CURRENT )
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    // BUILDING A NOTIFICATION:

    private val CHANNEL_ID = "mediaNotif"
    val notificationID = 105
    private var contentViews: RemoteViews? = null
    private var notifBuilder: NotificationCompat.Builder? = null

    private var notif: Notification? = null
    val notification
        get() = notif ?: createNotification().also { notif = it }

    private fun createNotification(): Notification {
        if(Build.VERSION.SDK_INT >= 26) {
            val notifMan = ContextCompat.getSystemService(
                appContext, NotificationManager::class.java )!!
            val channel = NotificationChannel(
                CHANNEL_ID,
                appContext.getString(R.string.media_service_notifchannel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notifMan.createNotificationChannel(channel)
        }

        val cViews = RemoteViews(appContext.packageName, R.layout.media_service_notification)
        var pint = createPendingIntent(REQCODE_PREVIOUS, actions.previous())
        cViews.setOnClickPendingIntent(R.id.imgvPrevious, pint)
        pint = createPendingIntent(REQCODE_PLAY_PAUSE, actions.playpause())
        cViews.setOnClickPendingIntent(R.id.imgvPlayPause, pint )
        pint = createPendingIntent(REQCODE_NEXT, actions.next())
        cViews.setOnClickPendingIntent(R.id.imgvNext, pint )
        contentViews = cViews

        pint = createPendingIntent(REQCODE_ACTIVITY, HeartBeatMainActivity::class.java)
        return NotificationCompat
            .Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(contentViews)
            .setContentIntent(pint)
            .also { notifBuilder = it }
            .build()
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    // UPDATING THE NOTIFICATION:

    private class NotifStateHolder(
        val title: String,
        val artist: String,
        val isPlaying: Boolean
    ) {
        constructor(res: Resources, stub: SongStub?, isPlaying: Boolean):
            this(
                stub?.displayTitle ?: res.getString(R.string.nothing_playing),
                stub?.displayArtist ?: "",
                isPlaying
            )

        override fun equals(other: Any?): Boolean {
            if(other !is NotifStateHolder?)
                throw IllegalArgumentException("Cannot compare")
            return (isPlaying == other?.isPlaying) &&
                (title == other?.title) &&
                (artist == other?.artist)
        }
    }

    private var lastStateHolder: NotifStateHolder? = null

    private fun RemoteViews.setStub(stub: SongStub) {
        setTextViewText(R.id.tvTitle, stub.displayTitle)
        setTextViewText(R.id.tvArtist, stub.displayArtist)
    }

    private fun updateNotification(state: PlaybackState) {
        val views = contentViews ?: return
        val builder = notifBuilder ?: return

        val newStateHolder = when(state) {
            is PlaybackState.Preparing ->
                NotifStateHolder(appContext.resources, state.stub, false)
            is PlaybackState.Playing ->
                NotifStateHolder(appContext.resources, state.stub, true)
            is PlaybackState.Paused ->
                NotifStateHolder(appContext.resources, state.stub, false)
            is PlaybackState.Stopped ->
                NotifStateHolder(appContext.resources, state.lastStub, false)

            is PlaybackState.PlayFailed -> {
                val msgPlayFailed = appContext.getString(R.string.play_failed)
                NotifStateHolder(msgPlayFailed, "", false)
            }
        }

        if(newStateHolder == lastStateHolder) return
        lastStateHolder = newStateHolder

        views.setTextViewText(R.id.tvTitle, newStateHolder.title)
        views.setTextViewText(R.id.tvArtist, newStateHolder.artist)
        val iconID =
            if(newStateHolder.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        views.setImageViewResource(R.id.imgvPlayPause, iconID)

        builder.setCustomContentView(views)
        val updatedNotif = builder
            .setCustomContentView(views)
            .build()
            .also { notif = it }
        val notifMan = ContextCompat.getSystemService(
            appContext, NotificationManager::class.java )!!
        notifMan.notify(notificationID, updatedNotif)
    }


    private val disposable = CompositeDisposable()

    fun observeState(observable: Observable<PlaybackState>) {
        observable.doOnNext { state ->
            updateNotification(state)
        }.subscribe()
        .also { disposable.add(it) }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    // OTHER:

    fun dispose() {
        disposable.clear()
    }
}