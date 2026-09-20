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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardTheme
import com.example.model.BoardThemes
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardsStoreScreen(
    userLevel: Int,
    equippedBoardId: Int,
    onSelectBoard: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("10 LUXURY BOARDS GALLERY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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
            items(BoardThemes.ALL_THEMES) { theme ->
                val isUnlocked = (userLevel >= theme.unlockLevel)
                val isEquipped = (equippedBoardId == theme.id)

                BoardCard(
                    theme = theme,
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    onEquip = { if (isUnlocked) onSelectBoard(theme.id) }
                )
            }
        }
    }
}

@Composable
fun BoardCard(
    theme: BoardTheme,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderColor = if (isEquipped) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    theme.boardBackgroundColors.map { Color(it) }
                                )
                            )
                            .border(2.dp, Color(theme.woodBorderColor), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(theme.iconEmoji, fontSize = 24.sp)
                    }

                    Column {
                        Text(
                            text = theme.name,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isUnlocked) "UNLOCKED (LVL ${theme.unlockLevel})" else "REQUIRES PLAYER LEVEL ${theme.unlockLevel}",
                            color = if (isUnlocked) Color(0xFFFFD700) else Color.Red.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = theme.description,
                color = Color.LightGray,
                fontSize = 12.sp
            )

            // Board Theme Color Palette Swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Swatches:", color = Color.Gray, fontSize = 10.sp)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(theme.pointColorLight))
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(theme.pointColorDark))
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(theme.accentGlowColor))
                )
            }

            // Action Button
            Button(
                onClick = onEquip,
                enabled = isUnlocked && !isEquipped,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEquipped) Color(0xFF00E5FF) else Color(0xFFFFD700),
                    disabledContainerColor = Color(0xFF2A2A38)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isEquipped) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                        Text("EQUIPPED", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else if (isUnlocked) {
                    Text("EQUIP BOARD", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Text("LOCKED (LVL ${theme.unlockLevel})", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
