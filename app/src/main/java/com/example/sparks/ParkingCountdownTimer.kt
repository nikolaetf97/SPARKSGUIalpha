package com.example.sparks
import android.app.NotificationManager
import android.os.CountDownTimer
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.sparks.App.Companion.notificationManager
import java.util.*
import java.util.concurrent.TimeUnit


/*-------------------------------------------------------
    Wrapper class around implementation of CountDownTimer
    allows us fine control of timer, and provides a method
    to change finish time
-------------------------------------------------------*/

object ParkingCountdownTimer  {
    private lateinit var packageName: String
    private var millisInFuture: Long = 0
    private var countDownTimer: ControlTimer? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationID: Int? = null


    public fun init(
        millisInFuture: Long,
        notificationID: Int,
        packageName: String,
        notificationBuilder: NotificationCompat.Builder
    ): ParkingCountdownTimer {
        this.millisInFuture = millisInFuture
        this.notificationID = notificationID
        this.notificationBuilder = notificationBuilder
        this.packageName = packageName

        countDownTimer = ControlTimer(millisInFuture,
            this.notificationBuilder!!, this.notificationID!!, this.packageName
        )
        countDownTimer!!.start()

        return  this
    }

    fun setTime(millisInFuture: Long){
        this.millisInFuture += millisInFuture

        countDownTimer!!.cancel()
        countDownTimer = ControlTimer(
            millisInFuture,
            this.notificationBuilder!!, this.notificationID!!, packageName
        )
        countDownTimer!!.start()
    }

    fun start(){
        countDownTimer!!.start()
    }
}


/*-----------------------------------------------
    An implementation of CountDownTimer which can
    update timer on notification, and start
    asyncTask to log parking ticket when finished
 ----------------------------------------------*/
open class ControlTimer(
    private val millisInFuture: Long,
    private val notificationBuilder: NotificationCompat.Builder,
    private val notificationID: Int,
    private val packageName: String
) : CountDownTimer(millisInFuture, 1000){
    override fun onFinish() {
        TODO("Treba jos napraviti da doda u logove kada se izadje sa parkinga")
    }

    override fun onTick(millisUntilFinished: Long) {
        val hms = String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisInFuture),
            TimeUnit.MILLISECONDS.toMinutes(millisInFuture) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millisInFuture) % TimeUnit.MINUTES.toSeconds(1)
        )

        val notificationViewExpanded = RemoteViews(packageName, R.layout.notification_layout_expanded)
        notificationViewExpanded.setTextViewText(R.id.chrono, hms)

        val notificationViewsCollapsed = RemoteViews(packageName, R.layout.notification_layout_collapsed)
        notificationViewsCollapsed.setTextViewText(R.id.chrono, hms)

        notificationBuilder
            .setCustomBigContentView(notificationViewExpanded)
            .setCustomContentView(notificationViewsCollapsed)

        notificationManager!!.notify(notificationID, notificationBuilder.build())
    }
}