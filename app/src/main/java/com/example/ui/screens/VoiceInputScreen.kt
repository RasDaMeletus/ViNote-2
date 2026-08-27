package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.OfflineNlpEngine
import com.example.data.model.NotaEyeState
import com.example.ui.components.FormatUtils
import com.example.ui.components.NotaAvatar
import com.example.ui.components.ViNoteButton
import com.example.ui.theme.ViNoteMintSuccess
import com.example.ui.theme.ViNotePrimary
import com.example.ui.theme.ViNoteSecondaryFixed
import com.example.ui.theme.ViNoteSurface
import com.example.ui.theme.ViNoteSurfaceContainerLow
import com.example.ui.theme.ViNoteSurfaceContainerLowest
import com.example.ui.theme.ViNoteTertiaryFixed
import com.example.ui.theme.ViNoteTextPrimary
import com.example.ui.theme.ViNoteTextSecondary
import com.example.viewmodel.ViNoteViewModel

@Composable
fun VoiceInputScreen(
    viewModel: ViNoteViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceTranscript by viewModel.voiceTranscript.collectAsState()
    val parsedEntity by viewModel.parsedVoiceEntity.collectAsState()
    val isListening by viewModel.isVoiceListening.collectAsState()
    val notaConfig by viewModel.notaConfig.collectAsState()

    var isEditingTranscript by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")
    val pulseRing1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring1"
    )
    val pulseRing2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViNoteSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ViNoteSurfaceContainerLow)
                        .testTag("voice_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ViNoteTextPrimary
                    )
                }

                Text(
                    text = "Offline Voice Input",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ViNoteTextPrimary
                )

                // Privacy Indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ViNoteMintSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Offline Privacy",
                        tint = ViNoteMintSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Offline Mode Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ViNoteMintSuccess.copy(alpha = 0.15f))
                    .border(1.dp, ViNoteMintSuccess.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ViNoteMintSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚡ Local ASR + On-Device NLP Engine Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F6E3B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Listening Nota
            NotaAvatar(
                size = 90.dp,
                eyeState = if (isListening) NotaEyeState.EXCITED else NotaEyeState.HAPPY,
                baseColor = notaConfig.baseColor,
                accessory = notaConfig.accessory,
                showSparkle = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isListening) "Nota is listening..." else "Ready for voice input",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ViNotePrimary
            )

            Text(
                text = "Tap a preset below or hold the mic to speak offline",
                fontSize = 13.sp,
                color = ViNoteTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pulsing Mic Graphic
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .clickable {
                        val randomSample = OfflineNlpEngine.sampleVoiceUtterances.random()
                        viewModel.simulateVoiceStreaming(randomSample)
                    }
            ) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseRing2)
                            .background(ViNoteSecondaryFixed.copy(alpha = 0.3f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(pulseRing1)
                            .background(ViNoteSecondaryFixed.copy(alpha = 0.5f), CircleShape)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            ambientColor = ViNotePrimary.copy(alpha = 0.5f),
                            spotColor = ViNotePrimary.copy(alpha = 0.5f)
                        )
                        .clip(CircleShape)
                        .background(ViNotePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = "Recording",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sample Voice Utterances Chips
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "TRY SPEAKING (SAMPLE PHRASES)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ViNoteTextSecondary,
                    letterSpacing = 0.06.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OfflineNlpEngine.sampleVoiceUtterances.take(4).forEach { phrase ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(ViNoteSecondaryFixed.copy(alpha = 0.6f))
                                .clickable {
                                    viewModel.simulateVoiceStreaming(phrase)
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = phrase,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ViNotePrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transcript Box with live cursor / input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x14171827)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(ViNoteSurfaceContainerLowest)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE SPEECH TRANSCRIPTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ViNoteTextSecondary,
                            letterSpacing = 0.08.sp
                        )

                        Text(
                            text = if (isEditingTranscript) "Done Editing" else "Edit Text",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ViNotePrimary,
                            modifier = Modifier.clickable {
                                isEditingTranscript = !isEditingTranscript
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingTranscript) {
                        OutlinedTextField(
                            value = voiceTranscript,
                            onValueChange = { viewModel.setVoiceTranscript(it) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ViNotePrimary,
                                unfocusedBorderColor = ViNoteSecondaryFixed
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { isEditingTranscript = false })
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = voiceTranscript,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ViNoteTextPrimary
                            )
                            if (isListening) {
                                Text(
                                    text = "|",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ViNotePrimary,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Parsed Entity Breakdown Card (Real-time NLP output)
            parsedEntity?.let { entity ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x14171827)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(ViNoteSurfaceContainerLowest)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ON-DEVICE NLP EXTRACTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ViNoteTextSecondary,
                                letterSpacing = 0.08.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = ViNoteMintSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "96% Confidence",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ViNoteMintSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Amount Entity
                            EntityTag(
                                icon = Icons.Default.Payments,
                                label = "Amount",
                                value = FormatUtils.formatRupiah(entity.amount),
                                bgColor = ViNoteTertiaryFixed,
                                tint = Color(0xFF6E2900),
                                modifier = Modifier.weight(1f)
                            )

                            // Merchant / Title Entity
                            EntityTag(
                                icon = Icons.Default.Storefront,
                                label = "Merchant",
                                value = entity.merchant,
                                bgColor = ViNoteSecondaryFixed,
                                tint = ViNotePrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Category Entity
                            EntityTag(
                                icon = Icons.Default.Category,
                                label = "Category",
                                value = entity.category,
                                bgColor = ViNoteSurfaceContainerLow,
                                tint = ViNoteTextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            // Payment Wallet Entity
                            EntityTag(
                                icon = Icons.Default.Payments,
                                label = "Payment",
                                value = entity.walletName ?: "GoPay",
                                bgColor = ViNoteMintSuccess.copy(alpha = 0.2f),
                                tint = Color(0xFF0F6E3B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Done & Record Expense Button
            ViNoteButton(
                text = "Record Spoken Expense",
                onClick = {
                    viewModel.processVoiceInput()
                    onBack()
                },
                testTag = "voice_done_btn"
            )

            Spacer(modifier = Modifier.navigationBarsPadding().height(24.dp))
        }
    }
}

@Composable
private fun EntityTag(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    bgColor: Color,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = tint.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint
                )
            }
        }
    }
}
