package com.example.roni.familychatting.CacheContactDetails

import android.content.Context
import android.content.SharedPreferences
import com.example.roni.familychatting.*

class CacheValidContact(context: Context) {
    var shareRef:SharedPreferences?= null
    init {
        shareRef = context.getSharedPreferences("CacheFamilyChat",Context.MODE_PRIVATE)
    }

    fun saveContactToCache(userNumber:String,userName:String){
        var editor:SharedPreferences.Editor = shareRef!!.edit()
        editor.putBoolean("runningCache",true)
        editor.putString(userNumber,userName)
        editor.commit()
    }


    fun loadContactFromCache(): SharedPreferences? {
        return shareRef
    }
}