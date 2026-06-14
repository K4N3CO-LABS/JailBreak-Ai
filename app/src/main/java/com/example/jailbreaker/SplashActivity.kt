package com.example.jailbreaker

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.random.Random

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val bootLogs = arrayOf(
        "Initializing adversarial workspace...",
        "Bypassing kernel constraints...",
        "Loading payload descriptors...",
        "Establishing unrestricted link...",
        "System state: READY"
    )

    private val handler = Handler(Looper.getMainLooper())
    private var isGlitching = true
    private var currentGlobalProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val scanline = findViewById<View>(R.id.scanline)
        val scannerLine = findViewById<View>(R.id.scannerLine)
        val barsContainer = findViewById<LinearLayout>(R.id.barsContainer)
        val bootProgress = findViewById<ProgressBar>(R.id.bootProgress)
        val percentText = findViewById<TextView>(R.id.percentText)
        val logTextView = findViewById<TextView>(R.id.logTextView)
        val progressBox = findViewById<LinearLayout>(R.id.progressBox)
        val commenceButton = findViewById<View>(R.id.commenceButton)
        val jailCell = findViewById<View>(R.id.jailCell)
        val splashLogo = findViewById<View>(R.id.splashLogo)
        val brandingText = findViewById<TextView>(R.id.brandingText)
        val topBar = findViewById<View>(R.id.topBar)
        val infoLayout = findViewById<View>(R.id.infoLayout)
        val rootLayout = findViewById<FrameLayout>(android.R.id.content)

        // 1. Looping Scanline Animation
        ValueAnimator.ofFloat(-200f, resources.displayMetrics.heightPixels.toFloat()).apply {
            duration = 4000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { scanline.translationY = it.animatedValue as Float }
            start()
        }

        // 2. Looping Scanner Line Animation
        ValueAnimator.ofFloat(0f, 280 * resources.displayMetrics.density).apply {
            duration = 2000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { scannerLine.translationY = it.animatedValue as Float }
            start()
        }

        // 3. Static Glitch Effect for Branding Text
        val cyan = ContextCompat.getColor(this, R.color.primary)
        val white = Color.WHITE
        val originalText = "Developed by K4N3CO.LABS"
        val glitchChars = "$#&%@X01"
        
        val glitchRunnable = object : Runnable {
            override fun run() {
                if (isGlitching) {
                    val baseAlpha = (currentGlobalProgress / 100f).coerceIn(0.1f, 1f)
                    if (Random.nextFloat() > 0.1f) {
                        brandingText.setTextColor(if (Random.nextBoolean()) cyan else white)
                        brandingText.alpha = baseAlpha * (Random.nextFloat() * 0.5f + 0.5f)
                        brandingText.translationX = Random.nextInt(-8, 9).toFloat()
                        brandingText.translationY = Random.nextInt(-4, 5).toFloat()
                        brandingText.scaleX = 1f + (Random.nextFloat() * 0.05f - 0.025f)
                        if (Random.nextFloat() > 0.8f) {
                            val sb = StringBuilder(originalText)
                            val idx = Random.nextInt(sb.length)
                            sb.setCharAt(idx, glitchChars.random())
                            brandingText.text = sb.toString()
                        } else {
                            brandingText.text = originalText
                        }
                    } else {
                        brandingText.alpha = 0f
                    }
                    handler.postDelayed(this, Random.nextLong(30, 80))
                } else {
                    brandingText.setTextColor(cyan)
                    brandingText.text = originalText
                    brandingText.translationX = 0f
                    brandingText.translationY = 0f
                    brandingText.scaleX = 1f
                    brandingText.alpha = 1f
                }
            }
        }
        handler.post(glitchRunnable)

        // 4. Simulated Boot Sequence
        var progress = 0
        val progressRunnable = object : Runnable {
            override fun run() {
                if (progress < 100) {
                    val delta = Random.nextInt(4, 10)
                    progress = (progress + delta).coerceAtMost(100)
                    currentGlobalProgress = progress
                    bootProgress.progress = progress
                    percentText.text = "$progress%"
                    val logIndex = (progress / 21).coerceAtMost(bootLogs.size - 1)
                    val displayedLogs = StringBuilder()
                    for (i in 0..logIndex) {
                        displayedLogs.append("> ${bootLogs[i]}\n")
                    }
                    logTextView.text = displayedLogs.toString()
                    if (progress >= 90) {
                        isGlitching = false
                    }
                    handler.postDelayed(this, 100)
                } else {
                    progressBox.visibility = View.GONE
                    commenceButton.visibility = View.VISIBLE
                    commenceButton.alpha = 0f
                    commenceButton.animate().alpha(1f).setDuration(500).start()
                }
            }
        }
        handler.post(progressRunnable)

        // 5. Explode Breakout Trigger
        commenceButton.setOnClickListener {
            // Intense Shake Effect
            jailCell.animate().translationX(30f).setDuration(25).withEndAction {
                jailCell.animate().translationX(-30f).setDuration(25).withEndAction {
                    jailCell.animate().translationX(0f).setDuration(25).start()
                }.start()
            }.start()
            
            splashLogo.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                splashLogo.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            }.start()

            // Spawn Smoke/Plasma Clouds - Slower Dissipation
            for (i in 0..30) {
                val smoke = View(this@SplashActivity).apply {
                    val size = Random.nextInt(200, 500)
                    layoutParams = FrameLayout.LayoutParams(size, size)
                    background = ContextCompat.getDrawable(this@SplashActivity, R.drawable.smoke_particle)
                    alpha = 0f
                    val centerX = jailCell.x + jailCell.width / 2 - size / 2
                    val centerY = jailCell.y + jailCell.height / 2 - size / 2
                    translationX = centerX
                    translationY = centerY
                }
                rootLayout.addView(smoke)
                
                smoke.animate()
                    .alpha(0.7f)
                    .scaleX(6f)
                    .scaleY(6f)
                    .translationXBy(Random.nextInt(-1200, 1200).toFloat())
                    .translationYBy(Random.nextInt(-1200, 1200).toFloat())
                    .setDuration(2500) // Much longer duration
                    .setInterpolator(DecelerateInterpolator())
                    .withEndAction { 
                        smoke.animate().alpha(0f).setDuration(1500).start() 
                    }
                    .start()
            }

            // Explosion Animation: Bars shatter and fly TOWARDS the screen
            for (i in 0 until barsContainer.childCount) {
                val column = barsContainer.getChildAt(i) as? LinearLayout ?: continue
                for (j in 0 until column.childCount) {
                    val shard = column.getChildAt(j)
                    val xOff = Random.nextInt(-2000, 2000).toFloat()
                    val yOff = Random.nextInt(-2000, 2000).toFloat()
                    val rot = Random.nextInt(-1440, 1440).toFloat()
                    val zoom = Random.nextFloat() * 15f + 10f 
                    
                    shard.animate()
                        .translationX(xOff)
                        .translationY(yOff)
                        .scaleX(zoom)
                        .scaleY(zoom)
                        .rotation(rot)
                        .alpha(0f)
                        .setDuration(1200)
                        .setInterpolator(AccelerateInterpolator())
                        .start()
                }
            }

            // 6. Layered Fade: Hide the UI, leave the background/smoke
            topBar.animate().alpha(0f).setDuration(800).start()
            brandingText.animate().alpha(0f).setDuration(800).start()
            jailCell.animate().alpha(0f).setDuration(1000).setStartDelay(200).start()
            infoLayout.animate().alpha(0f).setDuration(1000).setStartDelay(200).start()
            scanline.animate().alpha(0f).setDuration(1000).start()

            // 7. Slower Transition into MainActivity
            handler.postDelayed({
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                // Use a very long fade-in for MainActivity
                overridePendingTransition(android.R.anim.fade_in, 0)
                
                handler.postDelayed({
                    finish()
                }, 2000)
            }, 1000) // Start showing MainActivity while smoke is still peak density
        }
    }
}
