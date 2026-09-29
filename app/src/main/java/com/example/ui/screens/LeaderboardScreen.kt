package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FriendEntity
import com.example.data.UserEntity
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    currentUser: UserEntity,
    friends: List<FriendEntity>,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Global, 1 = Friends

    val globalMockList = remember {
        listOf(
            FriendEntity(1, "کوروش_بزرگ", 95, 12500000, 4),
            FriendEntity(2, "خسرو_پرویز", 88, 8900000, 2),
            FriendEntity(3, "شاهین_اصفهان", 82, 5400000, 3),
            FriendEntity(4, currentUser.username, currentUser.level, currentUser.coins, currentUser.avatarId),
            FriendEntity(5, "داریوش_شیراز", 76, 3200000, 5),
            FriendEntity(6, "سهراب_یل", 64, 1800000, 6)
        ).sortedByDescending { it.coins }
    }

    val displayList = if (selectedTab == 0) globalMockList else (friends + FriendEntity(99, currentUser.username, currentUser.level, currentUser.coins, currentUser.avatarId)).sortedByDescending { it.coins }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "رتبه‌بندی قهرمانان تخته‌نرد",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            "لیگ هفتگی و برترین بازیکنان کشور",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF080B12))
            )
        },
        containerColor = Color(0xFF080B12)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tab Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TabPill(
                    title = "رتبه‌بندی سراسری 🌐",
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                TabPill(
                    title = "لیست دوستان 👥",
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
            }

            // Top 3 Podium
            if (displayList.size >= 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 2nd Place (Silver)
                    PodiumPillar(player = displayList[1], rank = 2, color = Color(0xFFC0C0C0), height = 100.dp)
                    // 1st Place (Gold)
                    PodiumPillar(player = displayList[0], rank = 1, color = Color(0xFFFFD700), height = 125.dp)
                    // 3rd Place (Bronze)
                    PodiumPillar(player = displayList[2], rank = 3, color = Color(0xFFCD7F32), height = 85.dp)
                }
            }

            // List of Players
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(displayList) { index, player ->
                    val rank = index + 1
                    val isMe = (player.friendName == currentUser.username)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isMe) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF281E08), Color(0xFF161C2C))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF101420), Color(0xFF141926))
                                    )
                                }
                            )
                            .border(
                                1.dp,
                                if (isMe) Color(0xFFFFD700) else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
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
                                    color = when (rank) {
                                        1 -> Color(0xFFFFD700)
                                        2 -> Color(0xFFC0C0C0)
                                        3 -> Color(0xFFCD7F32)
                                        else -> Color(0xFF202636)
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "$rank",
                                            color = if (rank <= 3) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Text(getAvatarEmoji(player.avatarId), fontSize = 22.sp)

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            player.friendName,
                                            color = if (isMe) Color(0xFFFFD700) else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (isMe) {
                                            Surface(
                                                color = Color(0xFFFFD700),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "شما",
                                                    color = Color.Black,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text("سطح ${player.level}", color = Color.Gray, fontSize = 11.sp)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🪙", fontSize = 13.sp)
                                Text(
                                    "${String.format("%,d", player.coins)}",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabPill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFD700), Color(0xFFFF9100))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF131826), Color(0xFF181E2E))
                    )
                }
            )
            .border(
                1.dp,
                if (isSelected) Color(0xFFFFE082) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PodiumPillar(
    player: FriendEntity,
    rank: Int,
    color: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(getAvatarEmoji(player.avatarId), fontSize = 24.sp)
        Text(
            player.friendName.take(9),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(85.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.3f))
                    )
                )
                .border(1.dp, color, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    when (rank) {
                        1 -> "🥇 اول"
                        2 -> "🥈 دوم"
                        else -> "🥉 سوم"
                    },
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
                Text(
                    "${player.coins / 1000}K 🪙",
                    color = Color.Black.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
