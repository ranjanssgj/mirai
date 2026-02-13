package com.unifiedotaku.app.ui.screens.lock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.unifiedotaku.app.ui.theme.AppColors
import kotlinx.coroutines.delay

/**
 * Lock screen with PIN and biometric authentication.
 */
@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    savedPin: String,
    biometricEnabled: Boolean,
    onSetupPin: (String) -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSettingUp by remember { mutableStateOf(savedPin.isEmpty()) }
    var confirmPin by remember { mutableStateOf("") }
    var setupStep by remember { mutableStateOf(0) } // 0 = enter, 1 = confirm
    
    // Attempt biometric on launch if enabled
    LaunchedEffect(Unit) {
        if (biometricEnabled && savedPin.isNotEmpty()) {
            showBiometricPrompt(context, onUnlock)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // App icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = if (isSettingUp) {
                    if (setupStep == 0) "Set Your PIN" else "Confirm PIN"
                } else "Enter PIN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSettingUp) {
                    if (setupStep == 0) "Create a 4-digit PIN to secure your app"
                    else "Enter the same PIN again"
                } else "Enter your PIN to unlock",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val currentPin = if (isSettingUp && setupStep == 1) confirmPin else pin
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < currentPin.length) Color.White
                                else AppColors.DarkSurfaceVariant
                            )
                    )
                }
            }

            // Error message
            AnimatedVisibility(visible = error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Number pad
            NumberPad(
                onDigit = { digit ->
                    error = null
                    if (isSettingUp) {
                        if (setupStep == 0) {
                            if (pin.length < 4) pin += digit
                            if (pin.length == 4) {
                                setupStep = 1
                            }
                        } else {
                            if (confirmPin.length < 4) confirmPin += digit
                            if (confirmPin.length == 4) {
                                if (confirmPin == pin) {
                                    onSetupPin(pin)
                                    onUnlock()
                                } else {
                                    error = "PINs don't match. Try again."
                                    pin = ""
                                    confirmPin = ""
                                    setupStep = 0
                                }
                            }
                        }
                    } else {
                        if (pin.length < 4) pin += digit
                        if (pin.length == 4) {
                            if (pin == savedPin) {
                                onUnlock()
                            } else {
                                error = "Incorrect PIN"
                                pin = ""
                            }
                        }
                    }
                },
                onBackspace = {
                    if (isSettingUp && setupStep == 1) {
                        if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                    } else {
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    }
                },
                onBiometric = if (biometricEnabled && savedPin.isNotEmpty()) {
                    { showBiometricPrompt(context, onUnlock) }
                } else null
            )
        }
    }
}

@Composable
private fun NumberPad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: (() -> Unit)?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1-2-3
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            listOf("1", "2", "3").forEach { digit ->
                NumberButton(digit) { onDigit(digit) }
            }
        }
        // Row 4-5-6
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            listOf("4", "5", "6").forEach { digit ->
                NumberButton(digit) { onDigit(digit) }
            }
        }
        // Row 7-8-9
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            listOf("7", "8", "9").forEach { digit ->
                NumberButton(digit) { onDigit(digit) }
            }
        }
        // Row biometric-0-backspace
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Biometric button
            if (onBiometric != null) {
                FilledIconButton(
                    onClick = onBiometric,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AppColors.DarkSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = "Biometric",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }
            
            NumberButton("0") { onDigit("0") }
            
            // Backspace button
            FilledIconButton(
                onClick = onBackspace,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = AppColors.DarkSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NumberButton(
    digit: String,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = AppColors.DarkCard
        )
    ) {
        Text(
            text = digit,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary
        )
    }
}

/**
 * Show biometric authentication prompt.
 */
private fun showBiometricPrompt(context: Context, onSuccess: () -> Unit) {
    val activity = context as? FragmentActivity ?: return
    
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK
    )
    
    if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) return

    val executor = ContextCompat.getMainExecutor(context)
    
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Unified Otaku")
        .setSubtitle("Use your fingerprint to unlock")
        .setNegativeButtonText("Use PIN")
        .build()

    val biometricPrompt = BiometricPrompt(activity, executor, callback)
    biometricPrompt.authenticate(promptInfo)
}

/**
 * Check if biometric authentication is available.
 */
fun isBiometricAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == 
        BiometricManager.BIOMETRIC_SUCCESS
}
