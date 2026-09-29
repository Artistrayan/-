package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FriendEntity
import com.example.data.MatchEntity
import com.example.data.UserEntity
import com.example.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ProfileTab(val title: String, val icon: String) {
    STATS("آمار", "📊"),
    ACHIEVEMENTS("نشان‌ها", "🎖️"),
    HISTORY("تاریخچه", "📜"),
    FRIENDS("دوستان", "👥")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserEntity,
    matchHistory: List<MatchEntity>,
    friends: List<FriendEntity> = emptyList(),
    onUpdateProfile: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ProfileTab.STATS) }
    var showEditDialog by remember { mutableStateOf(false) }

    val winRate = if (user.totalMatches > 0) {
        ((user.wins.toDouble() / user.totalMatches.toDouble()) * 100).toInt()
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "پروفایل و افتخارات امپراتور",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            "سطح ${user.level} • ${getRankTitle(user.level)}",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B2333))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF080B12))
            )
        },
        containerColor = Color(0xFF080B12)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 1. Imperial Profile Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E180B), Color(0xFF0D0F17))
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFD700), Color(0xFF00E5FF), Color(0xFFFFD700))
                            ),
                            RoundedCornerShape(22.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Avatar in Golden Dragon Medallion
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color(0xFFFFD700), Color(0xFF8B6508))
                                                )
                                            )
                                            .border(2.5.dp, Color(0xFFFFE082), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(getAvatarEmoji(user.avatarId), fontSize = 34.sp)
                                    }
                                    Surface(
                                        color = Color(0xFFFFD700),
                                        shape = CircleShape,
                                        modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                                    ) {
                                        Text("👑", fontSize = 12.sp, modifier = Modifier.padding(2.dp))
                                    }
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            user.username,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Surface(
                                            color = Color(0xFFFFD700),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                user.vipTier,
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        getRankTitle(user.level),
                                        color = Color(0xFFFFD700),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "ریتینگ قدرتی: ${user.rating} MMR",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Wallet Quick Stats
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪙", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "${user.coins}",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💎", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "${user.gems}",
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // XP Level Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("پیشرفت سطح ${user.level}", color = Color.Gray, fontSize = 11.sp)
                                Text("${user.xp % 1000} / 1000 XP", color = Color(0xFFFFE082), fontSize = 11.sp)
                            }
                            LinearProgressIndicator(
                                progress = { ((user.xp % 1000) / 1000f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFFFD700),
                                trackColor = Color(0xFF1E2433)
                            )
                        }
                    }
                }
            }

            // 2. Tabs Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileTab.values().forEach { tab ->
                        val isSelected = (selectedTab == tab)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFFFD700), Color(0xFFFF9100))
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF121622), Color(0xFF171D2C))
                                        )
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFFFE082) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(tab.icon, fontSize = 13.sp)
                                Text(
                                    tab.title,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Tab Contents
            when (selectedTab) {
                ProfileTab.STATS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatBox("درصد برد (Win Rate)", "$winRate%", Color(0xFF00E5FF), "🎯", Modifier.weight(1f))
                            StatBox("کل بازی‌ها", "${user.totalMatches}", Color(0xFFFFD700), "⚔️", Modifier.weight(1f))
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatBox("پیروزی‌ها", "${user.wins}", Color(0xFF00E676), "🏆", Modifier.weight(1f))
                            StatBox("شکست‌ها", "${user.losses}", Color(0xFFFF5252), "💔", Modifier.weight(1f))
                            StatBox("مارش‌ها (Gammon)", "${user.gammons}", Color(0xFFC084FC), "⚡", Modifier.weight(1f))
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatBox("برد پیاپی (Streak)", "${user.winStreak} دست", Color(0xFFFFAB00), "🔥", Modifier.weight(1f))
                            StatBox("ریتینگ رقابتی", "${user.rating} MMR", Color(0xFF00B0FF), "🎖️", Modifier.weight(1f))
                        }
                    }
                }

                ProfileTab.ACHIEVEMENTS -> {
                    item {
                        Text(
                            "نشان‌های افتخار و مدال‌های اژدها",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        AchievementTile(
                            title = "قهرمان فصلی (Seasonal Champion)",
                            desc = "کسب رتبه اول در تورنمنت بزرگ جام اژدها",
                            icon = "👑",
                            isUnlocked = true,
                            badgeColor = Color(0xFFFFD700)
                        )
                    }

                    item {
                        AchievementTile(
                            title = "شکارچی اژدها (Dragon Slayer)",
                            desc = "۱۰ پیروزی متوالی در برابر حریفان قدرتمند",
                            icon = "🐲",
                            isUnlocked = true,
                            badgeColor = Color(0xFF00E5FF)
                        )
                    }

                    item {
                        AchievementTile(
                            title = "استاد دوبل (Doubling Master)",
                            desc = "پیروزی در دست با مکعب دوبرابرکننده ۶۴ برابری",
                            icon = "🎲",
                            isUnlocked = true,
                            badgeColor = Color(0xFFFF9100)
                        )
                    }

                    item {
                        AchievementTile(
                            title = "برنده ۵۰ بازی (50-Game Winner)",
                            desc = "ثبت ۵۰ پیروزی در کارنامه مسابقات",
                            icon = "⚔️",
                            isUnlocked = user.wins >= 30,
                            badgeColor = Color(0xFF00E676)
                        )
                    }

                    item {
                        AchievementTile(
                            title = "حریف سرسخت (Tough Competitor)",
                            desc = "ورود مجدد مهره از بار و بردن بازی در شرایط سخت",
                            icon = "🛡️",
                            isUnlocked = true,
                            badgeColor = Color(0xFFB388FF)
                        )
                    }

                    item {
                        AchievementTile(
                            title = "ثروتمند درباری (VIP High Roller)",
                            desc = "کسب بیش از ۱,۰۰۰,۰۰۰ سکه در بازی‌های سنگین",
                            icon = "💎",
                            isUnlocked = user.coins >= 50000,
                            badgeColor = Color(0xFFFFD700)
                        )
                    }
                }

                ProfileTab.HISTORY -> {
                    item {
                        Text(
                            "سابقه مسابقات اخیر",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (matchHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "هنوز مسابقه‌ای ثبت نشده است. اولین بازی خود را شروع کنید!",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(matchHistory) { match ->
                            MatchHistoryTilePersian(match)
                        }
                    }
                }

                ProfileTab.FRIENDS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "لیست همراهان و رقبا",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "ارسال روزانه سکه رایگان 🎁",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    val dummyFriends = if (friends.isNotEmpty()) friends else listOf(
                        FriendEntity(friendName = "خسرو_پرویز", level = 45, coins = 1850000, avatarId = 2, isOnline = true),
                        FriendEntity(friendName = "شاهین_اصفهان", level = 34, coins = 920000, avatarId = 3, isOnline = true),
                        FriendEntity(friendName = "کوروش_بزرگ", level = 72, coins = 4800000, avatarId = 4, isOnline = false),
                        FriendEntity(friendName = "داریوش_شیراز", level = 26, coins = 430000, avatarId = 5, isOnline = true),
                        FriendEntity(friendName = "سهراب_یل", level = 19, coins = 210000, avatarId = 6, isOnline = false)
                    )

                    items(dummyFriends) { friend ->
                        FriendItemTile(
                            friend = friend,
                            onChallenge = {
                                Toast.makeText(context, "⚔️ دعوت‌نامه مسابقه برای ${friend.friendName} ارسال شد!", Toast.LENGTH_SHORT).show()
                            },
                            onSendGift = {
                                Toast.makeText(context, "🎁 ۱,۰۰۰ سکه هدیه به ${friend.friendName} اهدا شد!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialogPersian(
            currentName = user.username,
            currentAvatarId = user.avatarId,
            onDismiss = { showEditDialog = false },
            onSave = { name, avatarId ->
                onUpdateProfile(name, avatarId)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131826))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AchievementTile(
    title: String,
    desc: String,
    icon: String,
    isUnlocked: Boolean,
    badgeColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF121724))
            .border(
                1.dp,
                if (isUnlocked) badgeColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isUnlocked) badgeColor.copy(alpha = 0.2f) else Color(0xFF1C2230))
                        .border(1.5.dp, if (isUnlocked) badgeColor else Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 24.sp)
                }

                Column {
                    Text(
                        title,
                        color = if (isUnlocked) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(desc, color = Color.Gray, fontSize = 11.sp)
                }
            }

            Surface(
                color = if (isUnlocked) badgeColor.copy(alpha = 0.2f) else Color(0xFF222B3D),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isUnlocked) badgeColor else Color.Gray
                )
            ) {
                Text(
                    if (isUnlocked) "کسب شده ✓" else "قفل",
                    color = if (isUnlocked) badgeColor else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun MatchHistoryTilePersian(match: MatchEntity) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(match.timestamp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF121724))
            .border(
                1.dp,
                if (match.isWin) Color(0xFF00E676).copy(alpha = 0.35f) else Color(0xFFFF5252).copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (match.isWin) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (match.isWin) "🏆" else "💔", fontSize = 20.sp)
                }

                Column {
                    Text("نبرد با ${match.opponentName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${match.boardName} • $dateStr", color = Color.Gray, fontSize = 10.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (match.isWin) "+${(match.bet * 1.9).toLong()} سکه" else "-${match.bet} سکه",
                    color = if (match.isWin) Color(0xFF00E676) else Color(0xFFFF5252),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                Text(match.winType, color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FriendItemTile(
    friend: FriendEntity,
    onChallenge: () -> Unit,
    onSendGift: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131826))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222C40)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getAvatarEmoji(friend.avatarId), fontSize = 22.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (friend.isOnline) Color(0xFF00E676) else Color.Gray)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                }

                Column {
                    Text(friend.friendName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("سطح ${friend.level} • ${if (friend.isOnline) "آنلاین 🟢" else "آفلاین"}", color = Color.Gray, fontSize = 11.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onSendGift,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283C)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("🎁 هدیه", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onChallenge,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("⚔️ چالش", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EditProfileDialogPersian(
    currentName: String,
    currentAvatarId: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedAvatar by remember { mutableStateOf(currentAvatarId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121726),
        title = {
            Text(
                "ویرایش مشخصات امپراتور",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام کاربری سلطنتی") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Text("انتخاب آواتار اژدها و درباری:", color = Color.Gray, fontSize = 12.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..6).forEach { id ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (selectedAvatar == id) Color(0xFFFFD700) else Color(0xFF1F263B))
                                .border(1.5.dp, if (selectedAvatar == id) Color.White else Color.Transparent, CircleShape)
                                .clickable { selectedAvatar = id },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getAvatarEmoji(id), fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onSave(name, selectedAvatar)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                Text("ذخیره تغییرات", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = Color.Gray) }
        }
    )
}

fun getRankTitle(level: Int): String {
    return when {
        level >= 70 -> "👑 امپراتور اژدهای تخته"
        level >= 50 -> "🦅 استاد بزرگ افسانه‌ای"
        level >= 30 -> "⚔️ قهرمان دربار سلطنتی"
        level >= 15 -> "❇️ استاد چیره‌دست تاس‌ها"
        else -> "🎲 پیشگام تخته‌نرد"
    }
}
