package com.example.roni.familychatting

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.BaseAdapter
import com.example.roni.familychatting.ApplicationBehaviourState.ApplicationStateStorage
import com.example.roni.familychatting.CacheContactDetails.*
import com.example.roni.familychatting.ChatHistory.Recents_Chats_Storage
import com.example.roni.familychatting.ChatScreenDetails.Chat_Activity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_select_layout.view.*


class MainActivity : AppCompatActivity() {



    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseAuthLis:FirebaseAuth.AuthStateListener?=null
    private val RC_SIGN_IN = 1258
    private var recentsChast:Recents_Chats_Storage? = null
    private var recentsContact:ArrayList<ContactDetails>? = null
    private var recentsCustomAdapter:RecentsCustomAdapter? = null
    private var mFirebaseDatabase:FirebaseDatabase? = null
    //private var mDatabaseRefRegisterUsr:DatabaseReference? = null
    private var shareRef:SharedPreferences? = null
    private var editor:SharedPreferences.Editor? = null
    private val providers = arrayListOf(
        AuthUI.IdpConfig.PhoneBuilder().build())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        recentsContact = ArrayList()

        shareRef = CacheValidContact(this).loadContactFromCache()
        recentsCustomAdapter = RecentsCustomAdapter(this, recentsContact!!)
        //mDatabaseRefRegisterUsr = mFirebaseDatabase!!.reference.child("users")

        editor= shareRef!!.edit()
        setRecentsAdapter()
        lview.setOnItemClickListener { parent, view, position, id ->
            val storeContact = recentsContact!![position]
            var intent = Intent(this, Chat_Activity::class.java)
            intent.putExtra("userName",storeContact.nameOfUser)
            intent.putExtra("userNumber",storeContact.numberOfUser)
            startActivity(intent)

        }

    }

    override fun onResume() {
        super.onResume()
//        Toast.makeText(this,"on rsm",Toast.LENGTH_LONG).show()
        setRecentsAdapter()
    }


    fun setRecentsAdapter(){
        var recentsList = Recents_Chats_Storage(this)

        if (recentsList.query(null,null,null,null,null).count>0){
            recentsContact!!.clear()

            var query = recentsList.query(null,null,null,Recents_Chats_Storage.mTimeStamps+" DESC",null)

            if (query.moveToFirst()){
                do {
                    recentsContact!!.add(ContactDetails(query.getString(query.getColumnIndex(Recents_Chats_Storage.mNameUsers)),
                        query.getString(query.getColumnIndex(Recents_Chats_Storage.mNumberUser))))
                } while (query.moveToNext())
            }

            lview.adapter = recentsCustomAdapter
        }

    }

    class RecentsCustomAdapter: BaseAdapter {

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


    fun addNewChat(view: View){
        if (shareRef!!.getBoolean("runningCache",false)){
            ApplicationStateStorage(applicationContext)
            editor!!.putBoolean("runningCache",false).commit()
        }
        checkPermmison()
    }

    fun openAddContactActivity(){
        val intent = Intent(this,NewContactAdd::class.java)
        startActivity(intent)
    }

    var ACCESSCONTACT=123

    fun checkPermmison(){

        if(Build.VERSION.SDK_INT>=23){

            if(ActivityCompat.
                    checkSelfPermission(this,
                        android.Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),ACCESSCONTACT)
                return
            }
        }
            openAddContactActivity()
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){

            ACCESSCONTACT->{

                val len = permissions.size

                for (i in 0 until len){
                    val permission = permissions[i]
                    if (grantResults[i]==PackageManager.PERMISSION_DENIED){
                        val showRationale:Boolean = shouldShowRequestPermissionRationale( permission )
                        if (!showRationale){
                            var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            var uri = Uri.fromParts("package", getPackageName(), null)
                            intent.setData(uri)
                            startActivity(intent)
                        }else if (Manifest.permission.READ_CONTACTS.equals(permission)){
                            val builder = AlertDialog.Builder(this)
                            builder.setMessage("Permission to access the contacts")
                                .setTitle("Permission required")

                            builder.setPositiveButton("OK"
                            ) { dialog, id ->
                                dialog.cancel()
                                checkPermmison()

                            }

                            val dialog = builder.create()
                            dialog.show()
                        }
                    }else if (grantResults[i]==PackageManager.PERMISSION_GRANTED){
                        openAddContactActivity()
                    }
                }

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater:MenuInflater = menuInflater
        inflater.inflate(R.menu.mainmenu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId){
            R.id.exit_usr ->{
                editor!!.clear().commit()
                AuthUI.getInstance().signOut(this).addOnCompleteListener {
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                        RC_SIGN_IN)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}