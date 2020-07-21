package com.example.sparks

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.sparks.ExtendBroadcastReceiver.Companion.DEFAULT_EXTEND

/*
*  TODO("Napisi ovaj servis da prikazuje onaj tajmer u notifikaciji i " +
                "da se pokrene iz onArrival workera")
* */
class CheckLeavingService: Service() {
    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val extendIntent = Intent(this, ExtendBroadcastReceiver::class.java)
        val goBackToMainIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java), 0)

        val notificationViewsExpanded = RemoteViews(packageName, R.layout.notification_layout_expanded)
        val notificationViewsCollapsed = RemoteViews(packageName, R.layout.notification_layout_collapsed)

        notificationViewsExpanded.setOnClickPendingIntent(R.id.quickExtendTimer, PendingIntent.getActivity(
            this, 0, extendIntent, 0
        ))

        val notificationBuilder = NotificationCompat.Builder(this, "channel1")
            .setSmallIcon(R.drawable.parking_pin_large)
            .setContentTitle("Preostalo vrijeme na parkingu")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setCustomContentView(notificationViewsCollapsed)
            .setCustomBigContentView(notificationViewsExpanded)
            .setContentIntent(goBackToMainIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        startForeground(111, notificationBuilder.build())

        return START_NOT_STICKY

        ParkingCountdownTimer.init(DEFAULT_EXTEND, 111, packageName, notificationBuilder)
    }
}