package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    HOME,
    GAME,
    BOARDS_STORE,
    PROFILE,
    LEADERBOARD,
    SHOP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0E17)
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val userProfile by viewModel.userProfile.collectAsState()
                    val gameState by viewModel.gameState.collectAsState()
                    val aiCommentary by viewModel.aiCommentary.collectAsState()
                    val chatMessages by viewModel.chatMessages.collectAsState()
                    val matchHistory by viewModel.matchHistory.collectAsState()
                    val friendsList by viewModel.friendsList.collectAsState()

                    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

                    // Auto switch to GAME screen when gameState is active
                    LaunchedEffect(gameState) {
                        if (gameState != null) {
                            currentScreen = AppScreen.GAME
                        }
                    }

                    when (currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                user = userProfile,
                                onStartMatch = { bet, mode ->
                                    viewModel.startNewGame(bet, mode)
                                    currentScreen = AppScreen.GAME
                                },
                                onNavigateToBoards = { currentScreen = AppScreen.BOARDS_STORE },
                                onNavigateToProfile = { currentScreen = AppScreen.PROFILE },
                                onNavigateToLeaderboard = { currentScreen = AppScreen.LEADERBOARD },
                                onNavigateToShop = { currentScreen = AppScreen.SHOP },
                                onClaimDaily = { onResult ->
                                    viewModel.claimDailyCoins(onResult)
                                }
                            )
                        }

                        AppScreen.GAME -> {
                            val activeState = gameState
                            if (activeState == null) {
                                currentScreen = AppScreen.HOME
                            } else {
                                GameScreen(
                                    state = activeState,
                                    equippedBoardId = userProfile.selectedBoardId,
                                    commentary = aiCommentary,
                                    chatMessages = chatMessages,
                                    canUndo = viewModel.canUndo,
                                    onRollClick = { viewModel.rollDice() },
                                    onPointClick = { pt -> viewModel.onPointClicked(pt) },
                                    onBarClick = { color -> viewModel.onBarClicked(color) },
                                    onDoubleOffer = { viewModel.offerDouble() },
                                    onUndoClick = { viewModel.undoLastMove() },
                                    onConfirmTurn = { viewModel.confirmTurn() },
                                    onSendChat = { msg -> viewModel.sendChatMessage(msg) },
                                    onExitGame = {
                                        viewModel.exitGame()
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            }
                        }

                        AppScreen.BOARDS_STORE -> {
                            BoardsStoreScreen(
                                userLevel = userProfile.level,
                                equippedBoardId = userProfile.selectedBoardId,
                                onSelectBoard = { boardId ->
                                    viewModel.selectBoardTheme(boardId)
                                },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.PROFILE -> {
                            ProfileScreen(
                                user = userProfile,
                                matchHistory = matchHistory,
                                onUpdateProfile = { name, avatarId ->
                                    viewModel.updateProfile(name, avatarId)
                                },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.LEADERBOARD -> {
                            LeaderboardScreen(
                                currentUser = userProfile,
                                friends = friendsList,
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.SHOP -> {
                            ShopScreen(
                                currentCoins = userProfile.coins,
                                onBuyCoins = { amount ->
                                    viewModel.addCoinsPackage(amount)
                                },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}
