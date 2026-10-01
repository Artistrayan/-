package com.example.ui.screens

import android.widget.Toast
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
                            "سطح ${user.level} • ${getProfileRankTitle(user.level)}",
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
            // =========================================================================
            // MASTER-DESIGN PROFILE HEADER (Matching Reference Image 1)
            // =========================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT / MAIN PROFILE CARD
                    Box(
                        modifier = Modifier
                            .weight(1.6f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1C170C), Color(0xFF0A0D14))
                                )
                            )
                            .border(
                                1.8.dp,
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFFFFD700))
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Avatar & Username & Level
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 24K Gold Ornate Medallion Avatar Frame
                                Box(
                                    modifier = Modifier
                                        .size(74.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(0xFFFFD700), Color(0xFF6B4F0F))
                                            )
                                        )
                                        .border(2.5.dp, Color(0xFFFFE082), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(getProfileAvatarEmoji(user.avatarId), fontSize = 36.sp)
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = user.username,
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "سطح ${user.level}",
                                        color = Color(0xFFFFD700),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Golden XP Progress Bar
                                    LinearProgressIndicator(
                                        progress = { ((user.xp % 1000) / 1000f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .width(130.dp)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = Color(0xFFFFD700),
                                        trackColor = Color(0xFF1E2433)
                                    )
                                }
                            }

                            // Stats Row: بازی‌ها | بردها | درصد برد
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileStatItem(label = "بازی‌ها", value = "${user.totalMatches}", icon = "💬")
                                ProfileStatItem(label = "بردها", value = "${user.wins}", icon = "👤")
                                ProfileStatItem(label = "درصد برد", value = "$winRate%", icon = "🎯")
                            }

                            // Badges Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("نشان‌ها", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("👑", fontSize = 18.sp)
                                    Text("⭐", fontSize = 18.sp)
                                    Text("🦅", fontSize = 18.sp)
                                    Text("💠", fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    // RIGHT: BEST SCORE & GLOBAL RANK CARD (Matching Reference Image 1)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(210.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1C170C), Color(0xFF0A0D14))
                                )
                            )
                            .border(
                                1.8.dp,
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFF8B6508))
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("بهترین امتیاز", color = Color(0xFFFFE082), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${1500 + user.wins * 10}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            HorizontalDivider(
                                color = Color(0xFFFFD700).copy(alpha = 0.3f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("رتبه جهانی", color = Color(0xFFFFE082), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "#${(150 - user.level * 2).coerceAtLeast(1)}",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Surface(
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📈", fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // RECENT ACTIVITY SECTION (Matching Reference Image 1)
            // =========================================================================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF111522))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "فعالیت اخیر",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        ActivityLogRow(icon = "🎲", title = "برد در بازی آنلاین", time = "۲۵ ساعت پیش", reward = "+120 امتیاز")
                        ActivityLogRow(icon = "🏆", title = "شرکت در مسابقه تورنمنت", time = "۴۰ روز پیش", reward = "+350 امتیاز")
                        ActivityLogRow(icon = "👥", title = "دعوت از دوستان", time = "۱۵ روز پیش", reward = "+50 امتیاز")
                    }
                }
            }

            // =========================================================================
            // TABS & DETAILED CONTENT
            // =========================================================================
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
                                "دوستان آنلاین و دعوت",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    Toast.makeText(context, "🔗 لینک دعوت شما در کلیپ‌بورد کپی شد!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("دعوت دوست +", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (friends.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "لیست دوستان خالی است. با دعوت دوستان هدیه بگیرید!",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(friends) { friend ->
                            FriendRowCard(friend = friend)
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = user.username,
            currentAvatarId = user.avatarId,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newAvatarId ->
                onUpdateProfile(newName, newAvatarId)
                showEditDialog = false
                Toast.makeText(context, "✨ پروفایل با موفقیت به‌روزرسانی شد!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ProfileStatItem(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(icon, fontSize = 11.sp)
            Text(label, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun ActivityLogRow(icon: String, title: String, time: String, reward: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(icon, fontSize = 16.sp)
            Column {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(time, color = Color.Gray, fontSize = 10.sp)
            }
        }
        Surface(
            color = Color(0xFFFFD700).copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                reward,
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    accentColor: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF141926), Color(0xFF0D101A))
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.6f), accentColor.copy(alpha = 0.2f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Color.Gray, fontSize = 11.sp)
                Text(value, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text(emoji, fontSize = 26.sp)
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
            .background(
                if (isUnlocked) Color(0xFF141926) else Color(0xFF0D101A).copy(alpha = 0.6f)
            )
            .border(
                1.dp,
                if (isUnlocked) badgeColor.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) badgeColor.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.2f)
                        )
                        .border(
                            1.dp,
                            if (isUnlocked) badgeColor else Color.Gray,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 20.sp)
                }

                Column {
                    Text(
                        title,
                        color = if (isUnlocked) Color.White else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        desc,
                        color = if (isUnlocked) Color.LightGray else Color.DarkGray,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                color = if (isUnlocked) badgeColor.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isUnlocked) "باز است ✓" else "قفل 🔒",
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
    val dateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(match.timestamp))
    val isWin = match.isWin

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131722))
            .border(
                1.dp,
                if (isWin) Color(0xFF00E676).copy(alpha = 0.4f) else Color(0xFFFF5252).copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = if (isWin) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (isWin) "🏆" else "💥", fontSize = 16.sp)
                    }
                }

                Column {
                    Text(
                        text = if (isWin) "پیروزی در برابر ${match.opponentName}" else "شکست در برابر ${match.opponentName}",
                        color = if (isWin) Color(0xFF00E676) else Color(0xFFFF5252),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "شرط: ${match.bet} سکه • $dateStr", color = Color.Gray, fontSize = 10.sp)
                }
            }

            Text(
                text = if (isWin) "+${match.bet}" else "-${match.bet}",
                color = if (isWin) Color(0xFFFFD700) else Color(0xFFFF5252),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun FriendRowCard(friend: FriendEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131722))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(14.dp)
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
                Text(getProfileAvatarEmoji(friend.avatarId), fontSize = 24.sp)
                Column {
                    Text(friend.friendName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("سطح ${friend.level} • ${friend.coins / 1000}K سکه", color = Color(0xFFFFD700), fontSize = 11.sp)
                }
            }

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text("چالش ⚔️", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentAvatarId: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var selectedAvatar by remember { mutableIntStateOf(currentAvatarId) }
    val avatars = listOf(1, 2, 3, 4, 5, 6)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121622),
        title = { Text("ویرایش پروفایل امپراتور", color = Color(0xFFFFD700), fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("نام نمایشی", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("انتخاب آواتار سلطنتی:", color = Color.White, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    avatars.forEach { avId ->
                        val isSelected = (selectedAvatar == avId)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFFFD700).copy(alpha = 0.3f) else Color(0xFF1E2433))
                                .border(2.dp, if (isSelected) Color(0xFFFFD700) else Color.Transparent, CircleShape)
                                .clickable { selectedAvatar = avId },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getProfileAvatarEmoji(avId), fontSize = 22.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nameInput.isNotBlank()) onSave(nameInput, selectedAvatar) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                Text("ذخیره تغییرات", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color.Gray)
            }
        }
    )
}

fun getProfileRankTitle(level: Int): String {
    return when {
        level >= 90 -> "شاهنشاه بی‌رقیب 👑"
        level >= 70 -> "امپراتور بزرگ اژدها 🐉"
        level >= 50 -> "استاد اعظم شاهانه ⚜️"
        level >= 30 -> "سردار نامدار ⚔️"
        level >= 15 -> "شوالیه دلیر 🛡️"
        else -> "نوآموز درباری 📜"
    }
}

fun getProfileAvatarEmoji(avatarId: Int): String {
    return when (avatarId) {
        1 -> "👑"
        2 -> "🦁"
        3 -> "🦅"
        4 -> "🐉"
        5 -> "💎"
        else -> "⚔️"
    }
}
