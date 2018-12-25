package com.example.roni.familychatting.ApplicationBehaviourState

import android.content.Context
import android.content.SharedPreferences

class ApplicationStateStorage(context: Context) {

    var editor:SharedPreferences.Editor? = null
    var sharePref:SharedPreferences? = null
    init {

        sharePref = context.getSharedPreferences("ApplicationStateStore",Context.MODE_PRIVATE)
        editor = sharePref!!.edit()
        editor!!.putBoolean("AdapterSettings",true)
        editor!!.commit()
    }


    fun loadAllDetails():SharedPreferences.Editor?{
        return editor
    }
}