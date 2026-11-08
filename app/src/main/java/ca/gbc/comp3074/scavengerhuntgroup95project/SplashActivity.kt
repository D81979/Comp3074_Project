package ca.gbc.comp3074.scavengerhuntgroup95project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
            finish()
        }, 2000) // 2 seconds

        val progressBar =
            findViewById<ProgressBar>(R.id.progressBar)

        progressBar.max = 100

        Thread {
            for (i in 0..100) {
                progressBar.post { progressBar.progress = i }
                Thread.sleep(20)
            }
        }.start()
    }
}