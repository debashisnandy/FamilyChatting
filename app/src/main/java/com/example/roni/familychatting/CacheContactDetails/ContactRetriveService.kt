package com.example.roni.familychatting.CacheContactDetails

import android.app.job.JobParameters
import android.app.job.JobService
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import com.example.roni.familychatting.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_new_contact_add.*

class ContactRetriveService:JobService() {

    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var mDatabaseRefRegisterUsr: DatabaseReference? = null

    private var mPhoneNumberList = ArrayList<ContactDetails>()
    override fun onStopJob(params: JobParameters?): Boolean {

        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mDatabaseRefRegisterUsr = mFirebaseDatabase!!.reference.child("users")
        storingCache()
        jobFinished(params,false)
        return true
    }

    private fun getContactList() {
        val cr = contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )

                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )
                    while (pCur!!.moveToNext()) {
                        var phoneNo = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        phoneNo = phoneNo.replace("[ -*,#]".toRegex(),"")
                        if (phoneNo.contains("+91")){
                            mPhoneNumberList.add(ContactDetails(name,phoneNo))
                        }else{
                            var tmp = "+91" + phoneNo
                            mPhoneNumberList.add(ContactDetails(name,tmp))
                        }


                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
    }

    private fun storingCache(){

        getContactList()
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (i in 0 until mPhoneNumberList.size){
                    var usrInfo = mPhoneNumberList[i]
                    //Toast.makeText(applicationContext,usrInfo.numberOfUser.toString(),Toast.LENGTH_LONG).show()

                    // adding child to the cache
                    if (dataSnapshot.hasChild(usrInfo.numberOfUser.toString()))
                    {
                        //Thread.sleep(1000)
                        CacheValidContact(applicationContext).saveContactToCache(usrInfo.numberOfUser.toString(),
                            usrInfo.nameOfUser.toString())
                    }

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        mDatabaseRefRegisterUsr!!.addListenerForSingleValueEvent(eventListener)

    }

}