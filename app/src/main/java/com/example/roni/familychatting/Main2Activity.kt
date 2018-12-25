package com.example.roni.familychatting

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.example.roni.familychatting.CacheContactDetails.CacheStoreServiceRunning
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Main2Activity : AppCompatActivity() {


    private var homeAct:Intent? = null
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mUserPhoneDetails:String?= null
    private var swittchPermission:Boolean = true
    private val RC_SIGN_IN = 1258
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var mDatabaseRefRegisterUsr: DatabaseReference? = null
    private val providers = arrayListOf(
        AuthUI.IdpConfig.PhoneBuilder().build())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        homeAct = Intent(this,MainActivity::class.java)
        mFirebaseAuth = FirebaseAuth.getInstance()
       // mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mDatabaseRefRegisterUsr = mFirebaseDatabase!!.reference.child("users")
        val user = FirebaseAuth.getInstance().currentUser != null

        if (user){
            //setContentView(R.layout.activity_main)
            startActivity(homeAct)
            finish()

            mUserPhoneDetails = mFirebaseAuth!!.currentUser!!.phoneNumber.toString()

        }else{

            if (isNetworkAvailable()) {
                swittchPermission = true

                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RC_SIGN_IN
                )
            }else{
                Toast.makeText(applicationContext,"Sorry no network connection", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    }
    fun  buildTheJob(){
        CacheStoreServiceRunning().startCaching(applicationContext)
    }
    var ACCESSCONTACT=123
    fun checkPermmison(){

        if(Build.VERSION.SDK_INT>=23){

            if(ActivityCompat.
                    checkSelfPermission(this,
                        android.Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),ACCESSCONTACT)
                return
            }
        }
        buildTheJob()
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){

            ACCESSCONTACT->{

                val len = permissions.size

                for (i in 0 until len){
                    val permission = permissions[i]
                    if (grantResults[i]== PackageManager.PERMISSION_DENIED){
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
                    }else if (grantResults[i]== PackageManager.PERMISSION_GRANTED){
                        buildTheJob()

                    }
                }

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==RC_SIGN_IN){
            if (resultCode== Activity.RESULT_OK){
                mUserPhoneDetails = mFirebaseAuth!!.currentUser!!.phoneNumber.toString()
                checkPermmison()
                UserDataVerifyPhone(this,mFirebaseDatabase!!,mDatabaseRefRegisterUsr!!, mUserPhoneDetails!!)
                startActivity(homeAct)
                finish()

            }else{
                finish()
            }
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}
