package com.unifiedotaku.app.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unifiedotaku.app.data.service.SecurityManager
import com.unifiedotaku.app.ui.navigation.BottomNavItem
import com.unifiedotaku.app.ui.navigation.NavGraph
import com.unifiedotaku.app.ui.navigation.Routes
import com.unifiedotaku.app.ui.screens.lock.LockScreen
import com.unifiedotaku.app.ui.screens.more.AppTheme
import com.unifiedotaku.app.ui.screens.more.SettingsViewModel
import com.unifiedotaku.app.ui.theme.UnifiedOtakuTheme
import com.unifiedotaku.app.ui.theme.AppColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var securityManager: SecurityManager

    private val settingsViewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()
            val isDarkTheme = when (settingsState.theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            UnifiedOtakuTheme(
                darkTheme = isDarkTheme,
                dynamicColor = settingsState.useDynamicColors,
                accentColor = androidx.compose.ui.graphics.Color(settingsState.accentColor.colorValue),
                pureBlack = settingsState.useAmoledBlack
            ) {
                MainApp(securityManager = securityManager)
            }
        }
    }
}

@Composable
fun MainApp(securityManager: SecurityManager) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val isLockEnabled by securityManager.isAppLockEnabled.collectAsState(initial = false)
    val storedPin by securityManager.storedPin.collectAsState(initial = "")
    val biometricEnabled by securityManager.isBiometricEnabled.collectAsState(initial = false)
    
    var isLocked by remember { mutableStateOf(false) }
    var hasCheckedLock by remember { mutableStateOf(false) }
    
    LaunchedEffect(isLockEnabled) {
        if (isLockEnabled && storedPin.isNotEmpty()) {
            isLocked = true
        }
        hasCheckedLock = true
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    scope.launch { securityManager.updateLastActive() }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isLockEnabled && storedPin.isNotEmpty()) {
                        scope.launch {
                            if (securityManager.shouldShowLock()) {
                                isLocked = true
                            }
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    if (isLocked && hasCheckedLock) {
        LockScreen(
            onUnlock = { isLocked = false },
            savedPin = storedPin,
            biometricEnabled = biometricEnabled,
            onSetupPin = { pin ->
                scope.launch {
                    securityManager.enableAppLock(pin)
                }
            }
        )
        return
    }
    
    val showBottomNav = currentDestination?.route in listOf(
        Routes.ANIME,
        Routes.MANGA,
        Routes.FORUMS,
        Routes.LIBRARY,
        Routes.MORE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = AppColors.DarkBackground,
                    contentColor = AppColors.TextPrimary,
                    tonalElevation = 8.dp
                ) {
                    BottomNavItem.entries.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == item.route 
                        } == true
                        
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.Primary,
                                selectedTextColor = AppColors.Primary,
                                unselectedIconColor = AppColors.TextTertiary,
                                unselectedTextColor = AppColors.TextTertiary,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
