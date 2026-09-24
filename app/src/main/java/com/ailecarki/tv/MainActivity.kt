package com.ailecarki.tv

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ailecarki.tv.ui.AppRoot
import com.ailecarki.tv.ui.theme.AileCarkiTheme

class MainActivity : ComponentActivity() {
    private val audio get() = (application as AileCarkiApp).container.audio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            AileCarkiTheme {
                AppRoot(onExit = { finish() })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audio.pauseAll()
    }

    override fun onResume() {
        super.onResume()
        audio.resumeAll()
    }
}
