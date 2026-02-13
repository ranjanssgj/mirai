package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionRepoScreen(
    navController: NavController
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Mock Data
    var repos by remember {
        mutableStateOf(
            listOf(
                RepoItem("Keiyoushi", "https://keiyoushi.github.io/extensions/index.min.json", true)
            )
        )
    }
    
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Extension Repos", style = AppTypography.TitleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, "Add Repo")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = AppColors.DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 8.dp,
                    color = Color.White.copy(alpha=0.05f)
                ) {
                    Text(
                        "Repositories are used to find and update extensions.",
                        style = AppTypography.BodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = Color.White.copy(alpha=0.7f)
                    )
                }
            }

            items(repos) { repo ->
                RepoCard(repo, onDelete = {
                    repos = repos.filter { it != repo }
                })
            }
        }
        
        if (showAddDialog) {
            AddRepoDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, url ->
                    repos = repos + RepoItem(name, url, true)
                    showAddDialog = false
                }
            )
        }
    }
}

data class RepoItem(
    val name: String,
    val url: String,
    val isVerified: Boolean
)

@Composable
 fun RepoCard(item: RepoItem, onDelete: () -> Unit) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.name, style = AppTypography.BodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    if (item.isVerified) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha=0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                             Text(
                                 "VERIFIED", 
                                 style = AppTypography.LabelSmall.copy(fontSize = 10.sp), 
                                 fontWeight = FontWeight.Bold,
                                 color = Color.White
                             )
                        }
                    }
                }
                Text(item.url, style = AppTypography.LabelSmall, color = AppColors.TextSecondary, maxLines = 1)
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete", tint = Color.White.copy(alpha=0.4f))
            }
        }
    }
}

@Composable
fun AddRepoDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.DarkSurface,
        titleContentColor = Color.White,
        textContentColor = AppColors.TextSecondary,
        title = { Text("Add Repository", style = AppTypography.TitleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = Color.White.copy(alpha=0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha=0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL", color = Color.White.copy(alpha=0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha=0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name, url) }) {
                Text("Add", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.TextSecondary)
            }
        }
    )
}
