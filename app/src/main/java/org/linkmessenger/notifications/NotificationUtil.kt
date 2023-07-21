package org.linkmessenger.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.linkmessenger.posts.view.activities.SinglePostViewerActivity
import org.linkmessenger.profile.view.activities.ProfileActivity
import org.linkmessenger.request.models.NotificationData
import org.linkmessenger.utils.receivers.ChatRequestReceiver
import org.thoughtcrime.securesms.MainActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.mms.GlideApp


object NotificationUtil {
    fun showNotification(context: Context, title:String, text:String, type:String, data: NotificationData){
        with(NotificationManagerCompat.from(context)) {
            if(data.avatar.isNullOrEmpty()){
//                val name = if(!data.username.isNullOrEmpty()){
//                    data.username
//                }else if(data.profileName.isNullOrEmpty()){
//                    data.profileName
//                }else{
//                    "Unknown"
//                }
//
//                val fallbackPhotoDrawable = if(data.username !=null || data.profileName != null){
//                    val avatarColor = AvatarColor.valueOf(SignalStore.settings().getUserAvatarIfExist(data.userId.toString()))
//                    GeneratedContactPhoto(
//                            name!!,
//                            R.drawable.ic_profile_outline_40,
//                            AndroidUtilities.dp(30f)
//                    ).asSmallDrawable(context, avatarColor, false)
//                }else{
//                    ResourceContactPhoto(
//                            R.drawable.ic_profile_outline_40,
//                            R.drawable.ic_profile_outline_20
//                    ).asDrawable(context, AvatarColor.UNKNOWN, false)
//                }

                notify(data.id!!.toInt(), initNotification(context, title, text, type, data)
//                        .setLargeIcon(fallbackPhotoDrawable.toBitmap())
                        .build())
            }else{
                GlideApp.with(context)
                        .asBitmap()
                        .load(data.avatar)
                        .error(R.drawable.ic_fluent_person_circle_24_filled)
                        .into(object : CustomTarget<Bitmap?>() {
                            override fun onLoadCleared(placeholder: Drawable?) {}
                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                try {
                                    notify(data.id!!.toInt(), initNotification(context, title, text, type, data)
                                        .build())
                                }catch (e:Exception){
                                    FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                                }
                            }
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                                try {
                                    notify(data.id!!.toInt(), initNotification(context, title, text, type, data)
                                        .setLargeIcon(resource)
                                        .build())
                                }catch (e:Exception){
                                    FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                                }
                            }
                        })
            }
        }
    }

    private fun initNotification(context: Context, title:String, text:String, type:String, data: NotificationData):Notification.Builder{
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, createNotificationChannel(context, notificationManager, type))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(context.getColor(R.color.signal_colorPrimary))
//                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))

                    .setContentIntent(getIntentForAction(context, type, data))
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
        } else {
            Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
//                .setColor(context.getColor(R.color.signal_colorPrimary))
                .setSmallIcon(R.drawable.ic_notification)
//                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))
                .setContentIntent(getIntentForAction(context, type, data))
                .setAutoCancel(true)
        }
        if(type=="COMMENT" || type=="CHAT_REQUEST" || type=="ACCEPT_CHAT_REQUEST"){
            builder.setCategory(NotificationCompat.CATEGORY_MESSAGE)
        }else{
            builder.setCategory(NotificationCompat.CATEGORY_EVENT)
        }
        if(type=="CHAT_REQUEST" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val allowIntent = Intent(context, ChatRequestReceiver::class.java).apply {
                action = ChatRequestReceiver.ACTION_ALLOW
                putExtra(ChatRequestReceiver.REQUEST_ID, data.id!!.toLong())
            }
            val allowPendingIntent: PendingIntent =
                PendingIntent.getBroadcast(context, 0, allowIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                else PendingIntent.FLAG_UPDATE_CURRENT)

            val allowAction = Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.ic_fluent_check_20_regular),
                context.getString(R.string.allow),
                allowPendingIntent
            ).build()

            builder.addAction(allowAction)

            val denyIntent = Intent(context, ChatRequestReceiver::class.java).apply {
                action = ChatRequestReceiver.ACTION_DENY
                putExtra(ChatRequestReceiver.REQUEST_ID, data.id!!.toLong())
            }
            val denyPendingIntent: PendingIntent =
                PendingIntent.getBroadcast(context, 0, denyIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                else PendingIntent.FLAG_UPDATE_CURRENT)

            val denyAction = Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.ic_fluent_dismiss_20_regular),
                context.getString(R.string.deny),
                denyPendingIntent
            ).build()

            builder.addAction(denyAction)
        }
        return builder
    }
    private fun getIntentForAction(context: Context, type: String, data: NotificationData): PendingIntent {
        val intent = when (type) {
            "FOLLOW", "ACCEPT_CHAT_REQUEST" -> {
                ProfileActivity.newIntent(
                        context,
                        data.userId?.toInt(),
                        data.username,
                        null
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            "REACTION" -> {
                SinglePostViewerActivity.newIntent(
                        context,
                        data.postId?.toLong() ?: 0
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            "COMMENT" ->{
                SinglePostViewerActivity.newIntent(
                        context,
                        data.postId?.toLong() ?: 0
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            else -> {
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager, type:String):String{
        var channelId = ""
        var description = ""
        when(type){
            "FOLLOW"->{
                channelId = "link.notify.follows.new"
                description = context.getString(R.string.notify_follow_channel_desc)
            }
            "REACTION"->{
                channelId = "link.notify.like.new"
                description = context.getString(R.string.notify_like_channel_desc)
            }
            "COMMENT"->{
                channelId = "link.notify.comment.new"
                description = context.getString(R.string.notify_comment_channel_desc)
            }
            "CHAT_REQUEST"->{
                channelId = "link.notify.chat_request"
                description = context.getString(R.string.notify_chat_request_channel_desc)
            }
            "ACCEPT_CHAT_REQUEST"->{
                channelId = "link.notify.chat_request_allow"
                description = context.getString(R.string.notify_chat_request_allowed_channel_desc)
            }
        }
        val notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_LOW)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.enableVibration(false)
        if(type=="COMMENT" || type=="CHAT_REQUEST" || type=="ACCEPT_CHAT_REQUEST"){
            notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
        }
        notificationManager.createNotificationChannel(notificationChannel)
        return channelId
    }
    fun clearNotifications(context: Context){
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}