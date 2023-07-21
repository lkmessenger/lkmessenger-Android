package org.linkmessenger.extensions

import java.util.regex.Matcher
import java.util.regex.Pattern

fun String.getHashtags():ArrayList<String>?{
    val pattern: Pattern = Pattern.compile("#(\\w+)")
    val mat: Matcher = pattern.matcher(this)
    var tags:ArrayList<String>? = null
    while (mat.find()) {
        if(tags==null){
            tags = ArrayList()
        }
        mat.group(1)?.let { tags.add(it) }
    }
    return tags
}