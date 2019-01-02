package donggolf.android.fcm


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Handler
import android.support.v4.app.NotificationCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import donggolf.android.R
import donggolf.android.activities.IntroActivity

/**
 * Created by dev1 on 2017-12-15.
 */

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val mHandler: Handler

    init {

        mHandler = Handler()
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        if (remoteMessage == null) {
            return
        }

        val data = remoteMessage.data ?: return

        val title = data["title"]
        val body = data["body"]

        val intent = Intent(this, IntroActivity::class.java)
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("market_id", data["market_id"])
        intent.putExtra("FROM_PUSH", true)

        val pendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, 0)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000))
            .setContentIntent(pendingIntent)

        notificationBuilder.setSound(defaultSoundUri)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private val TAG = "MyFirebaseMsgService"
    }

}
