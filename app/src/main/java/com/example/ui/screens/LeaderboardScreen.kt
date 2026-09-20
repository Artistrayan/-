package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.ui.components.CoinChip
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
            FriendEntity(1, "Sultan_Khosrow", 95, 12500000, 1),
            FriendEntity(2, "Grandmaster_Cyrus", 88, 8900000, 4),
            FriendEntity(3, "Persian_Knight", 82, 5400000, 3),
            FriendEntity(4, currentUser.username, currentUser.level, currentUser.coins, currentUser.avatarId),
            FriendEntity(5, "Darius_The_Great", 76, 3200000, 2),
            FriendEntity(6, "Nard_Wizard", 64, 1800000, 5)
        ).sortedByDescending { it.coins }
    }

    val displayList = if (selectedTab == 0) globalMockList else (friends + FriendEntity(99, currentUser.username, currentUser.level, currentUser.coins, currentUser.avatarId)).sortedByDescending { it.coins }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TAKHTE NARD LEADERBOARD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0E17))
            )
        },
        containerColor = Color(0xFF0F0E17)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1B1A28),
                contentColor = Color(0xFFFFD700)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("GLOBAL TOP", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("FRIENDS", fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(displayList) { index, player ->
                    val rank = index + 1
                    val isMe = (player.friendName == currentUser.username)

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        borderColor = if (isMe) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
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
                                // Rank Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (rank) {
                                                1 -> Color(0xFFFFD700)
                                                2 -> Color(0xFFC0C0C0)
                                                3 -> Color(0xFFCD7F32)
                                                else -> Color(0xFF2A2A38)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (rank) {
                                            1 -> "🥇"
                                            2 -> "🥈"
                                            3 -> "🥉"
                                            else -> "#$rank"
                                        },
                                        color = if (rank in 1..3) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(getAvatarEmoji(player.avatarId), fontSize = 24.sp)

                                Column {
                                    Text(
                                        text = player.friendName + (if (isMe) " (YOU)" else ""),
                                        color = if (isMe) Color(0xFFFFD700) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text("LVL ${player.level}", color = Color.Gray, fontSize = 11.sp)
                                }
                            }

                            CoinChip(coins = player.coins)
                        }
                    }
                }
            }
        }
    }
}
