package com.example.roni.familychatting.ChatScreenDetails

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.roni.familychatting.ChatHistory.Recents_Chats_Storage
import com.example.roni.familychatting.ChatHistory.Storing_Chats
import com.example.roni.familychatting.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chats.*

class Chat_Activity : AppCompatActivity() {

    private var DEFAULT_MSG_LENGTH_LIMIT = 1000
    private var mFirebaseAuth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    private var chatRef: DatabaseReference? = null
    private var recentSqlDB:Recents_Chats_Storage? = null
    private var sqlDB:Storing_Chats? = null
    private var chatList:ArrayList<MessageDetails>? = null
    private var chatAdapter:chatCustomAdaper? = null
    private var receiverPhone:String? = null
    private var user:String? = null
    private var mChildEventListener:ChildEventListener? = null
    //var editor: SharedPreferences.Editor? = null
    //private var sharePref: SharedPreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)
        setTitle(intent.getStringExtra("userName"))
        mFirebaseAuth = FirebaseAuth.getInstance()
        chatList = ArrayList()
        chatAdapter = chatCustomAdaper(this, chatList!!)
        user = mFirebaseAuth!!.currentUser!!.phoneNumber.toString()
        receiverPhone = intent.getStringExtra("userNumber")
        database = FirebaseDatabase.getInstance()
        myRef = database!!.reference.child("message")
            .child(receiverPhone!!)
        chatRef = database!!.reference.child("message")
            .child(user!!)
        //sharePref = getSharedPreferences("Recent_Contact_Chats",Context.MODE_PRIVATE)
        progressBar.visibility = View.GONE

        recentSqlDB = Recents_Chats_Storage(this)
        messageEditText.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim({ it <= ' ' }).length > 0) {
                    sendButton.isEnabled = true
                } else {
                    sendButton.isEnabled = false
                }
            }
        })

        messageEditText.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)))

        sqlDB = Storing_Chats(this,"M"+receiverPhone!!.trim('+'))

        sendButton.setOnClickListener { view ->
            var unixTime = System.currentTimeMillis() / 1000L
            var temp = messageEditText.text.toString()
            var msgDetails = MessageDetails(temp,1,user!!,unixTime)
            myRef!!.push().setValue(msgDetails)
            // recent acts
            /*
            if (sharePref!!.getString(intent.getStringExtra("userNumber"),null)==null) {
                val editor = sharePref!!.edit()
                editor.putString(intent.getStringExtra("userNumber"), intent.getStringExtra("userName"))
                editor.apply()
            }*/

            val recentsContentValues = ContentValues()
            if (!recentSqlDB!!.checkValueIsExist(intent.getStringExtra("userNumber"))) {
                recentsContentValues.put(Recents_Chats_Storage.mNumberUser, intent.getStringExtra("userNumber"))
                recentsContentValues.put(Recents_Chats_Storage.mNameUsers, intent.getStringExtra("userName"))
                recentsContentValues.put(Recents_Chats_Storage.mTimeStamps, unixTime)
                recentSqlDB!!.insert(recentsContentValues)
            }else{
                recentsContentValues.put(Recents_Chats_Storage.mNumberUser, intent.getStringExtra("userNumber"))
                recentsContentValues.put(Recents_Chats_Storage.mNameUsers, intent.getStringExtra("userName"))
                recentsContentValues.put(Recents_Chats_Storage.mTimeStamps, unixTime)
                var selectionArgs = arrayOf(intent.getStringExtra("userNumber"))
                recentSqlDB!!.update(recentsContentValues,"${Recents_Chats_Storage.mNumberUser}=?",selectionArgs)

            }

            val contentValues = ContentValues()
            contentValues.put(Storing_Chats.mTimeStamp,unixTime)
            contentValues.put(Storing_Chats.mMessage,temp)
            contentValues.put(Storing_Chats.mIsSend,0)
            sqlDB!!.insert(contentValues)
            chatList!!.add(MessageDetails(temp,0,user!!,unixTime))
            messageListView.adapter = chatAdapter

            messageEditText.setText("")

        }
        loadAllData()

    }

    fun loadAllData(){

        val cursor:Cursor = sqlDB!!.query(null,null, null,null,100.toString())

        if (cursor.moveToFirst()){
            do {
                chatList!!.add(MessageDetails(
                    cursor.getString(cursor.getColumnIndex(Storing_Chats.mMessage)),
                    cursor.getString(cursor.getColumnIndex(Storing_Chats.mIsSend)).toInt(),
                    user!!,cursor.getInt(cursor.getColumnIndex(Storing_Chats.mTimeStamp)).toLong()
                    ))
            }while (cursor.moveToNext())
        }

        messageListView.adapter = chatAdapter
    }

    override fun onResume() {
        super.onResume()
        addDataBaseReadListener()
    }

    fun addDataBaseReadListener(){
        mChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                val familyChatMessage = dataSnapshot.getValue(MessageDetails::class.java) as MessageDetails
                if (familyChatMessage.getSendersId()==receiverPhone){
                    var unixTime = System.currentTimeMillis() / 1000L
                    // recent acts
                    val recentsContentValues = ContentValues()
                    if (!recentSqlDB!!.checkValueIsExist(intent.getStringExtra("userNumber"))) {
                        recentsContentValues.put(Recents_Chats_Storage.mNumberUser, intent.getStringExtra("userNumber"))
                        recentsContentValues.put(Recents_Chats_Storage.mNameUsers, intent.getStringExtra("userName"))
                        recentsContentValues.put(Recents_Chats_Storage.mTimeStamps, unixTime)
                        recentSqlDB!!.insert(recentsContentValues)
                    }else{
                        recentsContentValues.put(Recents_Chats_Storage.mNumberUser, intent.getStringExtra("userNumber"))
                        recentsContentValues.put(Recents_Chats_Storage.mNameUsers, intent.getStringExtra("userName"))
                        recentsContentValues.put(Recents_Chats_Storage.mTimeStamps, unixTime)
                        var selectionArgs = arrayOf(intent.getStringExtra("userNumber"))
                        recentSqlDB!!.update(recentsContentValues,"${Recents_Chats_Storage.mNumberUser}=?",selectionArgs)

                    }
                    dataSnapshot.ref.removeValue()
                    val contentValues = ContentValues()
                    contentValues.put(Storing_Chats.mTimeStamp,familyChatMessage.getTimeStamps())
                    contentValues.put(Storing_Chats.mMessage,familyChatMessage.getMsg())
                    contentValues.put(Storing_Chats.mIsSend,familyChatMessage.getIsSend())
                    sqlDB!!.insert(contentValues)
                    chatList!!.add(familyChatMessage)
                    messageListView.adapter = chatAdapter
                }
                /*
                mMessageAdapter.add(friendlyMessage)*/
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        chatRef!!.addChildEventListener(mChildEventListener!!)
    }

    class chatCustomAdaper: BaseAdapter {


        var context: Context?= null
        var chatListElements = ArrayList<MessageDetails>()
        constructor(context: Context, chatListElements:ArrayList<MessageDetails>):super(){
            this.chatListElements= chatListElements
            this.context=context

        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var holder = MessageViewHolder()
            val andr = chatListElements[position]
            var layout:View? = null

            var layoutInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (andr.getIsSend()==0) {
                layout = layoutInflater.inflate(R.layout.senders_list, null)
                layout!!.setTag(holder)
                holder.name = layout.findViewById(R.id.sndTeView) as TextView
                holder.name!!.text = andr.getMsg()

            } else if (andr.getIsSend()==1) {
                layout = layoutInflater.inflate(R.layout.receivers_list, null)
                layout!!.setTag(holder)
                holder.name = layout.findViewById(R.id.rcvTeView)
                holder.name!!.text = andr.getMsg()

            }

            return layout!!

        }

        override fun getItem(position: Int): Any {
            return chatListElements[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return chatListElements.size
        }
    }

    class MessageViewHolder {
        var name: TextView? = null
    }
}
