package com.greyogproducts.greyog.windowcleaner.android

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.greyogproducts.greyog.windowcleaner.MainClass

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize(MainClass(), AndroidApplicationConfiguration())
    }
}
