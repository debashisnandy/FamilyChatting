package com.example.roni.familychatting.CacheContactDetails

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build

class CacheStoreServiceRunning {

    private var builder: JobInfo.Builder? = null
    private var jobScheduler: JobScheduler? = null
    fun startCaching(context: Context){
        jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        builder = JobInfo.Builder(154, ComponentName(context,
            ContactRetriveService::class.java)
        )

        if (Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M){
            builder!!.setMinimumLatency(0).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        }else{
            builder!!.setPeriodic(0).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        }

        jobScheduler!!.schedule(builder!!.build())
    }
}