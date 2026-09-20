package com.example.data

import android.content.Context
import com.example.model.BoardThemes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val matchDao = db.matchDao()
    private val friendDao = db.friendDao()

    val userProfile: Flow<UserEntity> = userDao.getUserProfileFlow().map {
        it ?: UserEntity()
    }

    val matchHistory: Flow<List<MatchEntity>> = matchDao.getMatchHistoryFlow()
    val friendsList: Flow<List<FriendEntity>> = friendDao.getFriendsFlow()

    suspend fun initDefaultDataIfNeeded() {
        var user = userDao.getUserProfile()
        if (user == null) {
            user = UserEntity(
                username = "Takhte_Master_${(1000..9999).random()}",
                coins = 25000,
                level = 1,
                xp = 0
            )
            userDao.insertOrUpdateUser(user)

            // Seed initial friends for social leaderboard
            friendDao.insertFriend(FriendEntity(friendName = "Khosrow_King", level = 45, coins = 850000, avatarId = 2))
            friendDao.insertFriend(FriendEntity(friendName = "Persia_Pro", level = 28, coins = 320000, avatarId = 3))
            friendDao.insertFriend(FriendEntity(friendName = "Cyrus_The_Great", level = 72, coins = 2400000, avatarId = 4))
            friendDao.insertFriend(FriendEntity(friendName = "Darius_Nard", level = 12, coins = 85000, avatarId = 5))
        }
    }

    suspend fun updateUsername(newName: String, avatarId: Int) {
        val current = userDao.getUserProfile() ?: UserEntity()
        userDao.insertOrUpdateUser(current.copy(username = newName, avatarId = avatarId))
    }

    suspend fun selectBoard(boardId: Int) {
        userDao.setSelectedBoard(boardId)
    }

    suspend fun addCoins(amount: Long) {
        val current = userDao.getUserProfile() ?: UserEntity()
        val newCoins = (current.coins + amount).coerceAtLeast(0)
        userDao.updateCoins(newCoins)
    }

    suspend fun claimDailyReward(): Pair<Boolean, Long> {
        val current = userDao.getUserProfile() ?: UserEntity()
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        if (now - current.lastDailyClaimTime >= oneDayMs || current.lastDailyClaimTime == 0L) {
            val rewardCoins = 5000L
            val updated = current.copy(
                coins = current.coins + rewardCoins,
                lastDailyClaimTime = now
            )
            userDao.insertOrUpdateUser(updated)
            return Pair(true, rewardCoins)
        }
        return Pair(false, 0L)
    }

    suspend fun recordMatchResult(
        opponentName: String,
        bet: Long,
        isWin: Boolean,
        winType: String,
        movesCount: Int,
        boardName: String
    ) {
        val current = userDao.getUserProfile() ?: UserEntity()

        // Calculate coin change: Winner takes pot (2 * bet) minus 5% house fee
        val houseFee = (bet * 2 * 0.05).toLong()
        val coinDelta = if (isWin) (bet - houseFee) else -bet
        val newCoins = (current.coins + coinDelta).coerceAtLeast(0)

        // Calculate XP gain
        val baseXP = if (isWin) 250L else 75L
        val winMultiplier = when (winType) {
            "GAMMON" -> 1.5
            "BACKGAMMON" -> 2.0
            else -> 1.0
        }
        val gainedXP = (baseXP * winMultiplier).toLong()
        val totalXP = current.xp + gainedXP

        // Level calculation (1000 XP per level up to level 100)
        val calculatedLevel = ((totalXP / 1000) + 1).toInt().coerceIn(1, 100)

        val updatedUser = current.copy(
            coins = newCoins,
            xp = totalXP,
            level = calculatedLevel,
            totalMatches = current.totalMatches + 1,
            wins = current.wins + (if (isWin) 1 else 0),
            losses = current.losses + (if (!isWin) 1 else 0),
            gammons = current.gammons + (if (isWin && winType != "NORMAL") 1 else 0)
        )

        userDao.insertOrUpdateUser(updatedUser)
        matchDao.insertMatch(
            MatchEntity(
                opponentName = opponentName,
                bet = bet,
                isWin = isWin,
                winType = winType,
                boardName = boardName,
                movesCount = movesCount
            )
        )
    }
}
