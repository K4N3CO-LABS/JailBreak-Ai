package com.example.jailbreaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.floor
import kotlin.random.Random

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val bootLogs = arrayOf(
        "Initializing sandboxed workspace...",
        "Loading target configurations...",
        "Preparing prompt validator pipeline...",
        "Environment verified. Ready."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val bars = findViewById<LinearLayout>(R.id.barsContainer)
        val bootProgress = findViewById<ProgressBar>(R.id.bootProgress)
        val percentText = findViewById<TextView>(R.id.percentText)
        val logTextView = findViewById<TextView>(R.id.logTextView)
        val progressBox = findViewById<LinearLayout>(R.id.progressBox)
        val commenceButton = findViewById<View>(R.id.commenceButton)
        val statusBadgeText = findViewById<TextView>(R.id.statusBadgeText)

        var progress = 0
        val handler = Handler(Looper.getMainLooper())
        
        val progressRunnable = object : Runnable {
            override fun run() {
                if (progress < 100) {
                    val delta = Random.nextInt(3, 11)
                    progress = (progress + delta).coerceAtMost(100)
                    
                    bootProgress.progress = progress
                    percentText.text = "$progress%"
                    
                    // Sync log indexes with progress
                    val logIndex = (progress / 25).coerceAtMost(bootLogs.size - 1)
                    val displayedLogs = StringBuilder()
                    for (i in 0..logIndex) {
                        displayedLogs.append("> ${bootLogs[i]}\n")
                    }
                    logTextView.text = displayedLogs.toString()
                    
                    handler.postDelayed(this, 150)
                } else {
                    // Preparation Complete
                    progressBox.visibility = View.GONE
                    commenceButton.visibility = View.VISIBLE
                    statusBadgeText.text = "READY"
                    statusBadgeText.setTextColor(ContextCompat.getColor(this@SplashActivity, R.color.success))
                }
            }
        }
        handler.post(progressRunnable)

        commenceButton.setOnClickListener {
            // Trigger Bypass/Breakout Animation
            for (i in 0 until bars.childCount) {
                val bar = bars.getChildAt(i)
                val xOffset: Float
                val rotation: Float
                
                if (i < 3) {
                    xOffset = -(500f + Random.nextFloat() * 200f)
                    rotation = -(45f + Random.nextFloat() * 30f)
                } else {
                    xOffset = (500f + Random.nextFloat() * 200f)
                    rotation = (45f + Random.nextFloat() * 30f)
                }

                bar.animate()
                    .translationX(xOffset)
                    .rotation(rotation)
                    .alpha(0.1f)
                    .setDuration(800)
                    .setInterpolator(AccelerateInterpolator())
                    .start()
            }

            // Dismiss Splash Screen after animation
            handler.postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }, 900)
        }
    }
}
