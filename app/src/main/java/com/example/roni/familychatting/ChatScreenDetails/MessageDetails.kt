package com.example.roni.familychatting.ChatScreenDetails


class MessageDetails {

    private var sendersId:String? = null
    private var msg:String? = null
    private var timeStamps:Long? = null
    private var isSend:Int? = null

    public constructor(){}

    public constructor(msg:String,isSend:Int,sendersId:String,timeStamps:Long) {
        this.timeStamps = timeStamps
        this.msg = msg
        this.sendersId = sendersId
        this.isSend = isSend
    }


    fun getTimeStamps():Long{
        return timeStamps!!
    }

    fun getMsg():String{
        return msg!!
    }

    fun getSendersId():String{
        return sendersId!!
    }

    fun getIsSend():Int{
        return isSend!!
    }


}