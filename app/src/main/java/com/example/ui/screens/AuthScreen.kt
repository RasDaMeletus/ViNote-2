package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotaEyeState
import com.example.ui.components.NotaAvatar
import com.example.ui.components.ViNoteButton
import com.example.ui.components.ViNoteButtonType
import com.example.ui.components.ViNoteCard
import com.example.ui.theme.ViNoteError
import com.example.ui.theme.ViNotePrimary
import com.example.ui.theme.ViNoteSecondaryFixed
import com.example.ui.theme.ViNoteSurface
import com.example.ui.theme.ViNoteSurfaceContainerLow
import com.example.ui.theme.ViNoteSurfaceContainerLowest
import com.example.ui.theme.ViNoteTextPrimary
import com.example.ui.theme.ViNoteTextSecondary
import com.example.ui.theme.ViNoteWarmYellow
import com.example.viewmodel.ViNoteViewModel

enum class AuthMode {
    LOGIN,
    SIGNUP
}

@Composable
fun AuthScreen(
    viewModel: ViNoteViewModel,
    initialMode: AuthMode = AuthMode.LOGIN,
    onLoginSuccess: () -> Unit,
    onSignupSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(initialMode) }
    val notaConfig by viewModel.notaConfig.collectAsState()
    val focusManager = LocalFocusManager.current

    // Form fields
    var fullName by remember { mutableStateOf("Farras Syafiq") }
    var email by remember { mutableStateOf("farrassyafiq213@gmail.com") }
    var password by remember { mutableStateOf("vinote123") }
    var confirmPassword by remember { mutableStateOf("vinote123") }
    var rememberMe by remember { mutableStateOf(true) }
    var agreeTerms by remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViNoteSurface)
    ) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ViNoteSecondaryFixed.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ViNoteWarmYellow.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Mascot & Welcome
            NotaAvatar(
                size = 80.dp,
                eyeState = if (authMode == AuthMode.SIGNUP) NotaEyeState.EXCITED else NotaEyeState.HAPPY,
                baseColor = notaConfig.baseColor,
                accessory = notaConfig.accessory,
                showSparkle = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ViNote",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ViNoteTextPrimary,
                letterSpacing = (-0.02).sp
            )

            Text(
                text = if (authMode == AuthMode.LOGIN) "Welcome back to smart finance" else "Start your journey with Nota",
                fontSize = 14.sp,
                color = ViNoteTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Mode Segmented Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(ViNoteSurfaceContainerLow)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (authMode == AuthMode.LOGIN) ViNotePrimary else Color.Transparent)
                        .clickable {
                            authMode = AuthMode.LOGIN
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp)
                        .testTag("auth_tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (authMode == AuthMode.LOGIN) Color.White else ViNoteTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (authMode == AuthMode.SIGNUP) ViNotePrimary else Color.Transparent)
                        .clickable {
                            authMode = AuthMode.SIGNUP
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp)
                        .testTag("auth_tab_signup"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Up",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (authMode == AuthMode.SIGNUP) Color.White else ViNoteTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form Card
            ViNoteCard(
                padding = 20.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = authMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "auth_form_transition"
                ) { currentMode ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Full Name (Only on Signup)
                        if (currentMode == AuthMode.SIGNUP) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = ViNotePrimary)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ViNotePrimary,
                                    unfocusedBorderColor = Color(0xFFDDE3EA),
                                    focusedContainerColor = ViNoteSurfaceContainerLowest,
                                    unfocusedContainerColor = ViNoteSurfaceContainerLowest
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )
                        }

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = ViNotePrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ViNotePrimary,
                                unfocusedBorderColor = Color(0xFFDDE3EA),
                                focusedContainerColor = ViNoteSurfaceContainerLowest,
                                unfocusedContainerColor = ViNoteSurfaceContainerLowest
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ViNotePrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = ViNoteTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (currentMode == AuthMode.SIGNUP) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ViNotePrimary,
                                unfocusedBorderColor = Color(0xFFDDE3EA),
                                focusedContainerColor = ViNoteSurfaceContainerLowest,
                                unfocusedContainerColor = ViNoteSurfaceContainerLowest
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        // Confirm Password (Only on Signup)
                        if (currentMode == AuthMode.SIGNUP) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = ViNotePrimary)
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ViNotePrimary,
                                    unfocusedBorderColor = Color(0xFFDDE3EA),
                                    focusedContainerColor = ViNoteSurfaceContainerLowest,
                                    unfocusedContainerColor = ViNoteSurfaceContainerLowest
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_confirm_password_input")
                            )
                        }

                        // Options row (Remember Me or Agree Terms)
                        if (currentMode == AuthMode.LOGIN) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        colors = CheckboxDefaults.colors(checkedColor = ViNotePrimary)
                                    )
                                    Text(
                                        text = "Remember me",
                                        fontSize = 13.sp,
                                        color = ViNoteTextSecondary
                                    )
                                }

                                Text(
                                    text = "Forgot password?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ViNotePrimary,
                                    modifier = Modifier.clickable {
                                        viewModel.login("farrassyafiq213@gmail.com", "password")
                                        onLoginSuccess()
                                    }
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agreeTerms,
                                    onCheckedChange = { agreeTerms = it },
                                    colors = CheckboxDefaults.colors(checkedColor = ViNotePrimary)
                                )
                                Text(
                                    text = "I agree to Terms & Privacy Policy",
                                    fontSize = 12.sp,
                                    color = ViNoteTextSecondary
                                )
                            }
                        }

                        // Error message
                        errorMessage?.let { err ->
                            Text(
                                text = err,
                                color = ViNoteError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Submit Button
                        ViNoteButton(
                            text = if (currentMode == AuthMode.LOGIN) "Log In" else "Create Account & Setup",
                            onClick = {
                                if (email.isBlank() || !email.contains("@")) {
                                    errorMessage = "Please enter a valid email address"
                                    return@ViNoteButton
                                }
                                if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters"
                                    return@ViNoteButton
                                }
                                if (currentMode == AuthMode.SIGNUP && password != confirmPassword) {
                                    errorMessage = "Passwords do not match"
                                    return@ViNoteButton
                                }
                                if (currentMode == AuthMode.SIGNUP && !agreeTerms) {
                                    errorMessage = "Please accept the Terms to continue"
                                    return@ViNoteButton
                                }

                                if (currentMode == AuthMode.LOGIN) {
                                    viewModel.login(email, password)
                                    onLoginSuccess()
                                } else {
                                    viewModel.signup(fullName, email, password)
                                    onSignupSuccess()
                                }
                            },
                            testTag = if (currentMode == AuthMode.LOGIN) "auth_submit_login_btn" else "auth_submit_signup_btn"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider OR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFE2E8F0))
                )
                Text(
                    text = "OR CONTINUE WITH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ViNoteTextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFE2E8F0))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign-In Quick Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable {
                        viewModel.login("farrassyafiq213@gmail.com", "google_auth")
                        if (authMode == AuthMode.SIGNUP) onSignupSuccess() else onLoginSuccess()
                    }
                    .padding(vertical = 14.dp)
                    .testTag("auth_google_signin_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🌐",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (authMode == AuthMode.LOGIN) "Sign in with Google" else "Sign up with Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ViNoteTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
