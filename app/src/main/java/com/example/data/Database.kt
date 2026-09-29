package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "Master_Nard",
    val avatarId: Int = 1,
    val customAvatarUri: String? = null,
    val coins: Long = 50000,
    val gems: Long = 250,
    val level: Int = 8,
    val xp: Long = 3450,
    val rating: Int = 1540,
    val wins: Int = 38,
    val losses: Int = 12,
    val gammons: Int = 14,
    val totalMatches: Int = 50,
    val winStreak: Int = 5,
    val selectedBoardId: Int = 1,
    val selectedDiceStyle: String = "CyanDragon",
    val vipTier: String = "VIP طلایی",
    val isVipActive: Boolean = true,
    val lastDailyClaimTime: Long = 0
)

@Entity(tableName = "match_history")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opponentName: String,
    val bet: Long,
    val isWin: Boolean,
    val winType: String,
    val boardName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val movesCount: Int
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendName: String,
    val level: Int,
    val coins: Long,
    val avatarId: Int,
    val isOnline: Boolean = true
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE user_profile SET coins = :newCoins WHERE id = 1")
    suspend fun updateCoins(newCoins: Long)

    @Query("UPDATE user_profile SET gems = :newGems WHERE id = 1")
    suspend fun updateGems(newGems: Long)

    @Query("UPDATE user_profile SET selectedBoardId = :boardId WHERE id = 1")
    suspend fun setSelectedBoard(boardId: Int)

    @Query("UPDATE user_profile SET selectedDiceStyle = :diceStyle WHERE id = 1")
    suspend fun setSelectedDiceStyle(diceStyle: String)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 50")
    fun getMatchHistoryFlow(): Flow<List<MatchEntity>>

    @Insert
    suspend fun insertMatch(match: MatchEntity)
}

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY level DESC")
    fun getFriendsFlow(): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE id = :friendId")
    suspend fun deleteFriend(friendId: Long)
}

@Database(entities = [UserEntity::class, MatchEntity::class, FriendEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun matchDao(): MatchDao
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "backgammon_takhte_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
