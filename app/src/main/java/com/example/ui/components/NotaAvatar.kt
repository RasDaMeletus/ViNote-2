package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotaAccessory
import com.example.data.model.NotaBaseColor
import com.example.data.model.NotaEyeState
import com.example.ui.theme.ViNoteMintSuccess
import com.example.ui.theme.ViNotePrimary
import com.example.ui.theme.ViNoteSecondaryFixed
import com.example.ui.theme.ViNoteSoftPink
import com.example.ui.theme.ViNoteTextPrimary
import com.example.ui.theme.ViNoteWarmYellow
import kotlinx.coroutines.launch

@Composable
fun NotaAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    eyeState: NotaEyeState = NotaEyeState.HAPPY,
    baseColor: NotaBaseColor = NotaBaseColor.SOFT_PINK,
    accessory: NotaAccessory = NotaAccessory.NONE,
    showSparkle: Boolean = false,
    showCheckBadge: Boolean = false,
    isAnimated: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val bounceScale = remember { Animatable(1f) }
    val touchSquashX = remember { Animatable(1f) }
    val touchSquashY = remember { Animatable(1f) }

    // Continuous floating & gentle breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "nota_motion")
    val floatOffset by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -7f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val pulseScale by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val auraPulse by if (isAnimated && (eyeState == NotaEyeState.THINKING || eyeState == NotaEyeState.EXCITED || showSparkle)) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "aura"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    // Dynamic tilt angle transition based on emotion/state
    val targetRotationAngle = when (eyeState) {
        NotaEyeState.THINKING -> -8f
        NotaEyeState.CURIOUS -> 6f
        NotaEyeState.EXCITED -> 4f
        NotaEyeState.PROUD -> -3f
        NotaEyeState.HAPPY -> 0f
        NotaEyeState.NEUTRAL -> 0f
    }
    val animatedRotation by animateFloatAsState(
        targetValue = if (isAnimated) targetRotationAngle else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nota_tilt"
    )

    // Smooth color morphing transition
    val rawBgColor = when (baseColor) {
        NotaBaseColor.SOFT_PINK -> ViNoteSoftPink
        NotaBaseColor.SOFT_BLUE -> ViNoteSecondaryFixed
        NotaBaseColor.WARM_YELLOW -> ViNoteWarmYellow
        NotaBaseColor.MINT_GREEN -> Color(0xFFE8F5E9)
    }
    val animatedBgColor by animateColorAsState(
        targetValue = rawBgColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "nota_bg_color"
    )

    val eyesText = when (eyeState) {
        NotaEyeState.NEUTRAL -> "● ●"
        NotaEyeState.CURIOUS -> "⊙ ⊙"
        NotaEyeState.EXCITED -> "★ ★"
        NotaEyeState.HAPPY -> "◡ ◡"
        NotaEyeState.THINKING -> "● ◌"
        NotaEyeState.PROUD -> "✦ ✦"
    }

    val eyeFontSize = (size.value * 0.28f).sp

    Box(
        modifier = modifier
            .size(size)
            .offset(y = floatOffset.dp)
            .rotate(animatedRotation)
            .scale(pulseScale * bounceScale.value)
            .testTag("nota_avatar")
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        coroutineScope.launch {
                            // Responsive squash and stretch physics
                            touchSquashX.animateTo(1.15f, tween(90))
                            touchSquashY.animateTo(0.85f, tween(90))
                            bounceScale.animateTo(1.12f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                            touchSquashX.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                            touchSquashY.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                            bounceScale.animateTo(1f, tween(120))
                        }
                        onClick()
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Soft aura glow when thinking or animated
        if (isAnimated && (eyeState == NotaEyeState.THINKING || eyeState == NotaEyeState.EXCITED || showSparkle)) {
            Box(
                modifier = Modifier
                    .size(size * 1.12f)
                    .scale(auraPulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                animatedBgColor.copy(alpha = 0.55f),
                                animatedBgColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Main circular body with squash & stretch support
        Box(
            modifier = Modifier
                .size(
                    width = (size.value * touchSquashX.value).dp,
                    height = (size.value * touchSquashY.value).dp
                )
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x1A171827),
                    spotColor = Color(0x1F171827)
                )
                .clip(CircleShape)
                .background(animatedBgColor)
                .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Subtle top highlight gradient
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Blushes (for Happy & Proud or Pink)
            AnimatedVisibility(
                visible = eyeState == NotaEyeState.HAPPY || eyeState == NotaEyeState.PROUD || baseColor == NotaBaseColor.SOFT_PINK,
                enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.7f),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (size.value * 0.12f).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size((size.value * 0.12f).dp, (size.value * 0.08f).dp)
                            .offset(x = -(size.value * 0.22f).dp)
                            .background(Color(0x33BA1A1A), RoundedCornerShape(50))
                    )
                    Box(
                        modifier = Modifier
                            .size((size.value * 0.12f).dp, (size.value * 0.08f).dp)
                            .offset(x = (size.value * 0.22f).dp)
                            .background(Color(0x33BA1A1A), RoundedCornerShape(50))
                    )
                }
            }

            // Animated Eye Expression Transition
            AnimatedContent(
                targetState = eyesText to eyeState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(180)) + scaleIn(
                        initialScale = 0.7f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                    )).togetherWith(
                        fadeOut(animationSpec = tween(120)) + scaleOut(targetScale = 0.7f)
                    )
                },
                label = "nota_eyes_transition"
            ) { (currentEyesText, currentEyeState) ->
                Text(
                    text = currentEyesText,
                    fontSize = eyeFontSize,
                    fontWeight = FontWeight.Bold,
                    color = ViNoteTextPrimary,
                    letterSpacing = if (currentEyeState == NotaEyeState.HAPPY) 6.sp else 3.sp,
                    modifier = Modifier.offset(y = if (currentEyeState == NotaEyeState.HAPPY) 2.dp else (-1).dp)
                )
            }

            // Accessories Overlay with smooth animated transitions
            AnimatedContent(
                targetState = accessory,
                transitionSpec = {
                    (fadeIn(tween(200)) + scaleIn(initialScale = 0.6f)).togetherWith(
                        fadeOut(tween(150)) + scaleOut(targetScale = 0.6f)
                    )
                },
                label = "nota_accessory_transition"
            ) { currentAccessory ->
                when (currentAccessory) {
                    NotaAccessory.GLASSES -> {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = (-2).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((size.value * 0.32f).dp)
                                    .border(2.5.dp, ViNotePrimary, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size((size.value * 0.10f).dp, 2.5.dp)
                                    .align(Alignment.CenterVertically)
                                    .background(ViNotePrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .size((size.value * 0.32f).dp)
                                    .border(2.5.dp, ViNotePrimary, CircleShape)
                            )
                        }
                    }
                    NotaAccessory.BOWTIE -> {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = -(size.value * 0.08f).dp)
                                .size((size.value * 0.28f).dp, (size.value * 0.14f).dp)
                                .background(ViNotePrimary, RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((size.value * 0.08f).dp)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    }
                    NotaAccessory.HEADPHONES -> {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Headphones",
                            tint = ViNotePrimary,
                            modifier = Modifier
                                .size((size.value * 0.85f).dp)
                                .align(Alignment.TopCenter)
                                .offset(y = -(size.value * 0.05f).dp)
                        )
                    }
                    NotaAccessory.NONE -> {}
                }
            }
        }

        // Sparkle decoration (top-right) with smooth scale transition
        AnimatedVisibility(
            visible = showSparkle,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.4f, animationSpec = spring(Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.4f),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = (-4).dp)
                    .size((size.value * 0.3f).dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0x1A000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    color = ViNotePrimary,
                    fontSize = (size.value * 0.18f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Check badge (for confirmation & verified states) with spring scale transition
        AnimatedVisibility(
            visible = showCheckBadge,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.3f, animationSpec = spring(Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.3f),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.32f).dp)
                    .background(Color.White, CircleShape)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(ViNoteMintSuccess, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = Color.White,
                        modifier = Modifier.size((size.value * 0.2f).dp)
                    )
                }
            }
        }
    }
}
