package com.example.roni.familychatting

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.support.v4.app.FragmentActivity
import android.view.*
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.Toast
import com.example.roni.familychatting.CacheContactDetails.CacheStoreServiceRunning
import com.example.roni.familychatting.CacheContactDetails.CacheValidContact
import com.example.roni.familychatting.ChatScreenDetails.Chat_Activity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_new_contact_add.*
import kotlinx.android.synthetic.main.contact_select_layout.view.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import com.example.roni.familychatting.ApplicationBehaviourState.ApplicationStateStorage


class NewContactAdd : AppCompatActivity() {

    private var shareRef:SharedPreferences? = null
    private var editor:SharedPreferences.Editor? = null
    private var mListOfContactForDisplay = ArrayList<ContactDetails>()
    private var adapterCustom:myCustomAdapter? = null

    //private var linlaHeaderProgress:LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shareRef = getSharedPreferences("ApplicationStateStore",Context.MODE_PRIVATE)
        setContentView(R.layout.activity_new_contact_add)
        adapterCustom = myCustomAdapter(this, mListOfContactForDisplay)
        editor = shareRef!!.edit()
        if (shareRef!!.getBoolean("AdapterSettings",false)) {
            SetAdapterTask(this).execute()
            editor!!.putBoolean("AdapterSettings",false).commit()
        }else{
            contact_list.adapter = adapterCustom
            linlaHeaderProgress.visibility=View.GONE
            setMyAdapter()
        }

        contact_list.setOnItemClickListener { parent, view, position, id ->
            var storeContact = mListOfContactForDisplay[position]
            var intent = Intent(this,Chat_Activity::class.java)
            intent.putExtra("userName",storeContact.nameOfUser)
            intent.putExtra("userNumber",storeContact.numberOfUser)
            startActivity(intent)
            finish()
        }



    }



    private fun setMyAdapter(){
        val retriveCacheContact:SharedPreferences = CacheValidContact(applicationContext).loadContactFromCache()!!
        var cachingContact = retriveCacheContact.all
        //cachingContact.

        //mListOfContactForDisplay.clear()

        for (entry in cachingContact) {
            if (entry.key!="runningCache") {
                mListOfContactForDisplay.add(ContactDetails(entry.value.toString(), entry.key.toString()))
            }
        }



    }

    class myCustomAdapter:BaseAdapter{

        var mPhoneNumberList:ArrayList<ContactDetails>?= null
        var context:Context? = null

        constructor(context: Context,mPhoneNumberList: ArrayList<ContactDetails>){
            this.context=context
            this.mPhoneNumberList=mPhoneNumberList
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val conList = mPhoneNumberList!![position]
            val layoutInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val myView = layoutInflater.inflate(R.layout.contact_select_layout,null)
            myView.shwContact.text = conList.nameOfUser
            return myView
        }

        override fun getItem(position: Int): Any {
            return mPhoneNumberList!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mPhoneNumberList!!.size
        }

    }

    class SetAdapterTask(private var activity: NewContactAdd): AsyncTask<String, String, String>() {


        override fun doInBackground(vararg params: String?): String {
            var i = activity.isJobServiceOn()
            while (i){
                Thread.sleep(800)
                i=activity.isJobServiceOn()
            }

            activity.setMyAdapter()
            return null.toString()
        }

        override fun onPreExecute() {
            activity.linlaHeaderProgress.visibility = View.VISIBLE

        }


        override fun onPostExecute(result: String?) {
            activity.contact_list.adapter = activity.adapterCustom
            activity.linlaHeaderProgress.visibility = View.GONE

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.contact_list_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId){
            R.id.refresh_list ->{

                mListOfContactForDisplay.clear()

                if (!isJobServiceOn()) {
                    CacheStoreServiceRunning().startCaching(this)
                    SetAdapterTask(this).execute()
                }else{
                    Toast.makeText(applicationContext,"Fetching Contact",Toast.LENGTH_LONG).show()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun isJobServiceOn(): Boolean {
        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        var hasBeenScheduled = false

        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == 154) {
                hasBeenScheduled = true
                break
            }
        }

        return hasBeenScheduled
    }
}
