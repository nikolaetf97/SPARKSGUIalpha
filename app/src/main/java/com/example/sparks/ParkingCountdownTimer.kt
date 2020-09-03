package com.example.sparks
import android.app.PendingIntent
import android.content.Intent
import android.os.CountDownTimer
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.sparks.App.Companion.notificationManager
import com.example.sparks.SPARKService.Companion.context
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ParkingCountdownTimer  {
    private lateinit var packageName: String
    var millisInFuture: Long = 0
    private var countDownTimer: ControlTimer? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationID: Int? = null
    private var sparkService: SPARKService? = null


    fun init(
        millisInFuture: Long,
        notificationID: Int,
        packageName: String,
        notificationBuilder: NotificationCompat.Builder,
        sparkService: SPARKService
    ): ParkingCountdownTimer {
        this.millisInFuture = millisInFuture
        this.notificationID = notificationID
        this.notificationBuilder = notificationBuilder
        this.packageName = packageName
        this.sparkService = sparkService

        countDownTimer = ControlTimer(millisInFuture,
            this.notificationBuilder!!, this.notificationID!!, this.packageName, this.sparkService!!
        )

        countDownTimer!!.start()
        return this
    }

    fun setTime(toAdd: Long){
        this.millisInFuture += toAdd

        countDownTimer!!.cancel()
        countDownTimer = ControlTimer(
            millisInFuture,
            this.notificationBuilder!!, this.notificationID!!, packageName, this.sparkService!!
        )
        countDownTimer!!.start()
    }

    fun start(){
        countDownTimer!!.start()
    }
}

open class ControlTimer(
    millisInFuture: Long,
    private val notificationBuilder: NotificationCompat.Builder,
    private val notificationID: Int,
    private val packageName: String,
    private val sparkService: SPARKService
) : CountDownTimer(millisInFuture, 1000){
    override fun onFinish() {
        val len: String = if(SPARKService.leavingTime == 0L)
            format(System.currentTimeMillis() - SPARKService.arrivalTime)
        else
            format(SPARKService.leavingTime - SPARKService.arrivalTime)
        val date = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.US).format(Date(SPARKService.arrivalTime))
        val cost = SPARKService.cost.toString() + " KM"
        val loc = PSpotSupplier.parkingSpots
            .filter { spot -> spot.getMarker() == MainActivity.DESTINATION }[0].name


        LogDataSupplier.addLog(date, cost, loc, len)
        sparkService.prepareForegroundNotification()
    }

    override fun onTick(millisUntilFinished: Long) {
        ParkingCountdownTimer.millisInFuture = millisUntilFinished
        val extendIntent = Intent(context, ExtendBroadcastReceiver::class.java)
        val showInfoIntent = Intent(context, ShowInfoIntentService::class.java)

        extendIntent.action = "EXTEND_DEFAULT"
        showInfoIntent.action = "SHOW_INFO"

        val goBackToMainIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java), 0)

        val hms = format(millisUntilFinished)

        val notificationViewExpanded = RemoteViews(packageName, R.layout.notification_layout_expanded)
        notificationViewExpanded.setTextViewText(R.id.chrono, hms)

        val notificationViewsCollapsed = RemoteViews(packageName, R.layout.notification_layout_collapsed)
        notificationViewsCollapsed.setTextViewText(R.id.chrono, hms)

        notificationViewExpanded.setOnClickPendingIntent(R.id.bt_extend, PendingIntent.getBroadcast(
            context, 0, extendIntent, 0
        ))
        notificationViewExpanded.setOnClickPendingIntent(R.id.bt_info, PendingIntent.getService(
            context, 0, showInfoIntent, PendingIntent.FLAG_UPDATE_CURRENT
        ))

        notificationBuilder
            .setSmallIcon(R.drawable.parking_pin_large)
            .setContentTitle(context.getString(R.string.remaining_time))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setCustomContentView(notificationViewsCollapsed)
            .setCustomBigContentView(notificationViewExpanded)
            .setContentIntent(goBackToMainIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        notificationManager!!.notify(notificationID, notificationBuilder.build())
    }

    companion object {
        fun format(millis: Long): String = String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        )
    }

}