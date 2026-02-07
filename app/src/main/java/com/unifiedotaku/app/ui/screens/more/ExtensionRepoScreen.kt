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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Repositories are used to find and update extensions.",
                        style = AppTypography.BodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
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
                    Text(item.name, style = AppTypography.BodyMedium, fontWeight = FontWeight.Bold)
                    if (item.isVerified) {
                        Surface(
                            color = Color(0xFF22C55E).copy(alpha=0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                             Text(
                                 "VERIFIED", 
                                 style = AppTypography.LabelSmall.copy(fontSize = 10.sp), 
                                 fontWeight = FontWeight.Bold,
                                 color = Color(0xFF22C55E),
                                 modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                             )
                        }
                    }
                }
                Text(item.url, style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete", tint = Color.Red.copy(alpha=0.7f))
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
        title = { Text("Add Repository") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name, url) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
