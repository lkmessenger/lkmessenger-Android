package org.linkmessenger.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class DateParser {
    @SuppressLint("SimpleDateFormat")
    fun getDateTimeInMilliseconds(date: String): Long {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val formattedDate = inputFormat.parse(date)
        return formattedDate!!.time
    }
}