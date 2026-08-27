package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotaEyeState
import com.example.data.repository.SyncStatus
import com.example.ui.components.NotaAvatar
import com.example.ui.components.ViNoteTransactionTile
import com.example.ui.theme.ViNoteError
import com.example.ui.theme.ViNotePrimary
import com.example.ui.theme.ViNoteSecondaryFixed
import com.example.ui.theme.ViNoteSoftPink
import com.example.ui.theme.ViNoteSurface
import com.example.ui.theme.ViNoteSurfaceContainerLow
import com.example.ui.theme.ViNoteSurfaceContainerLowest
import com.example.ui.theme.ViNoteTextPrimary
import com.example.ui.theme.ViNoteTextSecondary
import com.example.viewmodel.ActivityFilter
import com.example.viewmodel.ViNoteViewModel

@Composable
fun ActivityScreen(
    viewModel: ViNoteViewModel,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.activityFilter.collectAsState()
    val notaConfig by viewModel.notaConfig.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "sync_angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViNoteSurface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.statusBarsPadding())

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activity",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ViNoteTextPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cloud Sync button
                        IconButton(
                            onClick = { viewModel.syncExpensesWithFirestore() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ViNoteSecondaryFixed.copy(alpha = 0.5f))
                                .size(38.dp)
                                .testTag("sync_firestore_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync with Firestore",
                                tint = ViNotePrimary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .then(
                                        if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(rotationAngle)
                                        else Modifier
                                    )
                            )
                        }

                        // Clear All History button
                        if (allTransactions.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearHistoryDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ViNoteError.copy(alpha = 0.1f))
                                    .size(38.dp)
                                    .testTag("clear_history_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear History",
                                    tint = ViNoteError,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(ViNoteSurfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ViNoteTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = "Search transactions...",
                                    color = ViNoteTextSecondary,
                                    fontSize = 15.sp
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input")
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = ViNoteTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filters Row: All, Income, Expense
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        label = "All",
                        selected = currentFilter == ActivityFilter.ALL,
                        onClick = { viewModel.setActivityFilter(ActivityFilter.ALL) },
                        testTag = "filter_all"
                    )
                    FilterChip(
                        label = "Income",
                        selected = currentFilter == ActivityFilter.INCOME,
                        onClick = { viewModel.setActivityFilter(ActivityFilter.INCOME) },
                        testTag = "filter_income"
                    )
                    FilterChip(
                        label = "Expense",
                        selected = currentFilter == ActivityFilter.EXPENSE,
                        onClick = { viewModel.setActivityFilter(ActivityFilter.EXPENSE) },
                        testTag = "filter_expense"
                    )
                }
            }

            // Group: Today
            val todayItems = filteredTransactions.filter { it.timeLabel.startsWith("Today") || it.timeLabel.startsWith("Just now") }
            if (todayItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Today",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ViNoteTextSecondary,
                        letterSpacing = 0.05.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(todayItems, key = { it.id }) { item ->
                    ViNoteTransactionTile(
                        transaction = item,
                        onClick = {
                            viewModel.selectTransactionDetail(item)
                        }
                    )
                }
            }

            // Group: Yesterday and earlier
            val earlierItems = filteredTransactions.filter { !it.timeLabel.startsWith("Today") && !it.timeLabel.startsWith("Just now") }
            if (earlierItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Earlier",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ViNoteTextSecondary,
                        letterSpacing = 0.05.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(earlierItems, key = { it.id }) { item ->
                    ViNoteTransactionTile(
                        transaction = item,
                        onClick = {
                            viewModel.selectTransactionDetail(item)
                        }
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NotaAvatar(
                            size = 80.dp,
                            eyeState = NotaEyeState.CURIOUS,
                            baseColor = notaConfig.baseColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No transactions found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ViNoteTextSecondary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp)) // bottom navigation space
            }
        }

        // Floating Micro Nota Presence (Bottom Right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-90).dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color(0x1F171827)
                )
                .clip(RoundedCornerShape(50))
                .background(ViNoteSurfaceContainerLowest)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(ViNoteSoftPink),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "● ●",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = ViNoteTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Watching...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ViNoteTextSecondary
                )
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Clear All Transactions?",
                    fontWeight = FontWeight.Bold,
                    color = ViNoteTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently delete your entire transaction history locally and from Cloud Firestore sync.",
                    color = ViNoteTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        viewModel.clearAllTransactions()
                    },
                    modifier = Modifier.testTag("confirm_clear_all_btn")
                ) {
                    Text("Clear All", color = ViNoteError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = ViNoteTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) ViNotePrimary else Color.Transparent)
            .then(
                if (!selected) {
                    Modifier.border(1.5.dp, ViNotePrimary, RoundedCornerShape(50))
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 9.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else ViNotePrimary
        )
    }
}

