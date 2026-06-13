package com.example.jailbreaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

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

        val logo = findViewById<ImageView>(R.id.splashLogo)
        val bars = findViewById<LinearLayout>(R.id.barsContainer)
        val bootProgress = findViewById<ProgressBar>(R.id.bootProgress)
        val percentText = findViewById<TextView>(R.id.percentText)
        val logTextView = findViewById<TextView>(R.id.logTextView)
        val progressBox = findViewById<LinearLayout>(R.id.progressBox)
        val commenceButton = findViewById<View>(R.id.commenceButton)
        val statusBadgeText = findViewById<TextView>(R.id.statusBadgeText)

        // 1. Simulate Progress and Logs
        var progress = 0
        val handler = Handler(Looper.getMainLooper())
        
        val progressRunnable = object : Runnable {
            override fun run() {
                if (progress < 100) {
                    progress += (Math.random() * 8 + 3).toInt()
                    if (progress > 100) progress = 100
                    
                    bootProgress.progress = progress
                    percentText.text = "$progress%"
                    
                    // Update logs based on progress
                    val logIndex = (progress / 26).coerceAtMost(bootLogs.size - 1)
                    val displayedLogs = StringBuilder()
                    for (i in 0..logIndex) {
                        displayedLogs.append("> ${bootLogs[i]}\n")
                    }
                    logTextView.text = displayedLogs.toString()
                    
                    handler.postDelayed(this, 150)
                } else {
                    // 2. Preparation Complete
                    progressBox.visibility = View.GONE
                    commenceButton.visibility = View.VISIBLE
                    statusBadgeText.text = "READY"
                    statusBadgeText.setTextColor(ContextCompat.getColor(this@SplashActivity, R.color.success))
                }
            }
        }
        handler.post(progressRunnable)

        // 3. Handle Commencement
        commenceButton.setOnClickListener {
            // Trigger Bar Break Animation
            val leftBarsAnim = TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, -1.8f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f
            ).apply {
                duration = 800
                fillAfter = true
                interpolator = AccelerateDecelerateInterpolator()
            }

            val rightBarsAnim = TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 1.8f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0f
            ).apply {
                duration = 800
                fillAfter = true
                interpolator = AccelerateDecelerateInterpolator()
            }

            for (i in 0 until bars.childCount) {
                val bar = bars.getChildAt(i)
                if (i < 3) bar.startAnimation(leftBarsAnim)
                else bar.startAnimation(rightBarsAnim)
            }

            // Move to Main Activity after bars clear
            handler.postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }, 900)
        }
    }
}
