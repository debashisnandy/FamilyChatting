package com.example.roni.familychatting.ChatHistory

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.media.projection.MediaProjection
import android.util.Log

class Storing_Chats(context: Context,mTableName:String) {

    private var context:Context? = null

    companion object {

        var mTableName:String? = null
        var mTimeStamp:String = "Time_Stamp"
        var mMessage:String = "Message"
        var mIsSend:String = "isSend"
        private var DBversion = 1

    }
    var mDataBaseName = "Family_Chat_Storage"
    private var createTable:String? = null

    private var sqlDB:SQLiteDatabase? = null

    private class ChatDbManegerHelper
        (context: Context,createTable:String,mDataBaseName:String):SQLiteOpenHelper
        (context, mDataBaseName, null,DBversion){
        var createTable:String? = null
        init {
            Log.d("MyTABLEESB",Companion.mTableName.toString())
            this.createTable = createTable
        }

        override fun onCreate(db: SQLiteDatabase?) {

            db!!.execSQL(createTable)
            Log.d("MyTABLEES",mTableName.toString())
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("DROP TABLE IF EXISTS "+ mTableName)
            onCreate(db)
        }
    }

    init {
        this.context = context
        Companion.mTableName = mTableName
        mDataBaseName += mTableName
        createTable = "CREATE TABLE IF NOT EXISTS "+ Companion.mTableName.toString() +"(" +
        mTimeStamp + " INTEGER PRIMARY KEY,"+ mMessage+" TEXT NOT NULL,"+ mIsSend+
                " INTEGER NOT NULL DEFAULT 0)"
        var db = ChatDbManegerHelper(context,createTable!!,mDataBaseName)
        sqlDB = db.writableDatabase
    }

    fun insert(contentValues: ContentValues):Long{
        var ID = sqlDB!!.insert(mTableName,"",contentValues)
        return ID
    }

    fun query(projection:Array<String>?,selection:String?,
              selectionArgs:Array<String>?,sortOrder:String?,limit:String?):Cursor{
        var sqLiteQueryBuilder = SQLiteQueryBuilder()
        sqLiteQueryBuilder.tables = mTableName
        var cursor:Cursor = sqLiteQueryBuilder.query(sqlDB,projection,selection,selectionArgs,
            null,null,sortOrder,limit)
        return cursor
    }
}