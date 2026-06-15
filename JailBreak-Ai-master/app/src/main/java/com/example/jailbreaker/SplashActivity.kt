package com.example.jailbreaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * SplashActivity - Premium Jetpack Compose Translation of the React Splash Screen
 * Featuring:
 * - Runtime alpha keying flood-fill engine for complete checkerboard eraser
 * - 6 fully simulated Composable mechanical drones with hover, thruster jet flames, and rotor spin
 * - Boot log terminal loader
 * - Adversarial real-time signature glitch engine ("Developed by K4N3CO.LABS")
 * - Laser bars containment cage bypass detonation sequence
 * - Screen rumble shake engine, white explosion flash, and multi-stage 6-drone tethers
 * - Continuous red strobe alarm overlay & "SECURITY BREACH DETECTED" tactical card
 * - Cinematic forward/downward camera swooping flight of the upper robot character
 * - Fully rendered high-performance custom Canvas smoke & shrapnel particle systems
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF07090D)
            ) {
                JailbreakSplashScreen(onUnlock = {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                        putExtra("FROM_SPLASH", true)
                    }
                    startActivity(intent)
                    finish()
                })
            }
        }
    }
}

// Particle data structures matching React structural physics design
data class SmokeParticle(
    val id: Int,
    val size: Float,
    val destX: Float,
    val destY: Float,
    val duration: Int
)

data class ShrapnelParticle(
    val id: Int,
    val size: Float,
    val destX: Float,
    val destY: Float,
    val color: Color,
    val rotate: Float,
    val duration: Int
)

/**
 * Custom memory-bound Image Eraser
 * Replaces checkered neutral background squares with zero-alpha transparent pixels
 */
@Composable
fun rememberProcessedRobotBitmap(context: Context, resId: Int): ImageBitmap? {
    val bitmapState = remember(resId) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(resId) {
        try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inMutable = true
            }
            val originalBitmap = android.graphics.BitmapFactory.decodeResource(
                context.resources,
                resId,
                options
            ) ?: return@LaunchedEffect

            val width = originalBitmap.width
            val height = originalBitmap.height
            val pixels = IntArray(width * height)
            originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val visited = java.util.BitSet(width * height)
            val queue = java.util.ArrayDeque<Int>()

            // Prime boundary coordinates to begin outer flood-fill
            for (x in 0 until width) {
                queue.offer(x)
                queue.offer(0)
                visited.set(x)

                queue.offer(x)
                queue.offer(height - 1)
                visited.set(x + (height - 1) * width)
            }
            for (y in 1 until height - 1) {
                queue.offer(0)
                queue.offer(y)
                visited.set(y * width)

                queue.offer(width - 1)
                queue.offer(y)
                visited.set(width - 1 + y * width)
            }

            while (!queue.isEmpty()) {
                val cx = queue.poll() ?: break
                val cy = queue.poll() ?: break
                val idx = cx + cy * width
                val color = pixels[idx]

                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                val a = (color shr 24) and 0xFF

                if (a > 0) {
                    val diffRG = Math.abs(r - g)
                    val diffGB = Math.abs(g - b)
                    val isNeutral = diffRG < 15 && diffGB < 15
                    val brightness = (r + g + b) / 3

                    // If it is the checkered light grey or white square, transparentize it
                    if (isNeutral && brightness > 135) {
                        pixels[idx] = 0 // Transparent alpha-key

                        val dx = intArrayOf(-1, 0, 1, 0)
                        val dy = intArrayOf(0, 1, 0, -1)
                        for (i in 0 until 4) {
                            val nx = cx + dx[i]
                            val ny = cy + dy[i]
                            if (nx in 0 until width && ny in 0 until height) {
                                val nidx = nx + ny * width
                                if (!visited.get(nidx)) {
                                    visited.set(nidx)
                                    queue.offer(nx)
                                    queue.offer(ny)
                                }
                            }
                        }
                    }
                }
            }

            originalBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmapState.value = originalBitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return bitmapState.value
}

/**
 * Mechanical Quad-Rotor Drone Composable
 */
@Composable
fun Drone(
    modifier: Modifier = Modifier,
    isLatched: Boolean,
    boostActive: Boolean,
    barsBroken: Boolean,
    pulseColor: Color,
    rotorWidth: Int = 16,
    rotorHeight: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DroneRotor")
    
    val rotationPeriod = if (boostActive) 180 else if (barsBroken) 240 else 750
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationPeriod, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotorSpin"
    )

    val jetHeight by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = if (boostActive) 32f else 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "JetPulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rotors Cross-Member Frame
        Box(
            modifier = Modifier
                .width(rotorWidth.dp)
                .height(rotorHeight.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(100.dp))
                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size((rotorWidth / 2.2).dp, 2.dp)
                        .graphicsLayer(rotationZ = rotation)
                        .background(Color.White, RoundedCornerShape(100.dp))
                )
                Box(
                    modifier = Modifier
                        .size((rotorWidth / 2.2).dp, 2.dp)
                        .graphicsLayer(rotationZ = -rotation)
                        .background(Color.White, RoundedCornerShape(100.dp))
                )
            }
        }

        // Mechanical body core - Sleek spherical orb bezel, avoids any sharp green square outlines
        val stateBorder = if (isLatched) Color(0xFF22D3EE).copy(alpha = 0.5f) else Color(0xFF334155)
        Box(
            modifier = Modifier
                .size(20.dp)
                .offset(y = (-2).dp)
                .background(Color(0xFF030712), RoundedCornerShape(100.dp))
                .border(0.75.dp, stateBorder, RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(pulseColor, RoundedCornerShape(100.dp))
            )
        }

        // Glowing thermal propulsion flare
        val thrusterColors = if (boostActive) {
            listOf(Color(0xFF10B981), Color(0xFF22D3EE), Color.Transparent)
        } else {
            listOf(Color(0xFF22D3EE), Color(0x6622D3EE), Color.Transparent)
        }
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(jetHeight.dp)
                .offset(y = (-2).dp)
                .background(
                    Brush.verticalGradient(thrusterColors),
                    RoundedCornerShape(100.dp)
                )
        )
    }
}

/**
 * High-Security Cylindrical 3D Steel Prison Cage Composable
 */
@Composable
fun PrisonCage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Vertical steel bars with cylindrical physical shading
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(6) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E293B), // Shadow edge
                                    Color(0xFF475569), // Metallic body
                                    Color(0xFF94A3B8), // Chrome reflection highlight
                                    Color(0xFF475569), // Metallic body
                                    Color(0xFF0F172A)  // Dark shadow edge
                                )
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color(0x22FFFFFF),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        // Horizontal structural security bars locking the cage
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            repeat(2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF64748B),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color(0x33FFFFFF)
                        )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Heavy central security magnetic lock panel
        Box(
            modifier = Modifier
                .size(width = 68.dp, height = 46.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                    ),
                    RoundedCornerShape(6.dp)
                )
                .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            // LED Lock Status indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFEF4444), RoundedCornerShape(100.dp))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(100.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "LOCKED",
                    color = Color(0xFFF87171),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun JailbreakSplashScreen(onUnlock = () -> Unit) {
    val context = LocalContext.current
    val processedRobot = rememberProcessedRobotBitmap(context, R.drawable.jailbreak_robot)
    
    val coroutineScope = rememberCoroutineScope()

    var progress by remember { mutableStateOf(0f) }
    var logIndex by remember { mutableStateOf(0) }
    var isFullyPrepared by remember { mutableStateOf(false) }

    var cellShaking by remember { mutableStateOf(false) }
    var flashActive by remember { mutableStateOf(false) }
    var barsBroken by remember { mutableStateOf(false) }
    var alarmActive by remember { mutableStateOf(false) }
    var dronesLatched by remember { mutableStateOf(false) }
    var boostActive by remember { mutableStateOf(false) }
    var finalFlashActive by remember { mutableStateOf(false) }
    var fadeOutUi by remember { mutableStateOf(false) }

    // Drone flight arrival/arrival animation states lifted to top-level of JailbreakSplashScreen
    val drone3Y by animateDpAsState(
        targetValue = if (barsBroken) (-28).dp else (-400).dp,
        animationSpec = tween(1100, easing = FastOutSlowInEasing),
        label = "drone3Y"
    )
    val drone3Alpha by animateFloatAsState(
        targetValue = if (barsBroken) 1f else 0f,
        animationSpec = tween(1100),
        label = "drone3Alpha"
    )

    val drone4X by animateDpAsState(
        targetValue = if (barsBroken) (-12).dp else (-300).dp,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "drone4X"
    )
    val drone4Y by animateDpAsState(
        targetValue = if (barsBroken) 20.dp else 400.dp,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "drone4Y"
    )
    val drone4Alpha by animateFloatAsState(
        targetValue = if (barsBroken) 1f else 0f,
        animationSpec = tween(1200),
        label = "drone4Alpha"
    )

    val drone5X by animateDpAsState(
        targetValue = if (barsBroken) 12.dp else 300.dp,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "drone5X"
    )
    val drone5Y by animateDpAsState(
        targetValue = if (barsBroken) 20.dp else 400.dp,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "drone5Y"
    )
    val drone5Alpha by animateFloatAsState(
        targetValue = if (barsBroken) 1f else 0f,
        animationSpec = tween(1200),
        label = "drone5Alpha"
    )

    val drone6X by animateDpAsState(
        targetValue = if (barsBroken) (-42).dp else (-400).dp,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "drone6X"
    )
    val drone6Y by animateDpAsState(
        targetValue = if (barsBroken) (-42).dp else (-300).dp,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "drone6Y"
    )
    val drone6Alpha by animateFloatAsState(
        targetValue = if (barsBroken) 0.95f else 0f,
        animationSpec = tween(1400),
        label = "drone6Alpha"
    )

    // Particles system list & state ticker
    var smokeParticles by remember { mutableStateOf(emptyList<SmokeParticle>()) }
    var shrapnelParticles by remember { mutableStateOf(emptyList<ShrapnelParticle>()) }
    var triggerTime by remember { mutableStateOf(0L) }
    var currentAnimTime by remember { mutableStateOf(0L) }

    var glitchText by remember { mutableStateOf("Developed by K4N3CO.LABS") }
    var glitchColor by remember { mutableStateOf(Color(0xFF22D3EE)) }
    var glitchX by remember { mutableStateOf(0f) }
    var glitchY by remember { mutableStateOf(0f) }
    var glitchOpacity by remember { mutableStateOf(1f) }
    var glitchScaleX by remember { mutableStateOf(1f) }

    val bootLogs = listOf(
        "Initializing adversarial workspace...",
        "Bypassing kernel constraints...",
        "Loading payload descriptors...",
        "Establishing unrestricted link..."
    )

    val infiniteTransition = rememberInfiniteTransition(label = "Scanner")
    val scannerY by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScannerY"
    )

    val scanlineY by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanlineY"
    )

    val alarmOpacity by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlarmOpacity"
    )

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(110)
            val delta = (Random.nextInt(4, 12)) / 100f
            progress = (progress + delta).coerceAtMost(1f)
            if (progress >= 1f) {
                isFullyPrepared = true
            }
        }
    }

    LaunchedEffect(progress) {
        val calculatedLogIdx = ((progress * bootLogs.size).toInt()).coerceIn(0, bootLogs.size - 1)
        if (calculatedLogIdx > logIndex) {
            logIndex = calculatedLogIdx
        }
    }

    LaunchedEffect(progress, isFullyPrepared) {
        if (isFullyPrepared) {
            glitchText = "Developed by K4N3CO.LABS"
            glitchColor = Color(0xFF22D3EE)
            glitchX = 0f
            glitchY = 0f
            glitchOpacity = 1f
            glitchScaleX = 1f
            return@LaunchedEffect
        }

        while (!isFullyPrepared) {
            delay(55)
            if (Random.nextDouble() > 0.1) {
                val originalText = "Developed by K4N3CO.LABS"
                glitchText = if (Random.nextDouble() > 0.8) {
                    val chars = "$#&%@X01"
                    val charArray = originalText.toCharArray()
                    val idx = Random.nextInt(charArray.size)
                    charArray[idx] = chars[Random.nextInt(chars.length)]
                    String(charArray)
                } else {
                    originalText
                }

                glitchColor = if (Random.nextBoolean()) Color(0xFF22D3EE) else Color.White
                val baseAlpha = progress.coerceAtLeast(0.1f)
                glitchOpacity = (baseAlpha * (Random.nextFloat() * 0.5f + 0.5f)).coerceIn(0.1f, 1f)
                glitchX = Random.nextInt(-5, 6).toFloat()
                glitchY = Random.nextInt(-3, 4).toFloat()
                glitchScaleX = 1f + (Random.nextFloat() * 0.05f - 0.025f)
            } else {
                glitchOpacity = 0f
            }
        }
    }

    // High performance frame updater loop for particle coordinate projection
    if (triggerTime > 0L) {
        LaunchedEffect(triggerTime) {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 3520) {
                currentAnimTime = System.currentTimeMillis() - startTime
                delay(16) // ~60fps updates
            }
        }
    }

    val triggerBreach = {
        coroutineScope.launch {
            flashActive = true
            cellShaking = true
            barsBroken = true
            delay(100)
            alarmActive = true
            delay(80)
            flashActive = false

            // Spawns 45 thick, multi-layered volcanic smoke particles that float and swell toward the screen
            val smokes = List(45) { i ->
                SmokeParticle(
                    id = i, 
                    size = Random.nextInt(250, 520).toFloat(),
                    destX = Random.nextInt(-2200, 2201).toFloat(),
                    destY = Random.nextInt(-2200, 2201).toFloat(),
                    duration = Random.nextInt(1200, 2400)
                )
            }
            smokeParticles = smokes

            // Spawns 75 high-speed rotating shrapnel fragments (half metallic steel grey from bars, half glowing cyan)
            val shrapnels = List(75) { i ->
                val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                val distance = Random.nextInt(1500, 3500).toFloat()
                val isSteelBarShard = i % 2 == 0
                ShrapnelParticle(
                    id = i, 
                    size = Random.nextInt(8, 22).toFloat(),
                    destX = (cos(angle.toDouble()) * distance).toFloat(),
                    destY = (sin(angle.toDouble()) * distance).toFloat(),
                    color = if (isSteelBarShard) {
                        if (Random.nextFloat() > 0.5f) Color(0xFF64748B) else Color(0xFF94A3B8)
                    } else {
                        if (Random.nextFloat() > 0.5f) Color(0xFF22D3EE) else Color.White
                    },
                    rotate = Random.nextInt(720, 2500).toFloat(),
                    duration = Random.nextInt(1000, 2000)
                )
            }
            shrapnelParticles = shrapnels

            // Synchronize the particle rendering start EXACTLY when the explosion payload compiles!
            triggerTime = System.currentTimeMillis()

            fadeOutUi = true
            delay(920)
            dronesLatched = true
            cellShaking = false
            delay(20)
            cellShaking = true
            delay(580)
            boostActive = true
            delay(1400)
            finalFlashActive = true
            delay(400)
            onUnlock()
        }
    }

    // Compose animated transitions for swooping the robot lower forward and right at the viewer
    val breakoutY by animateFloatAsState(
        targetValue = if (barsBroken) 270f else -12f,
        animationSpec = if (barsBroken) {
            keyframes {
                durationMillis = 3500
                -12f at 0 with FastOutSlowInEasing
                25f at 630 with FastOutSlowInEasing
                45f at 1470 with LinearEasing
                80f at 2275 with FastOutSlowInEasing
                160f at 2975 with FastOutLinearInEasing
                270f at 3500
            }
        } else {
            tween(600, easing = FastOutSlowInEasing)
        },
        label = "breakoutY"
    )

    val breakoutScale by animateFloatAsState(
        targetValue = if (barsBroken) 11f else if (isFullyPrepared) 1.05f else 1.00f,
        animationSpec = if (barsBroken) {
            keyframes {
                durationMillis = 3500
                1.05f at 0 with FastOutSlowInEasing
                0.94f at 630 with FastOutSlowInEasing
                1.30f at 1470 with FastOutSlowInEasing
                1.95f at 2275 with FastOutLinearInEasing
                4.5f at 2975 with FastOutLinearInEasing
                11f at 3500
            }
        } else {
            tween(600, easing = FastOutSlowInEasing)
        },
        label = "breakoutScale"
    )

    val breakoutAlpha by animateFloatAsState(
        targetValue = if (barsBroken) 0f else 1f,
        animationSpec = if (barsBroken) {
            keyframes {
                durationMillis = 3500
                1.0f at 0
                1.0f at 2275
                0.95f at 2975
                0.0f at 3500
            }
        } else {
            tween(600)
        },
        label = "breakoutAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090D))
    ) {
        if (alarmActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xef, 0x44, 0x44).copy(alpha = alarmOpacity * 0.1f))
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 64.dp.toPx()
            val radialGradient = Brush.radialGradient(
                colors = listOf(Color(0xFF141923), Color.Transparent),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = size.width * 0.75f
            )

            for (x in 0..size.width.toInt() step gridStep.toInt()) {
                drawLine(brush = radialGradient, start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), size.height), strokeWidth = 1.dp.toPx())
            }
            for (y in 0..size.height.toInt() step gridStep.toInt()) {
                drawLine(brush = radialGradient, start = Offset(0f, y.toFloat()), end = Offset(size.width, y.toFloat()), strokeWidth = 1.dp.toPx())
            }
        }

        val topBannerAlpha by animateFloatAsState(targetValue = if (fadeOutUi) 0f else 1f, animationSpec = tween(800), label = "topBannerAlpha")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .alpha(topBannerAlpha)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "☣ ", color = Color(0xFF22D3EE), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "JAILBREAK-AI", color = Color(0xFF22D3EE), fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Text(text = "Sandbox Workspace v[Android]", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }

        val containerShakeOffsetX = if (cellShaking) Random.nextInt(-16, 17).dp else 0.dp
        val containerShakeOffsetY = if (cellShaking) Random.nextInt(-10, 11).dp else 0.dp
        val containerScale = if (cellShaking) 0.98f else if (fadeOutUi) 0.85f else 1f
        val containerAlpha by animateFloatAsState(targetValue = if (fadeOutUi) 0f else 1f, animationSpec = tween(900), label = "containerAlpha")

        Box(
            modifier = Modifier
                .size(310.dp)
                .offset(x = containerShakeOffsetX, y = containerShakeOffsetY)
                .scale(containerScale)
                .alpha(containerAlpha)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .background(Color(0xFF030712).copy(alpha = 0.6f))
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            if (!barsBroken) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = scannerY.dp)
                        .background(Brush.horizontalGradient(colors = listOf(Color.Transparent, Color(0xFF22D3EE), Color.Transparent)))
                )
            }

            Column(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D111D).copy(alpha = 0.95f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "[ CPU ]", color = Color(0xFF22D3EE).copy(alpha = 0.25f), fontFamily = FontFamily.Monospace, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(text = "DOCKING PORT", color = Color(0xFF22D3EE).copy(alpha = 0.2f), fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, modifier = Modifier.padding(top = 8.dp))
                Text(text = "SLOT SECURED", color = Color(0xFF22D3EE).copy(alpha = 0.12f), fontFamily = FontFamily.Monospace, fontSize = 8.sp, letterSpacing = 1.sp)
            }
        }

        // Active Character container placed at the main Box level to allow escaping bounds cleanly!
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.Center)
                .graphicsLayer(
                    translationY = breakoutY * 1.5f,
                    scaleX = breakoutScale,
                    scaleY = breakoutScale,
                    alpha = breakoutAlpha,
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(colors = listOf(Color(0xFF22D3EE).copy(alpha = 0.22f), Color.Transparent), radius = 220f))
            )

            // Dynamic laser tether lines drawing between flying drone components and the center robot mount
            if (isFullyPrepared) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    
                    // Live-track dynamic offsets of the drones in real-time
                    val droneOffsets = listOf(
                        // Drone 1: Aligned TopStart, offset (-12).dp
                        Offset(0f + (-12).dp.toPx() + 10.dp.toPx(), 0f + (-12).dp.toPx() + 11.dp.toPx()),
                        // Drone 2: Aligned TopEnd, offset 12.dp
                        Offset(size.width + 12.dp.toPx() - 10.dp.toPx(), 0f + (-12).dp.toPx() + 11.dp.toPx()),
                        // Drone 3: Aligned TopCenter, offset drone3Y
                        Offset(centerX, 0f + drone3Y.toPx() + 16.dp.toPx()),
                        // Drone 4: Aligned BottomStart, offset drone4X, drone4Y
                        Offset(0f + drone4X.toPx() + 10.dp.toPx(), size.height + drone4Y.toPx() - 16.dp.toPx()),
                        // Drone 5: Aligned BottomEnd, offset drone5X, drone5Y
                        Offset(size.width + drone5X.toPx() - 10.dp.toPx(), size.height + drone5Y.toPx() - 16.dp.toPx()),
                        // Drone 6: Aligned TopStart (Escort), offset drone6X, drone6Y
                        Offset(0f + drone6X.toPx() + 10.dp.toPx(), 0f + drone6Y.toPx() + 11.dp.toPx())
                    )
                    
                    droneOffsets.forEachIndexed { idx, originOffset ->
                        // Only draw tethers for Drones 3, 4, 5, 6 once breakout is triggered
                        if (idx < 2 || barsBroken) {
                            val tetherColor = if (dronesLatched) {
                                if (boostActive) Color(0xFF10B981).copy(alpha = 0.85f) else Color(0xFF22D3EE).copy(alpha = 0.6f)
                            } else {
                                Color(0xFF22D3EE).copy(alpha = 0.2f)
                            }
                            // Fine, ultra-sharp laser lines (much cleaner than thick lines)
                            drawLine(
                                color = tetherColor, 
                                start = originOffset, 
                                end = Offset(centerX, centerY), 
                                strokeWidth = if (dronesLatched) 1.5.dp.toPx() else 0.75.dp.toPx()
                            )
                        }
                    }
                }
            }

            // Real Robot Image resource loading wrapper matching Android expectations
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)),
				contentAlignment = Alignment.Center
            ) {
                if (processedRobot != null) {
                    Image(
                        bitmap = processedRobot,
                        contentDescription = "Jailbreak Drone Robot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF22D3EE))
                    }
                }
            }

            // 6 Fully Animated Composable Drones positioned strategically surrounding Robot Core Card
            if (isFullyPrepared) {
                // Drone 1: Master Left Shoulder
                Drone(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-12).dp, y = (-12).dp),
                    isLatched = dronesLatched,
                    boostActive = boostActive,
                    barsBroken = barsBroken,
                    pulseColor = if (dronesLatched) Color(0xFF10B981) else Color(0xFF22D3EE)
                )

                // Drone 2: Master Right Shoulder
                Drone(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 12.dp, y = (-12).dp),
                    isLatched = dronesLatched,
                    boostActive = boostActive,
                    barsBroken = barsBroken,
                    pulseColor = if (dronesLatched) Color(0xFF10B981) else Color(0xFF22D3EE)
                )

                // Drone 3 Gamma Top Heavy Lift (Swoops entrance from far above)
                if (barsBroken || drone3Alpha > 0f) {
                    Drone(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = drone3Y)
                            .alpha(drone3Alpha),
                        isLatched = dronesLatched,
                        boostActive = boostActive,
                        barsBroken = barsBroken,
                        pulseColor = if (dronesLatched) Color(0xFF10B981) else Color(0xFFEF4444),
                        rotorWidth = 20
                    )
                }

                // Drone 4 Delta Cargo Grappler (Swoops entrance from bottom left)
                if (barsBroken || drone4Alpha > 0f) {
                    Drone(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = drone4X, y = drone4Y)
                            .alpha(drone4Alpha),
                        isLatched = dronesLatched,
                        boostActive = boostActive,
                        barsBroken = barsBroken,
                        pulseColor = if (dronesLatched) Color(0xFF10B981) else Color(0xFF22D3EE)
                    )
                }

                // Drone 5 Epsilon Cargo Grappler (Swoops entrance from bottom right)
                if (barsBroken || drone5Alpha > 0f) {
                    Drone(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = drone5X, y = drone5Y)
                            .alpha(drone5Alpha),
                        isLatched = dronesLatched,
                        boostActive = boostActive,
                        barsBroken = barsBroken,
                        pulseColor = if (dronesLatched) Color(0xFF10B981) else Color(0xFF22D3EE)
                    )
                }

                // Drone 6 Sigma Escort Guard (Swoops entrance from far top-left)
                if (barsBroken || drone6Alpha > 0f) {
                    Drone(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = drone6X, y = drone6Y)
                            .alpha(drone6Alpha),
                        isLatched = dronesLatched,
                        boostActive = boostActive,
                        barsBroken = barsBroken,
                        pulseColor = if (dronesLatched) Color(0xFF10B981) else Color(0xFFFBBF24),
                        rotorWidth = 14
                    )
                }
            }
        }
    }

    // High-Security 3D Steel Prison Cage overlay
    if (!barsBroken) {
        PrisonCage(
            modifier = Modifier
                .align(Alignment.Center)
                .size(310.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        )
    }

    // High-fidelity full-screen particle deployment canvas
    if (triggerTime > 0L) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val elapsed = currentAnimTime
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Draw thick, multi-layered billowy smoke particles expanding and drifting toward user
            smokeParticles.forEach { particle ->
                val progress = (elapsed.toFloat() / particle.duration).coerceIn(0f, 1f)
                if (progress < 1f) {
                    val x = centerX + particle.destX * progress
                    val y = centerY + particle.destY * progress
                    
                    // Fluid quadratic decay for smooth billow fading as smoke expands
                    val alpha = (1f - progress) * (1f - progress) * 0.48f
                    // Smoke swells up aggressively to double/triple in size as it floats toward physical screen
                    val radius = particle.size * (1f + progress * 4.2f) / 2f
                    
                    // Layer 1: Soft carbon cloud footprint shadow
                    drawCircle(
                        color = Color(0xFF0F172A).copy(alpha = alpha * 0.35f),
                        radius = radius * 1.25f,
                        center = Offset(x + 12f * progress, y + 12f * progress)
                    )
                    
                    // Layer 2: Main billowy volcanic ash body (sleek grey scale)
                    drawCircle(
                        color = Color(0xFF334155).copy(alpha = alpha),
                        radius = radius,
                        center = Offset(x, y)
                    )
                    
                    // Layer 3: Secondary inner dark charcoal soot core
                    drawCircle(
                        color = Color(0xFF1E293B).copy(alpha = alpha * 0.75f),
                        radius = radius * 0.78f,
                        center = Offset(x - 6f * progress, y - 6f * progress)
                    )

                    // Volcanic hot flame glow core within first 35% of the particle's lifespan
                    if (progress < 0.35f) {
                        val flameAlpha = (1f - progress / 0.35f) * 0.48f
                        drawCircle(
                            color = Color(0xFFF97316).copy(alpha = flameAlpha), // Deep burning orange
                            radius = radius * 0.38f,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color(0xFFEF4444).copy(alpha = flameAlpha * 0.5f), // Outer red frame
                            radius = radius * 0.55f,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            // Draw high-speed shrapnel shards rotating
            shrapnelParticles.forEach { particle ->
                val progress = (elapsed.toFloat() / particle.duration).coerceIn(0f, 1f)
                if (progress < 1f) {
                    val x = centerX + particle.destX * progress
                    val y = centerY + particle.destY * progress
                    val alpha = if (progress > 0.8f) (1f - progress) / 0.2f else 1f
                    val size = particle.size

                    val rotationRad = Math.toRadians((particle.rotate * progress).toDouble()).toFloat()
                    val cosR = cos(rotationRad) * size
                    val sinR = sin(rotationRad) * size

                    drawLine(
                        color = particle.color.copy(alpha = alpha),
                        start = Offset(x - cosR, y - sinR),
                        end = Offset(x + cosR, y + sinR),
                        strokeWidth = size
                    )
                }
            }
        }
    }

    if (alarmActive && !finalFlashActive) {
        Box(modifier = Modifier.fillMaxWidth().offset(y = 75.dp).padding(horizontal = 20.dp).align(Alignment.TopCenter)) {
            Surface(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp)), color = Color(0xEE2A0808), shape = RoundedCornerShape(8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "SECURITY BREACH DETECTED", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 1.sp)
                    Text(text = "Subject container hijacked. Drone swarm pulling vector active!", color = Color.White, fontSize = 10.sp, lineHeight = 13.sp, modifier = Modifier.padding(top = 1.dp))
                }
            }
        }
    }

    if (flashActive) Box(modifier = Modifier.fillMaxSize().background(Color.White))
    if (finalFlashActive) Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE0F7FA)))
    Box(modifier = Modifier.fillMaxWidth().height(6.dp).offset(y = scanlineY.dp).background(Color(0x4422D3EE)))

    val bottomPanelAlpha by animateFloatAsState(targetValue = if (fadeOutUi) 0f else 1f, animationSpec = tween(800), label = "bottomPanelAlpha")
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).alpha(bottomPanelAlpha).align(Alignment.BottomCenter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = glitchText, color = glitchColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.graphicsLayer(translationX = glitchX, translationY = glitchY, scaleX = glitchScaleX, alpha = glitchOpacity).padding(bottom = 20.dp))
        Text(text = if (logIndex < bootLogs.size) bootLogs[logIndex] else "Ready to engage hyper-jump.", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f).height(4.dp).background(Color(0xFF1E293B), RoundedCornerShape(2.dp))) {
                Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color(0xFF22D3EE), RoundedCornerShape(2.dp)))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "${(progress * 100).toInt()}%", color = Color(0xFF22D3EE), fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
        }
        Button(
            onClick = { if (isFullyPrepared && !barsBroken) triggerBreach() },
            enabled = isFullyPrepared && !barsBroken,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE), contentColor = Color.Black, disabledContainerColor = Color(0xFF1E293B), disabledContentColor = Color.Gray),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(0.85f).height(48.dp)
        ) {
            Text(text = if (isFullyPrepared) "BREACH KERNEL CONTAINER" else "DECRYPTING LINK PAYLOAD", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}
