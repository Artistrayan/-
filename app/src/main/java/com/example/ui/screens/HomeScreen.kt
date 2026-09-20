package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
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

    // Dialog state controllers
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showDailyGiftDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070B))
    ) {
        // 1. Cinematic 8K Ultra-realistic Background
        Image(
            painter = painterResource(id = R.drawable.luxury_backgammon_splash_1789937987492),
            contentDescription = "Casino Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Clear Ambient Overlay for Maximum Visual Clarity & Richness
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        // 3. Floating Golden Light Particles Animation (AAA Unreal Engine Atmosphere)
        FloatingGoldenParticles()

        // 4. Main Layout Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // TOP BAR: Profile | Logo | Wallet
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT: Profile Avatar with Gold VIP Crown + Username + Level Badge
                GlassCard(
                    modifier = Modifier.clickable { onNavigateToProfile() },
                    cornerRadius = 24.dp,
                    backgroundColor = Color(0xFF14131E).copy(alpha = 0.75f),
                    borderColor = Color(0xFFFFD700).copy(alpha = 0.35f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        // Avatar Circle with Crown Badge on Top
                        Box(contentAlignment = Alignment.TopEnd) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFFFFDF00), Color(0xFF996515))
                                        )
                                    )
                                    .border(2.dp, Color(0xFFFFE082), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getAvatarEmoji(user.avatarId), fontSize = 22.sp)
                            }
                            // Gold VIP Crown Over Avatar
                            Text(
                                text = "👑",
                                fontSize = 14.sp,
                                modifier = Modifier.offset(x = 4.dp, y = (-5).dp)
                            )
                        }

                        // Username & Badges
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = user.username,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    shadow = Shadow(color = Color.Black, blurRadius = 6f)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "VIP",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Surface(
                                    color = Color(0xFF262438),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "LVL ${user.level}",
                                        color = Color(0xFFFFE082),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // CENTER: Luxurious Gold Calligraphy Logo with "HIGH STAKES" Tag
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "تخته نرد حرفه‌ای",
                        color = Color(0xFFFFDF00),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFFFFB300).copy(alpha = 0.8f),
                                blurRadius = 20f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = Color(0xFF0F1420).copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "✦ HIGH STAKES ✦",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // RIGHT: Wallet Balance with Coin Icon + Glowing "شارژ" Button
                GlassCard(
                    cornerRadius = 24.dp,
                    backgroundColor = Color(0xFF14131E).copy(alpha = 0.75f),
                    borderColor = Color(0xFFFFD700).copy(alpha = 0.35f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { onNavigateToShop() }
                        ) {
                            Text("🪙", fontSize = 18.sp)
                            Text(
                                text = "${user.coins} سکه",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 8f))
                            )
                        }

                        // Glowing Charge Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00E676), Color(0xFF00B0FF))
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { onNavigateToShop() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ شارژ",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // ==========================================
            // MAIN CONTENT: 3 Large Modern Game Mode Cards
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Card 1: LIVE ONLINE (Cyan Neon)
                GameModeGlassCard(
                    modifier = Modifier.weight(1f),
                    badge = "LIVE ONLINE",
                    title = "بازی آنلاین دو نفره",
                    subtitle = "رقابت زنده با بازیکنان سراسر جهان",
                    icon = "🌐",
                    neonColor = Color(0xFF00E5FF),
                    onClick = {
                        if (user.coins >= defaultBet) {
                            onStartMatch(defaultBet, GameMode.ONLINE_QUICK)
                        } else {
                            Toast.makeText(context, "سکه کافی نیست! از منوی شارژ سکه تهیه کنید.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Card 2: PRACTICE (Purple Neon)
                GameModeGlassCard(
                    modifier = Modifier.weight(1f),
                    badge = "PRACTICE",
                    title = "بازی با هوش مصنوعی",
                    subtitle = "تقویت مهارت در برابر ربات استاد بزرگ",
                    icon = "🤖",
                    neonColor = Color(0xFFC084FC),
                    onClick = {
                        if (user.coins >= defaultBet) {
                            onStartMatch(defaultBet, GameMode.AI_MEDIUM)
                        } else {
                            Toast.makeText(context, "سکه کافی نیست!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Card 3: HIGH STAKES (Gold Neon)
                GameModeGlassCard(
                    modifier = Modifier.weight(1f),
                    badge = "HIGH STAKES",
                    title = "بازی شرطی / تورنمنت",
                    subtitle = "میزهای سنگین، مسابقات حذفی و جوایز میلیونی",
                    icon = "💎",
                    neonColor = Color(0xFFFFD700),
                    onClick = {
                        Toast.makeText(context, "🏆 تورنمنت به زودی فعال می‌شود!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // ==========================================
            // BOTTOM NAVIGATION BAR
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF141224).copy(alpha = 0.90f),
                                Color(0xFF090812).copy(alpha = 0.96f)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = 0.4f),
                                Color(0xFF00E5FF).copy(alpha = 0.4f),
                                Color(0xFFFFD700).copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
            ) {
                // Top Glowing Accent Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFFFFD700).copy(alpha = 0.8f),
                                    Color(0xFF00E5FF).copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavButton(
                        icon = Icons.Default.Person,
                        label = "پروفایل",
                        accentColor = Color(0xFF00E5FF),
                        onClick = onNavigateToProfile
                    )
                    BottomNavButton(
                        icon = Icons.Default.ShoppingCart,
                        label = "فروشگاه",
                        accentColor = Color(0xFFFFD700),
                        onClick = onNavigateToShop
                    )
                    BottomNavButton(
                        icon = Icons.Default.Settings,
                        label = "تنظیمات",
                        accentColor = Color(0xFFB0BEC5),
                        onClick = { showSettingsDialog = true }
                    )
                    BottomNavButton(
                        icon = Icons.Default.CardGiftcard,
                        label = "هدایا",
                        accentColor = Color(0xFFFF4081),
                        isBadgeActive = true,
                        onClick = { showDailyGiftDialog = true }
                    )
                    BottomNavButton(
                        icon = Icons.Default.History,
                        label = "تاریخچه",
                        accentColor = Color(0xFFC084FC),
                        onClick = { showHistoryDialog = true }
                    )
                    BottomNavButton(
                        icon = Icons.Default.Leaderboard,
                        label = "رتبه‌بندی",
                        accentColor = Color(0xFFFFAB00),
                        onClick = onNavigateToLeaderboard
                    )
                    BottomNavButton(
                        icon = Icons.Default.Headset,
                        label = "پشتیبانی",
                        accentColor = Color(0xFF00E676),
                        onClick = { showSupportDialog = true }
                    )
                }
            }
        }

        // ==========================================
        // DIALOGS: Settings | Gifts | History | Support
        // ==========================================

        // 1. Settings Dialog
        if (showSettingsDialog) {
            LuxurySettingsDialog(
                onDismiss = { showSettingsDialog = false }
            )
        }

        // 2. Daily Gift Dialog
        if (showDailyGiftDialog) {
            DailyGiftDialog(
                onClaim = {
                    onClaimDaily { success, amount ->
                        if (success) {
                            Toast.makeText(context, "🎉 هدیه +$amount سکه دریافت شد!", Toast.LENGTH_LONG).show()
                            showDailyGiftDialog = false
                        } else {
                            Toast.makeText(context, "⏳ هدیه روزانه امروز را قبلاً دریافت کرده‌اید!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = { showDailyGiftDialog = false }
            )
        }

        // 3. Match History Dialog
        if (showHistoryDialog) {
            MatchHistoryModal(
                user = user,
                onDismiss = { showHistoryDialog = false }
            )
        }

        // 4. Support Dialog
        if (showSupportDialog) {
            VipSupportDialog(
                onDismiss = { showSupportDialog = false }
            )
        }
    }
}

// =========================================================================
// ULTRA-PREMIUM GAME MODE GLASS CARD (Cinematic Depth + Glowing Neon Borders)
// =========================================================================
@Composable
fun GameModeGlassCard(
    modifier: Modifier = Modifier,
    badge: String,
    title: String,
    subtitle: String,
    icon: String,
    neonColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161525).copy(alpha = 0.85f),
                        Color(0xFF08070F).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        neonColor.copy(alpha = 0.9f),
                        neonColor.copy(alpha = 0.25f),
                        neonColor.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
    ) {
        // Subtle top illumination spotlight inside the card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(neonColor.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Badge Tag at Top
            Surface(
                color = neonColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, neonColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = badge,
                    color = neonColor,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Big Central 3D Icon with Radiant Halo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(neonColor.copy(alpha = 0.35f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 46.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = neonColor,
                            blurRadius = 25f
                        )
                    )
                )
            }

            // Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 10f))
                )
                Text(
                    text = subtitle,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            // Glowing Launch Button
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(neonColor, neonColor.copy(alpha = 0.7f))
                        )
                    )
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp), spotColor = neonColor)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "شروع بازی",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// =========================================================================
// BOTTOM NAVIGATION ITEM (Comfortable touch target + label + optional badge)
// =========================================================================
@Composable
fun BottomNavButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    isBadgeActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .width(62.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 28.dp),
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            if (isBadgeActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(x = 3.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// =========================================================================
// FLOATING PARTICLES CANVAS (Subtle Golden Sparkles & Volumetric Atmosphere)
// =========================================================================
@Composable
fun FloatingGoldenParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val particlePositions = listOf(
            Offset(0.12f, 0.25f),
            Offset(0.28f, 0.65f),
            Offset(0.42f, 0.35f),
            Offset(0.58f, 0.75f),
            Offset(0.72f, 0.20f),
            Offset(0.85f, 0.60f),
            Offset(0.93f, 0.40f),
            Offset(0.20f, 0.80f),
            Offset(0.68f, 0.85f),
            Offset(0.35f, 0.15f)
        )

        particlePositions.forEachIndexed { index, normPos ->
            val offsetFactor = sin(phase + index.toFloat()) * 18f
            val px = normPos.x * w + (offsetFactor * 0.5f)
            val py = (normPos.y * h - (phase * 15f)) % h
            val finalY = if (py < 0) py + h else py
            val alpha = (0.25f + 0.35f * sin(phase + index.toFloat())).coerceIn(0.1f, 0.8f)

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha),
                radius = (2.5f + (index % 3) * 1.5f),
                center = Offset(px, finalY)
            )
        }
    }
}

// =========================================================================
// MODALS / DIALOGS: Settings, Gifts, History, Support
// =========================================================================

@Composable
fun LuxurySettingsDialog(onDismiss: () -> Unit) {
    var soundEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .width(420.dp)
                .wrapContentHeight(),
            cornerRadius = 24.dp,
            backgroundColor = Color(0xFF141320),
            borderColor = Color(0xFFFFD700).copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚙️ تنظیمات بازی",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                SettingRow(title = "صدای بازی و تاس", state = soundEnabled, onToggle = { soundEnabled = it })
                SettingRow(title = "موسیقی متن لوکس", state = musicEnabled, onToggle = { musicEnabled = it })
                SettingRow(title = "لرزش (هپتیک)", state = vibrationEnabled, onToggle = { vibrationEnabled = it })

                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بستن و ذخیره", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingRow(title: String, state: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = state,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color(0xFFFFD700)
            )
        )
    }
}

@Composable
fun DailyGiftDialog(onClaim: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .width(380.dp)
                .wrapContentHeight(),
            cornerRadius = 24.dp,
            backgroundColor = Color(0xFF1B1425),
            borderColor = Color(0xFFFF4081).copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🎁", fontSize = 48.sp)
                Text(
                    text = "صندوق پاداش روزانه",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "هر ۲۴ ساعت می‌توانید ۵,۰۰۰ سکه رایگان دریافت کنید!",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("دریافت ۵,۰۰۰ سکه هدیه", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MatchHistoryModal(user: UserEntity, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .width(420.dp)
                .wrapContentHeight(),
            cornerRadius = 24.dp,
            backgroundColor = Color(0xFF13131F),
            borderColor = Color(0xFFC084FC).copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📜 آمار و تاریخچه بازی‌ها",
                    color = Color(0xFFC084FC),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val matchCount = user.totalMatches
                    val winCount = user.wins
                    val winRate = if (matchCount > 0) (winCount * 100) / matchCount else 0
                    StatTile("تعداد بازی", "$matchCount")
                    StatTile("پیروزی‌ها", "$winCount", Color(0xFF00E676))
                    StatTile("درصد برد", "$winRate%", Color(0xFFFFD700))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC084FC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بستن", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatTile(title: String, value: String, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Color.Gray, fontSize = 11.sp)
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VipSupportDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .width(380.dp)
                .wrapContentHeight(),
            cornerRadius = 24.dp,
            backgroundColor = Color(0xFF101918),
            borderColor = Color(0xFF00E676).copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🎧", fontSize = 42.sp)
                Text(
                    text = "پشتیبانی اختصاصی VIP",
                    color = Color(0xFF00E676),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "پشتیبانی ۲۴ ساعته آنلاین برای تمامی سوالات و پیگیری امور مالی و تورنمنت‌ها.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ارسال تیکت / تماس با پشتیبان", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
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
