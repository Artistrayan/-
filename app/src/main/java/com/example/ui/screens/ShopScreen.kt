package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserEntity
import com.example.ui.components.GlassCard

enum class ShopTab(val title: String, val icon: String) {
    COINS("سکه‌های طلایی", "🪙"),
    GEMS("جم و الماس", "💎"),
    VIP("عضویت VIP", "👑"),
    DICE("تاس‌های لوکس", "🎲")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    user: UserEntity,
    onBuyCoins: (Long) -> Unit,
    onBuyGems: (Long) -> Unit,
    onBuyVip: () -> Unit,
    onSelectDice: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ShopTab.COINS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "خزانه‌داری سلطنتی اژدها",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            "فروشگاه رسمی سکه، الماس و اشتراک ویژه VIP",
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
                    // Current Coins & Gems Chips
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Surface(
                            color = Color(0xFF131A26),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🪙", fontSize = 14.sp)
                                Text(
                                    "${user.coins}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF131A26),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("💎", fontSize = 14.sp)
                                Text(
                                    "${user.gems}",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Hero Treasury Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dragon_shop_vault_1790547070269),
                        contentDescription = "خزانه اژدها",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color(0xFF080B12).copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFFD700),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "جشنواره امپراتوری اژدها 🔥",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "تا ۱۰۰٪ بونوس شارژ رایگان در تمام بسته‌ها",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // 2. Category Tabs Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShopTab.values().forEach { tab ->
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
                                            listOf(Color(0xFF141926), Color(0xFF182030))
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
                                Text(tab.icon, fontSize = 14.sp)
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Tab Contents
            when (selectedTab) {
                ShopTab.COINS -> {
                    item {
                        CoinPackageItem(
                            title = "بسته نوآموز اژدها",
                            coins = 50000L,
                            bonus = "+۱۰,۰۰۰ سکه هدیه",
                            badge = "+20% بونوس",
                            priceToman = "۴۹,۰۰۰ تومان",
                            priceUsd = "$0.99",
                            icon = "🪙",
                            gradientColors = listOf(Color(0xFF0F1E2E), Color(0xFF152A3F)),
                            onBuy = {
                                onBuyCoins(60000L)
                                Toast.makeText(context, "🎉 خرید موفق! ۶۰,۰۰۰ سکه به حساب شما اضافه شد.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        CoinPackageItem(
                            title = "کیسه طلای شاهانه",
                            coins = 250000L,
                            bonus = "+۷۵,۰۰۰ سکه هدیه",
                            badge = "پرفروش‌ترین 🔥",
                            priceToman = "۱۴۹,۰۰۰ تومان",
                            priceUsd = "$2.99",
                            icon = "💰",
                            gradientColors = listOf(Color(0xFF261D0F), Color(0xFF382914)),
                            isFeatured = true,
                            onBuy = {
                                onBuyCoins(325000L)
                                Toast.makeText(context, "🎉 خرید موفق! ۳۲۵,۰۰۰ سکه با بونوس ویژه دریافت شد!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        CoinPackageItem(
                            title = "صندوقچه طلای درباری",
                            coins = 1000000L,
                            bonus = "+۵۰۰,۰۰۰ سکه هدیه",
                            badge = "تخفیف ۵۰٪ ✨",
                            priceToman = "۳۹۹,۰۰۰ تومان",
                            priceUsd = "$7.99",
                            icon = "🏆",
                            gradientColors = listOf(Color(0xFF1A1526), Color(0xFF2A203F)),
                            onBuy = {
                                onBuyCoins(1500000L)
                                Toast.makeText(context, "🎉 خرید موفق! ۱,۵۰۰,۰۰۰ سکه به حساب شما افزوده شد!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        CoinPackageItem(
                            title = "خزانه‌ی بزرگ امپراتور اژدها",
                            coins = 5000000L,
                            bonus = "+۲,۵۰۰,۰۰۰ سکه هدیه رایگان",
                            badge = "لوکس‌ترین بسته 👑",
                            priceToman = "۹۹۰,۰۰۰ تومان",
                            priceUsd = "$18.99",
                            icon = "👑",
                            gradientColors = listOf(Color(0xFF2B1F08), Color(0xFF45300B)),
                            isFeatured = true,
                            onBuy = {
                                onBuyCoins(7500000L)
                                Toast.makeText(context, "👑 درود بر امپراتور! ۷,۵۰۰,۰۰۰ سکه به خزانه‌تان افزوده شد!", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }

                ShopTab.GEMS -> {
                    item {
                        GemPackageItem(
                            title = "کیسه الماس فیروزه‌ای",
                            gems = 150L,
                            bonus = "+۲۵ الماس هدیه",
                            badge = "بسته پایه",
                            priceToman = "۶۹,۰۰۰ تومان",
                            priceUsd = "$1.49",
                            onBuy = {
                                onBuyGems(175L)
                                Toast.makeText(context, "💎 خرید موفق! ۱۷۵ الماس فیروزه‌ای شارژ شد.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        GemPackageItem(
                            title = "صندوقچه زمرد و الماس اژدها",
                            gems = 600L,
                            bonus = "+۱۵۰ الماس هدیه",
                            badge = "ارزش فوق‌العاده ✨",
                            priceToman = "۱۹۹,۰۰۰ تومان",
                            priceUsd = "$3.99",
                            isFeatured = true,
                            onBuy = {
                                onBuyGems(750L)
                                Toast.makeText(context, "💎 خرید موفق! ۷۵۰ الماس فیروزه‌ای به حساب شما افزوده شد.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        GemPackageItem(
                            title = "گنجینه گوهر شاهنشاهی",
                            gems = 2500L,
                            bonus = "+۱,۰۰۰ الماس هدیه ویژه",
                            badge = "بزرگترین بسته 💎",
                            priceToman = "۵۹۰,۰۰۰ تومان",
                            priceUsd = "$11.99",
                            onBuy = {
                                onBuyGems(3500L)
                                Toast.makeText(context, "💎 درود! ۳,۵۰۰ الماس به گنجینه شما افزوده شد!", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }

                ShopTab.VIP -> {
                    item {
                        VipPassCard(
                            isVipActive = user.isVipActive,
                            vipTier = user.vipTier,
                            onBuyVip = {
                                onBuyVip()
                                Toast.makeText(context, "👑 تبریک! اشتراک VIP امپراتور اژدها فعال گردید!", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }

                ShopTab.DICE -> {
                    item {
                        DiceStoreItem(
                            title = "تاس نئونی اژدهای فیروزه‌ای (Cyan Dragon)",
                            description = "بدنه ابسیدین کربنی با خال‌های نئونی فیروزه‌ای و هاله درخشان",
                            styleKey = "CyanDragon",
                            isEquipped = user.selectedDiceStyle == "CyanDragon",
                            price = "رایگان با تم اژدها",
                            icon = "🐲",
                            onSelect = {
                                onSelectDice("CyanDragon")
                                Toast.makeText(context, "🎲 تاس نئونی اژدها با موفقیت تجهیز شد.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        DiceStoreItem(
                            title = "تاس طلای ۲۴ عیار سلطنتی (Imperial Gold)",
                            description = "طلای خالص صیقلی با خال‌های جواهرنشان و بازتاب آینه‌ای",
                            styleKey = "Gold",
                            isEquipped = user.selectedDiceStyle == "Gold",
                            price = "موجود در حساب",
                            icon = "✨",
                            onSelect = {
                                onSelectDice("Gold")
                                Toast.makeText(context, "🎲 تاس طلای ۲۴ عیار با موفقیت تجهیز شد.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        DiceStoreItem(
                            title = "تاس یاقوت آتشین (Crimson Obsidian)",
                            description = "آبنوس آتشین با خال‌های یاقوتی سرخ و دود عرفانی",
                            styleKey = "Crimson",
                            isEquipped = user.selectedDiceStyle == "Crimson",
                            price = "۵۰,۰۰۰ سکه",
                            icon = "🔥",
                            onSelect = {
                                onSelectDice("Crimson")
                                Toast.makeText(context, "🎲 تاس یاقوت آتشین با موفقیت تجهیز شد.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoinPackageItem(
    title: String,
    coins: Long,
    bonus: String,
    badge: String,
    priceToman: String,
    priceUsd: String,
    icon: String,
    gradientColors: List<Color>,
    isFeatured: Boolean = false,
    onBuy: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .border(
                1.5.dp,
                if (isFeatured) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(18.dp)
            )
            .clickable { onBuy() }
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
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1420))
                        .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 28.sp)
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (isFeatured) Color(0xFFFFD700) else Color(0xFF00E5FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                badge,
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${String.format("%,d", coins)} سکه طلا",
                        color = Color(0xFFFFD700),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(bonus, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Buy Button
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFeatured) Color(0xFFFFD700) else Color(0xFF263238)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.8f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        priceToman,
                        color = if (isFeatured) Color.Black else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                    Text(
                        priceUsd,
                        color = if (isFeatured) Color.Black.copy(alpha = 0.7f) else Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GemPackageItem(
    title: String,
    gems: Long,
    bonus: String,
    badge: String,
    priceToman: String,
    priceUsd: String,
    isFeatured: Boolean = false,
    onBuy: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF081826), Color(0xFF0B2438))
                )
            )
            .border(
                1.5.dp,
                if (isFeatured) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(18.dp)
            )
            .clickable { onBuy() }
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
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF08121E))
                        .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 28.sp)
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFF00E5FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                badge,
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${String.format("%,d", gems)} الماس فیروزه",
                        color = Color(0xFF00E5FF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(bonus, color = Color(0xFF69F0AE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(priceToman, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    Text(priceUsd, color = Color.Black.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VipPassCard(
    isVipActive: Boolean,
    vipTier: String,
    onBuyVip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2E200B), Color(0xFF140F05))
                )
            )
            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("👑", fontSize = 36.sp)
                    Column {
                        Text(
                            "اشتراک ویژه VIP امپراتور اژدها",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            if (isVipActive) "وضعیت: فعال ($vipTier)" else "وضعیت: غیرفعال",
                            color = if (isVipActive) Color(0xFF00E676) else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "۳۰ روزه",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFFFD700).copy(alpha = 0.3f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VipBenefitRow("🔥 ضریب ۲ برابری کسب سکه در تمام پیروزی‌ها")
                VipBenefitRow("🐲 آواتار اختصاصی و متحرک اژدهای سلطنتی با قاب طلایی")
                VipBenefitRow("💎 ۱۰۰,۰۰۰ سکه هدیه آنی + ۵۰۰ الماس فیروزه‌ای")
                VipBenefitRow("🛡️ حفاظت از باخت در بازی‌های شرطی تا ۳ دست در روز")
                VipBenefitRow("🎲 دسترسی انحصاری به تمام تخته‌ها و تاس‌های VIP")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onBuyVip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "خرید اشتراک VIP — ۲۹۹,۰۰۰ تومان ($5.99)",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun VipBenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
        Text(text, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun DiceStoreItem(
    title: String,
    description: String,
    styleKey: String,
    isEquipped: Boolean,
    price: String,
    icon: String,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF131824))
            .border(
                1.5.dp,
                if (isEquipped) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(18.dp)
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
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0A0F1A))
                        .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 24.sp)
                }

                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(description, color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(price, color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEquipped) Color(0xFF00E676) else Color(0xFFFFD700)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    if (isEquipped) "فعال ✓" else "انتخاب",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
