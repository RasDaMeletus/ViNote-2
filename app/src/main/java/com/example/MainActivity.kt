package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ViNoteBottomNavigation
import com.example.ui.components.ViNoteNavTab
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.CreateGoalDialog
import com.example.ui.screens.CustomizeNotaScreen
import com.example.ui.screens.EWalletsScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeScreen
import com.example.ui.screens.NotaAssistantScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ScanReceiptScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionConfirmDialog
import com.example.ui.screens.TransactionDetailSheet
import com.example.ui.screens.VoiceInputScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ViNoteMintSuccess
import com.example.ui.theme.ViNotePrimary
import com.example.viewmodel.ViNoteViewModel

enum class ActiveScreen {
    MAIN_TABS,
    ADD_TRANSACTION,
    VOICE_INPUT,
    SCAN_RECEIPT,
    E_WALLETS,
    CUSTOMIZE_NOTA,
    SETTINGS,
    ONBOARDING
}

class MainActivity : ComponentActivity() {
    private val viewModel: ViNoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ViNoteApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ViNoteApp(viewModel: ViNoteViewModel) {
    var currentScreen by remember { mutableStateOf(ActiveScreen.MAIN_TABS) }
    var currentTab by remember { mutableStateOf(ViNoteNavTab.HOME) }
    var showCreateGoalDialog by remember { mutableStateOf(false) }

    val pendingTx by viewModel.pendingTransaction.collectAsState()
    val selectedDetailTx by viewModel.selectedTransactionDetail.collectAsState()
    val bannerText by viewModel.bannerNotification.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            ActiveScreen.ONBOARDING -> {
                OnboardingScreen(
                    onGetStarted = { currentScreen = ActiveScreen.MAIN_TABS }
                )
            }
            ActiveScreen.ADD_TRANSACTION -> {
                AddTransactionScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = ActiveScreen.MAIN_TABS },
                    onNavigateToScan = { currentScreen = ActiveScreen.SCAN_RECEIPT },
                    onNavigateToVoice = { currentScreen = ActiveScreen.VOICE_INPUT }
                )
            }
            ActiveScreen.VOICE_INPUT -> {
                VoiceInputScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = ActiveScreen.MAIN_TABS }
                )
            }
            ActiveScreen.SCAN_RECEIPT -> {
                ScanReceiptScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = ActiveScreen.MAIN_TABS }
                )
            }
            ActiveScreen.E_WALLETS -> {
                EWalletsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = ActiveScreen.MAIN_TABS }
                )
            }
            ActiveScreen.CUSTOMIZE_NOTA -> {
                CustomizeNotaScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = ActiveScreen.MAIN_TABS }
                )
            }
            ActiveScreen.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = ActiveScreen.MAIN_TABS },
                    onSignOut = { currentScreen = ActiveScreen.ONBOARDING }
                )
            }
            ActiveScreen.MAIN_TABS -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Active Tab Content
                    when (currentTab) {
                        ViNoteNavTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAdd = { currentScreen = ActiveScreen.ADD_TRANSACTION },
                                onNavigateToScan = { currentScreen = ActiveScreen.SCAN_RECEIPT },
                                onNavigateToVoice = { currentScreen = ActiveScreen.VOICE_INPUT },
                                onNavigateToActivity = { currentTab = ViNoteNavTab.ACTIVITY },
                                onNavigateToSettings = { currentScreen = ActiveScreen.SETTINGS }
                            )
                        }
                        ViNoteNavTab.ACTIVITY -> {
                            ActivityScreen(
                                viewModel = viewModel,
                                onNavigateToDetail = { }
                            )
                        }
                        ViNoteNavTab.NOTA -> {
                            NotaAssistantScreen(
                                viewModel = viewModel,
                                onNavigateToVoice = { currentScreen = ActiveScreen.VOICE_INPUT },
                                onNavigateToCustomizeNota = { currentScreen = ActiveScreen.CUSTOMIZE_NOTA }
                            )
                        }
                        ViNoteNavTab.GOALS -> {
                            GoalsScreen(
                                viewModel = viewModel,
                                onNavigateToCreateGoal = { showCreateGoalDialog = true },
                                onNavigateToSettings = { currentScreen = ActiveScreen.SETTINGS }
                            )
                        }
                        ViNoteNavTab.ME -> {
                            MeScreen(
                                viewModel = viewModel,
                                onNavigateToCustomizeNota = { currentScreen = ActiveScreen.CUSTOMIZE_NOTA },
                                onNavigateToEWallets = { currentScreen = ActiveScreen.E_WALLETS },
                                onNavigateToSettings = { currentScreen = ActiveScreen.SETTINGS }
                            )
                        }
                    }

                    // Floating Bottom Navigation Bar
                    ViNoteBottomNavigation(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // Pending Transaction Confirmation Dialog
        pendingTx?.let { tx ->
            TransactionConfirmDialog(
                transaction = tx,
                viewModel = viewModel,
                onDismiss = { viewModel.dismissPendingTransaction() }
            )
        }

        // Transaction Detail Sheet (View & Delete)
        selectedDetailTx?.let { tx ->
            TransactionDetailSheet(
                transaction = tx,
                viewModel = viewModel,
                onDismiss = { viewModel.selectTransactionDetail(null) }
            )
        }

        // Create Goal Sheet
        if (showCreateGoalDialog) {
            CreateGoalDialog(
                viewModel = viewModel,
                onDismiss = { showCreateGoalDialog = false }
            )
        }

        // In-App Toast/Banner Notification
        AnimatedVisibility(
            visible = bannerText != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            bannerText?.let { text ->
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = Color(0x1F171827)
                        )
                        .clip(RoundedCornerShape(50))
                        .background(ViNotePrimary)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
