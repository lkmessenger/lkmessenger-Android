package org.linkmessenger.utils.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.profile.viewmodel.ProfileViewModel

class ChatRequestReceiver: BroadcastReceiver(), KoinComponent {
    private val profileViewModel:ProfileViewModel = get()
    override fun onReceive(p0: Context?, p1: Intent?) {
        try {
            if(p0==null || p1==null) return
            val status = if(p1.action== ACTION_ALLOW){
                1
            }else {
                2
            }
            val id = p1.getLongExtra(REQUEST_ID, 0)
            if(id==0L) return
            profileViewModel.sendRequestForMessageAnswer(id, status){
                if(it){
                    val notificationManager: NotificationManager = p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(id.toInt())
                }
            }
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
        }
    }
    companion object{
        const val ACTION_ALLOW = "actionAllow"
        const val ACTION_DENY = "actionDENY"
        const val REQUEST_ID = "request_id"
    }
}