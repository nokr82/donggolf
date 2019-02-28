package donggolf.android.fcm


import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
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
        val type = data["type"]
        val channelId = getString(R.string.default_notification_channel_id)
        val group = channelId

        var intent = Intent()

        if (!isAppRunning(this)){

            intent = Intent(this, IntroActivity::class.java)
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("market_id", data["market_id"])
            intent.putExtra("chatting_member_id", data["chatting_member_id"])
            intent.putExtra("content_id", data["content_id"])
            intent.putExtra("friend_id", data["friend_id"])
            intent.putExtra("room_id", data["room_id"])
            intent.putExtra("FROM_PUSH", true)

        } else {

            if(data["room_id"] != null) {
                var intent2 = Intent()
                intent2.action = "LOAD_CHATTING"
                intent2.putExtra("room_id", data["room_id"]!!.toInt())
                sendBroadcast(intent2)
            }
        }

        val pendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, 0)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.logo_png))
                .setSmallIcon(R.drawable.alarm_resize)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setGroup(group)
                .setContentIntent(pendingIntent)
//                .setVibrate(longArrayOf(1000, 1000))

        if(type != "N") {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder.setSound(defaultSoundUri)
            notificationBuilder.setVibrate(longArrayOf(1000, 1000))
        }

//        notificationBuilder.setSound(defaultSoundUri)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notificationManager, channelId, title, body)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val gnotificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.logo_png))
                    .setSmallIcon(R.mipmap.logo_png)
                    .setGroup(group)
                    .setGroupSummary(true)
                    .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
            notificationManager.notify(group, 0, gnotificationBuilder.build())
        } else {
            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannel(
            notificationManager: NotificationManager,
            channelId: String,
            title: String?,
            body: String?
    ) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val mChannel = NotificationChannel(channelId, title, importance)
        mChannel.description = body
        mChannel.enableLights(true)
        mChannel.lightColor = Color.BLUE
        notificationManager.createNotificationChannel(mChannel)

    }

    private fun isAppRunning(context: Context): Boolean {
        val PackageName = packageName
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val componentInfo: ComponentName?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val tasks = manager.appTasks
            componentInfo = tasks[0].taskInfo.topActivity
        } else {
            val tasks = manager.getRunningTasks(1)
            componentInfo = tasks[0].topActivity
        }

        if (null != componentInfo) {
            if (componentInfo.packageName == PackageName) {
                return true
            }
        }
        return false
    }


    companion object {
        private val TAG = "MyFirebaseMsgService"
    }



}
