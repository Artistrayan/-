package com.example.config

import androidx.compose.ui.graphics.Color
import com.example.R

/**
 * =========================================================================================
 * 🎨 فایل تنظیمات سفارشی پوسته، تخته، تاس‌ها، مهره‌ها و پس‌زمینه (Custom Game Theme Config)
 * =========================================================================================
 * 
 * کاربر گرامی:
 * شما می‌توانید تمامی تصاویر، رنگ‌ها و طرح‌های بازی تخته‌نرد را در این فایل به دلخواه خود
 * تغییر دهید. مقادیر با پیش‌فرض فوق‌العاده باکیفیت طلایی-اژدها تنظیم شده‌اند.
 *
 * 📌 راهنمای تغییر رنگ‌ها (کدهای هگز رنگ):
 * - فرمت رنگ‌ها: 0xAARRGGBB است (مثلاً 0xFFFFD700 برای طلایی، 0xFF00E5FF برای آبی فیروزه‌ای).
 * - برای شفافیت کامل FF در ابتدای کد استفاده کنید.
 */
object CustomGameThemeConfig {

    // =====================================================================================
    // 🖼️ ۱. تصاویر پس‌زمینه بازی و منوی اصلی (Background Wallpapers)
    // =====================================================================================
    /**
     * تصویر پس‌زمینه سالن بازی (صفحه مسابقه تخته‌نرد).
     * می‌توانید عکس‌های دلخواه خود را در پوشه res/drawable قرار داده و آیدی آن را اینجا بگذارید.
     */
    var gameBackgroundDrawable: Int = R.drawable.img_dragon_vip_lounge_1790546607385

    /**
     * تصویر پس‌زمینه صفحه اصلی (Home Screen).
     */
    var homeBackgroundDrawable: Int = R.drawable.luxury_backgammon_splash_1789937987492

    /**
     * میزان تیرگی/روشنایی لایه سایه‌روشن پس‌زمینه (بین 0.0f تا 1.0f).
     * عدد کوچکتر = پس‌زمینه روشن‌تر و شفاف‌تر.
     */
    var backgroundVignetteDarkness: Float = 0.25f


    // =====================================================================================
    // 🎲 ۲. تنظیمات و رنگ‌های تاس‌های ۳D (3D Dice Appearance)
    // =====================================================================================
    /**
     * رنگ بدنه تاس (گرادیانت ۳ رنگی برای حالت سه‌بعدی).
     */
    var diceBodyGradient: List<Long> = listOf(
        0xFF262E3E, // بالای تاس (روشن‌تر)
        0xFF171B26, // وسط تاس
        0xFF0C0E14  // پایین تاس (تیره‌تر)
    )

    /**
     * رنگ نقطه‌های روی تاس (Pips / Dots).
     */
    var dicePipColor: Long = 0xFF00E5FF // فیروزه‌ای نئونی (یا 0xFFFFD700 برای طلایی)

    /**
     * رنگ کادر و حاشیه نئونی دور تاس.
     */
    var diceBorderColor: Long = 0xFF00E5FF

    /**
     * رنگ درخشش (Glow) تاس هنگام پرتاب.
     */
    var diceGlowColor: Long = 0xFF00E5FF

    /**
     * رنگ تاس‌های استفاده‌شده در این نوبت.
     */
    var diceUsedColor: Long = 0xFF556070


    // =====================================================================================
    // 🔘 ۳. رنگ و طرح مهره‌های تخته‌نرد (Checkers Customization)
    // =====================================================================================
    /**
     * رنگ مهره‌های بازیکن اول (شما / رنگ سفید-فیروزه‌ای).
     * گرادیانت ۲ رنگی جهت ایجاد افکت برجسته سه‌بعدی و براق.
     */
    var player1CheckerGradient: List<Long> = listOf(
        0xFF00E5FF, // رنگ روی مهره
        0xFF0052CC  // رنگ لبه و سایه مهره
    )
    var player1CheckerBorderColor: Long = 0xFF80DEEA
    var player1CheckerIconEmoji: String = "🐉" // آیکون حکاکی شده روی مهره (🐉, 👑, ⭐, 🦅, ...)

    /**
     * رنگ مهره‌های بازیکن دوم (حریف / رنگ مشکی-طلایی ۲۴ عیار).
     */
    var player2CheckerGradient: List<Long> = listOf(
        0xFFFFD700, // طلایی درخشان
        0xFFB8860B  // طلایی متالیک تیره
    )
    var player2CheckerBorderColor: Long = 0xFFFFE082
    var player2CheckerIconEmoji: String = "🐉"


    // =====================================================================================
    // 🪵 ۴. طرح و رنگ‌های تخته‌نرد (Board & Points Styling)
    // =====================================================================================
    /**
     * رنگ چوب یا قاب دور تا دور تخته‌نرد.
     */
    var boardWoodBorderColor: Long = 0xFF181C26

    /**
     * گرادیانت کف تخته‌نرد (زمین بازی).
     */
    var boardFloorGradient: List<Long> = listOf(
        0xFF0F141C, // مرکز تخته
        0xFF080B10  // گوشه‌های تخته
    )

    /**
     * رنگ مثلث‌های روشن تخته (Points - Light).
     */
    var pointColorLight: Long = 0xFF00E5FF // فیروزه‌ای نئونی

    /**
     * رنگ مثلث‌های تیره تخته (Points - Dark).
     */
    var pointColorDark: Long = 0xFFFFD700 // طلایی سلطنتی

    /**
     * رنگ لولاها و تزئینات طلایی وسط تخته (Center Bar Hinges).
     */
    var centerBarHingeColor: Long = 0xFFFFD700

    /**
     * رنگ حکاکی و درخشش اژدهای روی تخته.
     */
    var dragonArtworkColorLeft: Long = 0xFF00E5FF  // اژدهای سمت چپ (فیروزه‌ای)
    var dragonArtworkColorRight: Long = 0xFFFFD700 // اژدهای سمت راست (طلایی)


    // =====================================================================================
    // ⚡ توابع کمکی جهت دریافت رنگ‌ها با فرمت Compose Color
    // =====================================================================================
    val composeDicePipColor: Color get() = Color(dicePipColor)
    val composeDiceBorderColor: Color get() = Color(diceBorderColor)
    val composeDiceGlowColor: Color get() = Color(diceGlowColor)
    val composeDiceUsedColor: Color get() = Color(diceUsedColor)
    val composeDiceBodyColors: List<Color> get() = diceBodyGradient.map { Color(it) }

    val composeP1CheckerColors: List<Color> get() = player1CheckerGradient.map { Color(it) }
    val composeP1BorderColor: Color get() = Color(player1CheckerBorderColor)

    val composeP2CheckerColors: List<Color> get() = player2CheckerGradient.map { Color(it) }
    val composeP2BorderColor: Color get() = Color(player2CheckerBorderColor)

    val composeBoardWoodColor: Color get() = Color(boardWoodBorderColor)
    val composeBoardFloorColors: List<Color> get() = boardFloorGradient.map { Color(it) }
    val composePointLightColor: Color get() = Color(pointColorLight)
    val composePointDarkColor: Color get() = Color(pointColorDark)
    val composeHingeColor: Color get() = Color(centerBarHingeColor)
    val composeDragonLeftColor: Color get() = Color(dragonArtworkColorLeft)
    val composeDragonRightColor: Color get() = Color(dragonArtworkColorRight)
}
