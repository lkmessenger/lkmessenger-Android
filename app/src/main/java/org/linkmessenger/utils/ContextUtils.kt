package org.linkmessenger.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import org.thoughtcrime.securesms.R

object ContextUtils {
    fun Context.getUserBadge(type:Int?, size:Int=20): Drawable?{
        if(type==null || type==0) return null
        val budgeDrawable = when(type){
            1 ->{
                when (size) {
                    20 -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_gold_badge_20)
                    }
                    24 -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_gold_24)
                    }
                    else -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_gold_28)
                    }
                }
            }
            2 ->{
                when (size) {
                    20 -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_biz_badge_20)
                    }
                    24 -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_biz_badge_24)
                    }
                    else -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_biz_badge_28)
                    }
                }
            }
            3 ->{
                when (size) {
                    20 -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_official_20)
                    }
                    24 -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_official_24)
                    }
                    else -> {
                        ContextCompat.getDrawable(this, R.drawable.ic_official_28)
                    }
                }
            }
            else->{
                null
            }
        }
        return budgeDrawable;
    }
}