package org.linkmessenger.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink.AndroidParameters
import com.google.firebase.dynamiclinks.DynamicLink.IosParameters
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import org.linkmessenger.openComment
import org.linkmessenger.openProfile
import java.text.DecimalFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.log10


object PostUtil {
    fun sharePostUrl(url:String):String{
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(url))
            .setDomainUriPrefix("https://linkm.me") // Open links with this app on Android
            .setAndroidParameters(
                AndroidParameters.Builder().build()
            ) // Open links with com.example.ios on iOS
            .setIosParameters(IosParameters.Builder("com.linkmessenger.ios").build())
            .buildDynamicLink()

        return dynamicLink.uri.toString()
    }
    fun handlePostUrl(context:Context, url:String):Boolean{
        try {
            val s = url.split("/")
            if(s.size<4){
                return false
//            throw Exception("invalid params")
            }
            if(s[3]!="posts"){
                return false
//            throw Exception("invalid params")
            }
            context.openComment(s[4].toLong())
            return true
        }catch (e:Exception){
            return false
        }
    }
    fun handleUserUrl(context:Context, url:String):Boolean{
        try {
            val s = url.split("/")
            if(s.size<4){
                return false
            }
            if(s[3]!="users"){
                return false
            }
            context.openProfile(null, s[4])
            return true
        }catch (e:Exception){
            return false
        }
    }
    fun prettyCount(number: Number): String? {
        val suffix = charArrayOf(' ', 'K', 'M', 'B', 'T', 'P', 'E')
        val numValue = number.toLong()
        val value = floor(log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("#0.0").format(
                numValue / Math.pow(
                    10.0,
                    (base * 3).toDouble()
                )
            ) + suffix[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
    }
    fun cacheInvalid(unix:Long?):Boolean{
        if(unix==null) return true
        return (Date().time-unix) > (1000*60*60)
    }
}