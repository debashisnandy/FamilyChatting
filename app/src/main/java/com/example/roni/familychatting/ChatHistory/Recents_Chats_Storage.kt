package com.example.roni.familychatting.ChatHistory

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder

class Recents_Chats_Storage {

    private var sqlDB:SQLiteDatabase? = null
    companion object {
        private val mDataBaseName = "Recent_Chat_Stored"
        val mTableName = "Chat_List"
        val mNumberUser = "NumbersOfUser"
        val mNameUsers = "NameOfUsers"
        val mTimeStamps = "UtimeStamps"
        private val dbVersion = 1
        val createTable = "CREATE TABLE IF NOT EXISTS "+ mTableName+"("+
                mNumberUser + " TEXT PRIMARY KEY,"+ mNameUsers + " TEXT NOT NULL,"+
                mTimeStamps + " INTEGER NOT NULL);"
    }

    private class chatDbManegerHelper(context: Context):SQLiteOpenHelper(context, mDataBaseName,null, dbVersion){
        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(createTable)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("DROP TABLE IF EXISTS $mTableName")
            onCreate(db)
        }
    }

    constructor(context: Context){
        var dbManeger = chatDbManegerHelper(context)
        sqlDB = dbManeger.writableDatabase
    }

    fun insert(contentValues: ContentValues):Long{
        var ID = sqlDB!!.insert(mTableName,null,contentValues)
        return ID
    }

    fun query(projection:Array<String>?,selection:String?,
              selectionArgs:Array<String>?,sortOrder:String?,limit:String?): Cursor {
        var sqLiteQueryBuilder = SQLiteQueryBuilder()
        sqLiteQueryBuilder.tables = mTableName
        var cursor: Cursor = sqLiteQueryBuilder.query(sqlDB,projection,selection,selectionArgs,
            null,null,sortOrder,limit)
        return cursor
    }

    fun update(contentValues: ContentValues, Selection: String, SelectionArgs: Array<String>): Int {

        return sqlDB!!.update(mTableName, contentValues, Selection, SelectionArgs)

    }

    fun checkValueIsExist(checkValue:String):Boolean{

        var query = "SELECT $mNumberUser FROM $mTableName WHERE $mNumberUser=?;"
        val cursor = sqlDB!!.rawQuery(query, arrayOf(checkValue))
        return cursor.count>0

    }
}