package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.NotaEyeState
import com.example.ui.components.FormatUtils
import com.example.ui.components.NotaAvatar
import com.example.ui.theme.ViNoteMintSuccess
import com.example.ui.theme.ViNoteOnPrimary
import com.example.ui.theme.ViNotePrimary
import com.example.ui.theme.ViNoteSecondaryFixed
import com.example.ui.theme.ViNoteSoftPink
import com.example.ui.theme.ViNoteSurface
import com.example.ui.theme.ViNoteSurfaceContainer
import com.example.ui.theme.ViNoteSurfaceContainerLow
import com.example.ui.theme.ViNoteSurfaceContainerLowest
import com.example.ui.theme.ViNoteTextPrimary
import com.example.ui.theme.ViNoteTextSecondary
import com.example.ui.theme.ViNoteWarmYellow
import com.example.viewmodel.ViNoteViewModel

@Composable
fun NotaAssistantScreen(
    viewModel: ViNoteViewModel,
    onNavigateToVoice: () -> Unit,
    onNavigateToCustomizeNota: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isNotaTyping by viewModel.isNotaTyping.collectAsState()
    val notaConfig by viewModel.notaConfig.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(chatMessages.size, isNotaTyping) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size + 1)
        }
    }

    val latestEye = if (isNotaTyping) NotaEyeState.THINKING else {
        chatMessages.lastOrNull { !it.isUser }?.eyeState ?: notaConfig.eyeState
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViNoteSurface)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. TOP HEADER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NotaAvatar(
                        size = 38.dp,
                        eyeState = latestEye,
                        baseColor = notaConfig.baseColor,
                        accessory = notaConfig.accessory,
                        isAnimated = true,
                        onClick = { viewModel.cycleNotaExpression() }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NoTa",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ViNoteTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(ViNoteSecondaryFixed.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI Companion",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ViNotePrimary
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isNotaTyping) ViNoteWarmYellow else ViNoteMintSuccess)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isNotaTyping) "Analyzing finances..." else "Ready to assist you",
                                fontSize = 12.sp,
                                color = ViNoteTextSecondary
                            )
                        }
                    }
                }

                // Top Right Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Customize Nota appearance shortcut
                    IconButton(
                        onClick = onNavigateToCustomizeNota,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ViNoteSurfaceContainerLowest)
                            .border(1.dp, Color(0x1F747789), CircleShape)
                            .testTag("nota_customize_shortcut_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Customize Nota",
                            tint = ViNotePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Reset conversation
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ViNoteSurfaceContainerLowest)
                            .border(1.dp, Color(0x1F747789), CircleShape)
                            .testTag("nota_clear_chat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat",
                            tint = ViNoteTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. CONVERSATIONAL STREAM (LAZY COLUMN)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp)
            ) {
                // HERO STAGE: Interactive Mascot & Financial Pulse Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Large Avatar with tap interaction
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                        ) {
                            NotaAvatar(
                                size = 110.dp,
                                eyeState = latestEye,
                                baseColor = notaConfig.baseColor,
                                accessory = notaConfig.accessory,
                                showSparkle = true,
                                isAnimated = true,
                                onClick = { viewModel.cycleNotaExpression() }
                            )
                        }

                        // Speech bubble above pulse metrics
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(ViNoteSurfaceContainerLowest)
                                .border(1.dp, Color(0x1F747789), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "“Hey Farras! 🌟 Tap me anytime or ask below!”",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ViNoteTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Financial Pulse Mini Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PulseMetricBadge(
                                label = "Safe Money",
                                value = FormatUtils.formatRupiah(viewModel.safeMoney),
                                emoji = "🛡️",
                                badgeColor = ViNoteMintSuccess.copy(alpha = 0.15f),
                                textColor = Color(0xFF00796B),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.sendChatMessage("What is my safe money?") }
                            )
                            PulseMetricBadge(
                                label = "Daily Limit",
                                value = FormatUtils.formatRupiah(viewModel.dailyLimit),
                                emoji = "⚡",
                                badgeColor = ViNoteWarmYellow.copy(alpha = 0.35f),
                                textColor = Color(0xFFB06000),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.sendChatMessage("How much did I spend today?") }
                            )
                            PulseMetricBadge(
                                label = "Health Score",
                                value = "80% Optimal",
                                emoji = "🎯",
                                badgeColor = ViNoteSecondaryFixed.copy(alpha = 0.6f),
                                textColor = ViNotePrimary,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.sendChatMessage("Help me save money") }
                            )
                        }
                    }
                }

                // Chat Messages Feed
                items(chatMessages) { message ->
                    ChatBubble(
                        message = message,
                        notaEye = message.eyeState,
                        baseColor = notaConfig.baseColor,
                        accessory = notaConfig.accessory,
                        onChipClicked = { chipText ->
                            viewModel.sendChatMessage(chipText)
                        }
                    )
                }

                // Animated Typing Indicator
                if (isNotaTyping) {
                    item {
                        TypingIndicatorBubble()
                    }
                }
            }

            // 3. HORIZONTAL QUICK PROMPT CHIPS
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val promptSuggestions = listOf(
                    "🍣 Can I afford dinner tonight?" to "Can I afford dinner tonight?",
                    "📊 Where did my money go?" to "Where did my money go?",
                    "💡 Help me save faster" to "Help me save",
                    "☕ Coffee spending this week" to "How much did I spend on coffee this week?",
                    "🛡️ Check Uang Aman" to "What is my safe money status?"
                )
                items(promptSuggestions) { (label, query) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ViNoteSurfaceContainerLowest)
                            .border(1.dp, Color(0x1F747789), RoundedCornerShape(50))
                            .clickable { viewModel.sendChatMessage(query) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("prompt_chip_$query")
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ViNotePrimary
                        )
                    }
                }
            }

            // 4. BOTTOM INPUT DOCK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 10.dp, ambientColor = Color(0x14171827))
                    .background(ViNoteSurfaceContainerLowest)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Voice Mic Shortcut
                    IconButton(
                        onClick = onNavigateToVoice,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ViNoteSecondaryFixed.copy(alpha = 0.6f))
                            .testTag("chat_voice_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = ViNotePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Text Input Pill
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask Nota anything...",
                                color = ViNoteTextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = ViNoteSurfaceContainerLow,
                            unfocusedContainerColor = ViNoteSurfaceContainerLow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Animated Send Button
                    val isSendActive = inputText.isNotBlank()
                    IconButton(
                        onClick = {
                            if (isSendActive) {
                                val query = inputText
                                inputText = ""
                                viewModel.sendChatMessage(query)
                            }
                        },
                        enabled = isSendActive,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSendActive) ViNotePrimary else ViNoteSurfaceContainerLow)
                            .testTag("chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (isSendActive) ViNoteOnPrimary else ViNoteTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp)) // Bottom navigation bar clearance
        }
    }
}

@Composable
private fun PulseMetricBadge(
    label: String,
    value: String,
    emoji: String,
    badgeColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(badgeColor)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ViNoteTextPrimary
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    notaEye: NotaEyeState,
    baseColor: com.example.data.model.NotaBaseColor,
    accessory: com.example.data.model.NotaAccessory,
    onChipClicked: (String) -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            // Nota Message Header with Mini Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            ) {
                NotaAvatar(
                    size = 22.dp,
                    eyeState = notaEye,
                    baseColor = baseColor,
                    accessory = accessory,
                    isAnimated = false
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nota",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ViNotePrimary
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(if (isUser) ViNotePrimary else ViNoteSurfaceContainerLowest)
                .then(
                    if (!isUser) {
                        Modifier.border(
                            1.dp,
                            Color(0x1F747789),
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
                        )
                    } else Modifier
                )
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .fillMaxWidth(0.88f)
        ) {
            Text(
                text = message.text,
                fontSize = 15.sp,
                color = if (isUser) Color.White else ViNoteTextPrimary,
                lineHeight = 22.sp
            )
        }

        // Actionable response chips below assistant messages
        if (!isUser && message.quickChips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                message.quickChips.forEach { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ViNoteSecondaryFixed.copy(alpha = 0.5f))
                            .clickable { onChipClicked(chip) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ViNotePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "d3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ViNoteSurfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = dot1.dp)
                    .clip(CircleShape)
                    .background(ViNotePrimary)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = dot2.dp)
                    .clip(CircleShape)
                    .background(ViNotePrimary)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = dot3.dp)
                    .clip(CircleShape)
                    .background(ViNotePrimary)
            )
        }
        Text(
            text = "Nota is calculating...",
            fontSize = 13.sp,
            color = ViNoteTextSecondary
        )
    }
}
