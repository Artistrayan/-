package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CoinChip
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    currentCoins: Long,
    onBuyCoins: (Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("COIN & VIP STORE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    CoinChip(coins = currentCoins, modifier = Modifier.padding(end = 12.dp))
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
            item {
                Text("COIN PACKAGES", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            item {
                ShopPackageTile(
                    title = "STARTER POUCH",
                    coinsAmount = 50000L,
                    priceStr = "$0.99",
                    badge = "+20% BONUS",
                    emoji = "🪙",
                    onBuy = {
                        onBuyCoins(50000L)
                        Toast.makeText(context, "🎉 Added +50,000 Coins!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                ShopPackageTile(
                    title = "SULTAN CHEST",
                    coinsAmount = 250000L,
                    priceStr = "$4.99",
                    badge = "BEST VALUE 🔥",
                    emoji = "💰",
                    onBuy = {
                        onBuyCoins(250000L)
                        Toast.makeText(context, "🎉 Added +250,000 Coins!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                ShopPackageTile(
                    title = "ROYAL VAULT",
                    coinsAmount = 1000000L,
                    priceStr = "$14.99",
                    badge = "MEGA PACK 💎",
                    emoji = "👑",
                    onBuy = {
                        onBuyCoins(1000000L)
                        Toast.makeText(context, "🎉 Added +1,000,000 Coins!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ShopPackageTile(
    title: String,
    coinsAmount: Long,
    priceStr: String,
    badge: String,
    emoji: String,
    onBuy: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBuy() },
        cornerRadius = 20.dp,
        borderColor = Color(0xFFFFD700).copy(alpha = 0.4f)
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
                Text(emoji, fontSize = 36.sp)
                Column {
                    Surface(color = Color(0xFFFFD700), shape = RoundedCornerShape(8.dp)) {
                        Text(badge, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                    }
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("+$coinsAmount Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(priceStr, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
