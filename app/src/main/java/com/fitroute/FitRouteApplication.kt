package com.fitroute

import android.app.Application
import com.fitroute.service.SyncWorker

class FitRouteApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        SyncWorker.schedulePeriodic(this)
    }
}