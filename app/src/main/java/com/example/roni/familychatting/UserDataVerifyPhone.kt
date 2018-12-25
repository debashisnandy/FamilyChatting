package com.example.roni.familychatting

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.*

class UserDataVerifyPhone(context: Context ,mFirebaseDatabase: FirebaseDatabase,mDatabaseRefRegisterUsr: DatabaseReference,mUserPhoneDetails:String) {
    var mUserPhoneDetails:String? = null
    var mDatabaseRefRegisterUsr:DatabaseReference?= null
    var mFirebaseDatabase:FirebaseDatabase?= null
    var context: Context? = null
    init {
        this.mUserPhoneDetails=mUserPhoneDetails
        this.mFirebaseDatabase=mFirebaseDatabase
        this.mDatabaseRefRegisterUsr = mDatabaseRefRegisterUsr
        this.context=context
        regUser()
    }

    fun regUser(){
        mFirebaseDatabase!!.reference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChild("users")) {
                    mDatabaseRefRegisterUsr!!.child(mUserPhoneDetails.toString()).setValue(true);
                }else{

                    val eventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (!dataSnapshot.hasChild(mUserPhoneDetails.toString())){
                                mDatabaseRefRegisterUsr!!.child(mUserPhoneDetails.toString()).setValue(true);

                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    }
                    mDatabaseRefRegisterUsr!!.addListenerForSingleValueEvent(eventListener)
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })


    }
}