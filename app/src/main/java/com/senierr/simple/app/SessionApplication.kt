package com.senierr.simple.app

import android.app.Application

import com.senierr.simple.repository.Repository

class SessionApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this

        Repository.initialize(this)
    }

    companion object {
        var application: SessionApplication? = null
            private set
    }
}
