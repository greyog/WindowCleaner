@file:JvmName("DesktopLauncher")

package com.greyogproducts.greyog.windowcleaner.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

import com.greyogproducts.greyog.windowcleaner.MainClass

/** Launches the desktop (LWJGL) application. */
fun main(args: Array<String>) {
    LwjglApplication(MainClass(), LwjglApplicationConfiguration().apply {
        title = "WindowCleaner"
        width = 640
        height = 480
        resizable = false
        intArrayOf(128, 64, 32, 16).forEach{
            addIcon("libgdx$it.png", Files.FileType.Internal)
        }
    })
}
