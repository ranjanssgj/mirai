package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tracking Accounts", style = AppTypography.TitleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        // Design uses iOS style back arrow but let's stick to consistent Material for app cohesion, 
                        // or use the specific icon requested if strict on "High Fidelity" to code.html
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "Back", modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Done", style = AppTypography.BodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            
            // Intro
            item {
                Text(
                    "Sync your watch history across devices and unlock community features like forum discussions.",
                    style = AppTypography.BodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            // MAL Hero Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                         Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                             Box(
                                 modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF2E51A2)),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Text("MAL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                             }
                             Column {
                                 Text("MyAnimeList", style = AppTypography.TitleMedium, fontWeight = FontWeight.Bold)
                                 if (uiState.malConnected && uiState.malUsername.isNotEmpty()) {
                                     Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                         Icon(Icons.Filled.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(14.dp))
                                         Text(uiState.malUsername, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                     }
                                 } else {
                                     Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                         Icon(Icons.Filled.Forum, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                         Text("COMMENTS ENABLED", style = AppTypography.LabelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                     }
                                 }
                                 Spacer(modifier = Modifier.height(8.dp))
                                 Text("Connect to track progress & post on forums directly.", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                             }
                         }
                         Spacer(modifier = Modifier.height(16.dp))
                         if (uiState.malConnected) {
                             OutlinedButton(
                                 onClick = { viewModel.disconnectMal() },
                                 modifier = Modifier.fillMaxWidth().height(48.dp),
                                 shape = RoundedCornerShape(24.dp)
                             ) {
                                 Text("Disconnect", fontWeight = FontWeight.Bold)
                             }
                         } else {
                             Button(
                                 onClick = { viewModel.connectMal("demo_token", "User") },
                                 modifier = Modifier.fillMaxWidth().height(48.dp),
                                 colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                 shape = RoundedCornerShape(24.dp)
                             ) {
                                 Text("Connect MyAnimeList", fontWeight = FontWeight.Bold)
                             }
                         }
                    }
                }
            }
            
            // Other Accounts List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Header
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Spacer(Modifier.width(1.dp)) // Spacer to push next item to right
                        Text("Last synced: 2m ago", style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    // AniList
                    AccountItem(
                        name = "AniList",
                        username = uiState.aniListUsername.ifEmpty { null },
                        isConnected = uiState.aniListConnected,
                        color = Color(0xFF02A9FF),
                        onConnect = { viewModel.connectAniList("demo_token", "OtakuKing99") },
                        onDisconnect = { viewModel.disconnectAniList() },
                        logoContent = { Icon(Icons.Filled.CheckCircle, null, tint = Color.White) }
                    )
                    
                    // Kitsu
                    AccountItem(
                        name = "Kitsu",
                        description = "Sync library",
                        isConnected = false,
                        color = Color(0xFF332532),
                        onConnect = { },
                        logoContent = { Text("K", color = Color(0xFFFD755C), fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                    )
                    
                    // Shikimori
                    AccountItem(
                        name = "Shikimori",
                        description = "Sync library",
                        isConnected = false,
                        color = Color.White,
                        onConnect = { },
                        logoContent = { Box(Modifier.size(16.dp).background(Color.Gray)) },
                        isWhiteBg = true
                    )
                }
            }
            
            // Sync Preferences
            item {
                 Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    onClick = {}
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sync Preferences", style = AppTypography.BodyLarge, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            // Security Note
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Text(
                            "We do not store your passwords. Authentication happens securely and directly via the service providers' official login pages.",
                            style = AppTypography.LabelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountItem(
    name: String,
    username: String? = null,
    description: String? = null,
    isConnected: Boolean,
    color: Color,
    onConnect: () -> Unit = {},
    onDisconnect: () -> Unit = {},
    logoContent: @Composable () -> Unit,
    isWhiteBg: Boolean = false
) {
     Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = if(isConnected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.2f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(if(isWhiteBg) 1.dp else 0.dp, Color.Gray.copy(alpha=0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                logoContent()
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = AppTypography.BodyLarge, fontWeight = FontWeight.Bold)
                if (isConnected && username != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(14.dp))
                        Text(username, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (description != null) {
                     Text(description, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            if (isConnected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text("Disconnect")
                }
            } else {
                 Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha=0.1f), contentColor = MaterialTheme.colorScheme.primary),
                     elevation = null
                ) {
                    Text("Connect", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
