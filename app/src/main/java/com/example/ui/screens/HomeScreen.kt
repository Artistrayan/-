package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.config.CustomGameThemeConfig
import com.example.data.UserEntity
import com.example.model.GameMode
import com.example.ui.components.GlassCard
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: UserEntity,
    onStartMatch: (Long, GameMode) -> Unit,
    onNavigateToBoards: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToShop: () -> Unit,
    onClaimDaily: ((Boolean, Long) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val defaultBet = 100L

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showDailyGiftDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14121A))
    ) {
        // 1. Cinematic Background Art (Bright and clear)
        Image(
            painter = painterResource(id = CustomGameThemeConfig.homeBackgroundDrawable),
            contentDescription = "Casino Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Light Ambient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.22f)
                        )
                    )
                )
        )

        // 3. Floating Golden Light Particles
        FloatingGoldenParticles()

        // 4. Main Layout (Exact Match to Reference Image 2)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // =========================================================================
            // TOP: ORNATE TITLE & CROWN HEADER (Matching Image 2)
            // =========================================================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Crown / Filigree ornament above title
                Text("👑 ✦ 👑", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "تخته نرد",
                    color = Color(0xFFFFD700),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFFFF9100),
                            blurRadius = 20f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "بازی سنتی، رقابت هوشمند",
                    color = Color(0xFFFFE082),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // =========================================================================
            // CENTER: 3-COLUMN MASTER LAYOUT (Left Game Cards | Center Board | Right Profile & Daily Reward)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SIDE: 3 Stacked Game Mode Cards (Matching Image 2)
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    HomeGameModeCard(
                        title = "بازی جدید",
                        subtitle = "شروع بازی سریع",
                        icon = "⚔️",
                        accentColor = Color(0xFFFFD700),
                        onClick = { onStartMatch(defaultBet, GameMode.ONLINE_QUICK) }
                    )
                    HomeGameModeCard(
                        title = "بازی آنلاین",
                        subtitle = "حریف واقعی",
                        icon = "👥",
                        accentColor = Color(0xFF00E5FF),
                        onClick = { onStartMatch(defaultBet, GameMode.ONLINE_QUICK) }
                    )
                    HomeGameModeCard(
                        title = "بازی با کامپیوتر",
                        subtitle = "هوش مصنوعی",
                        icon = "🤖",
                        accentColor = Color(0xFFC084FC),
                        onClick = { onStartMatch(defaultBet, GameMode.AI_MEDIUM) }
                    )
                }

                // CENTER: Atmospheric Wood Board Preview Vignette
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF110E08).copy(alpha = 0.5f))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚜️ میز امپراتوری اژدها ⚜️",
                            color = Color(0xFFFFD700).copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // RIGHT SIDE: Profile Card & Daily Reward Card (Matching Image 2)
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 1. Profile Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1C170C), Color(0xFF0A0D14))
                                )
                            )
                            .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .clickable { onNavigateToProfile() }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFFFFD700), Color(0xFF8B6508))
                                        )
                                    )
                                    .border(2.dp, Color(0xFFFFE082), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getAvatarEmoji(user.avatarId), fontSize = 26.sp)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = user.username,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "سطح ${user.level}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { ((user.xp % 1000) / 1000f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFFFFD700),
                                    trackColor = Color(0xFF1E2433)
                                )
                            }
                        }
                    }

                    // 2. Daily Reward Card (جایزه روزانه)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1C170C), Color(0xFF0A0D14))
                                )
                            )
                            .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("جایزه روزانه", color = Color(0xFFFFE082), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🪙", fontSize = 20.sp)
                                Text(
                                    text = "+50",
                                    color = Color(0xFFFFD700),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Button(
                                onClick = {
                                    onClaimDaily { success, amount ->
                                        if (success) {
                                            Toast.makeText(context, "🎉 جایزه روزانه +$amount دریافت شد!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "⏳ امروز جایزه خود را دریافت کرده‌اید!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("دریافت", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // BOTTOM NAVIGATION BAR (Matching Reference Image 2: فروشگاه، مأموریت‌ها، رتبه‌بندی، تنظیمات)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF10131D).copy(alpha = 0.95f))
                    .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeBottomNavItem(icon = "🛒", label = "فروشگاه", onClick = onNavigateToShop)
                HomeBottomNavItem(icon = "📜", label = "مأموریت‌ها", onClick = { showDailyGiftDialog = true })
                HomeBottomNavItem(icon = "🏆", label = "رتبه‌بندی", onClick = onNavigateToLeaderboard)
                HomeBottomNavItem(icon = "⚙️", label = "تنظیمات", onClick = { showSettingsDialog = true })
            }
        }
    }

    // Dialogs
    if (showSettingsDialog) {
        LuxurySettingsDialog(onDismiss = { showSettingsDialog = false })
    }
    if (showDailyGiftDialog) {
        DailyGiftDialog(
            onClaim = {
                onClaimDaily { success, amount ->
                    if (success) {
                        Toast.makeText(context, "🎁 هدیه مأموریت +$amount دریافت شد!", Toast.LENGTH_SHORT).show()
                        showDailyGiftDialog = false
                    } else {
                        Toast.makeText(context, "⏳ قبلاً دریافت شده است!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showDailyGiftDialog = false }
        )
    }
}

@Composable
fun HomeGameModeCard(
    title: String,
    subtitle: String,
    icon: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF1C170C), Color(0xFF0A0D14))
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.8f), accentColor.copy(alpha = 0.3f))
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun HomeBottomNavItem(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(icon, fontSize = 18.sp)
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FloatingGoldenParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val particleCount = 20

        for (i in 0 until particleCount) {
            val x = (i * 137.5f) % width
            val y = (height - ((animProgress * height + i * 50f) % height))
            val radius = (i % 3 + 1).dp.toPx()
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = ((i % 5 + 1) * 0.12f)),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun LuxurySettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141722),
        title = { Text("تنظیمات بازی", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• صدا و موسیقی: فعال", color = Color.White, fontSize = 12.sp)
                Text("• لرزش گوشی: روشن", color = Color.White, fontSize = 12.sp)
                Text("• نسخه بازی: 1.0.4 پرو", color = Color.Gray, fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))) {
                Text("تایید", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun DailyGiftDialog(onClaim: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141722),
        title = { Text("جایزه و مأموریت روزانه", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎁 هدیه ویژه ورود روزانه آماده دریافت است!", color = Color.White, fontSize = 13.sp)
                Text("مقدار پاداش: 🪙 50 سکه", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(onClick = onClaim, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))) {
                Text("دریافت جایزه", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("بعداً", color = Color.Gray) }
        }
    )
}

fun getAvatarEmoji(avatarId: Int): String {
    return when (avatarId) {
        1 -> "👑"
        2 -> "🦁"
        3 -> "🦅"
        4 -> "🐉"
        5 -> "💎"
        else -> "⚔️"
    }
}
