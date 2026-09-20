package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.MatchEntity
import com.example.data.UserEntity
import com.example.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserEntity,
    matchHistory: List<MatchEntity>,
    onUpdateProfile: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    val winRate = if (user.totalMatches > 0) {
        ((user.wins.toDouble() / user.totalMatches.toDouble()) * 100).toInt()
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PLAYER PROFILE & STATS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- USER HEADER CARD ---
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
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
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFFFFD700), Color(0xFFB8860B))
                                        )
                                    )
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getAvatarEmoji(user.avatarId), fontSize = 32.sp)
                            }

                            Column {
                                Text(user.username, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(getRankTitle(user.level), color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("LVL ${user.level} (XP: ${user.xp % 1000}/1000)", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFFFFD700))
                        }
                    }
                }
            }

            // --- STATS GRID ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("WIN RATE", "$winRate%", Color(0xFF00E5FF), modifier = Modifier.weight(1f))
                    StatCard("TOTAL MATCHES", "${user.totalMatches}", Color(0xFFFFD700), modifier = Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("WINS", "${user.wins}", Color.Green, modifier = Modifier.weight(1f))
                    StatCard("LOSSES", "${user.losses}", Color.Red, modifier = Modifier.weight(1f))
                    StatCard("GAMMONS", "${user.gammons}", Color(0xFFC084FC), modifier = Modifier.weight(1f))
                }
            }

            // --- MATCH HISTORY SECTION ---
            item {
                Text("RECENT MATCH HISTORY", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (matchHistory.isEmpty()) {
                item {
                    Text("No matches played yet. Play a match to build history!", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                items(matchHistory) { match ->
                    MatchHistoryTile(match)
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
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
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, cornerRadius = 16.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MatchHistoryTile(match: MatchEntity) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(match.timestamp))

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(if (match.isWin) "🏆" else "💔", fontSize = 20.sp)
                Column {
                    Text("Vs ${match.opponentName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${match.boardName} • $dateStr", color = Color.Gray, fontSize = 11.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (match.isWin) "+🪙${(match.bet * 1.9).toLong()}" else "-🪙${match.bet}",
                    color = if (match.isWin) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(match.winType, color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    var name by remember { mutableStateOf(currentName) }
    var selectedAvatar by remember { mutableStateOf(currentAvatarId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2A),
        title = { Text("Edit Player Profile", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Username") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Text("Select Avatar Emoji:", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { id ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedAvatar == id) Color(0xFFFFD700) else Color(0xFF2A2A38))
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
                Text("SAVE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        }
    )
}

fun getRankTitle(level: Int): String {
    return when {
        level >= 80 -> "👑 Sultan of Nard"
        level >= 50 -> "🦅 Legendary Grandmaster"
        level >= 30 -> "⚔️ Master Player"
        level >= 10 -> "❇️ Skilled Apprentice"
        else -> "🎲 Backgammon Novice"
    }
}
