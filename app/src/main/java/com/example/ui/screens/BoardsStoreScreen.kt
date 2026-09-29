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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
                title = {
                    Column {
                        Text(
                            "تالار تخته‌های سلطنتی",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            "کلکسیون ۱۰ تخته لوکس تاریخی و اسطوره‌ای",
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

                BoardCardPersian(
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
fun BoardCardPersian(
    theme: BoardTheme,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (isEquipped) Color(0xFF1E180A) else Color(0xFF111624),
                        Color(0xFF0A0D17)
                    )
                )
            )
            .border(
                1.5.dp,
                if (isEquipped) Color(0xFFFFD700) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(20.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    theme.boardBackgroundColors.map { Color(it) }
                                )
                            )
                            .border(2.dp, Color(theme.woodBorderColor), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(theme.iconEmoji, fontSize = 26.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = theme.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (theme.id == 1) {
                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "اصلی 👑",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isUnlocked) "قابل دسترسی (سطح ${theme.unlockLevel})" else "نیازمند رسیدن به سطح ${theme.unlockLevel}",
                            color = if (isUnlocked) Color(0xFFFFD700) else Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isEquipped) {
                    Surface(
                        color = Color(0xFF00E676).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                            Text("انتخاب شده", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (!isUnlocked) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                            Text("قفل", color = Color.Red, fontSize = 11.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = onEquip,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("انتخاب تخته", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = theme.description,
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Color Palette Chips Preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("پالت رنگی:", color = Color.Gray, fontSize = 10.sp)
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(theme.pointColorLight)).border(0.5.dp, Color.White, CircleShape))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(theme.pointColorDark)).border(0.5.dp, Color.White, CircleShape))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(theme.accentGlowColor)).border(0.5.dp, Color.White, CircleShape))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(theme.checkerWhiteColors.first())).border(0.5.dp, Color.White, CircleShape))
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(theme.checkerBlackColors.first())).border(0.5.dp, Color.White, CircleShape))
            }
        }
    }
}
