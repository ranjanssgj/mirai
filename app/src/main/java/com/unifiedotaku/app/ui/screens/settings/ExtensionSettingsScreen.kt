package com.unifiedotaku.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unifiedotaku.app.data.extensions.ExtensionManager
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.data.extensions.LoadedExtension

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionSettingsScreen(
    onBack: () -> Unit,
    viewModel: ExtensionSettingsViewModel = hiltViewModel()
) {
    val extensions by viewModel.extensions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extensions", style = AppTypography.HeadlineSmall, color = AppColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.DarkBackground,
                    titleContentColor = AppColors.TextPrimary
                )
            )
        },
        containerColor = AppColors.DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.DarkBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(extensions) { extension ->
                ExtensionCard(extension)
            }
            
            if (extensions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No extensions installed", color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}


@Composable
fun ExtensionCard(extension: LoadedExtension) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Extension, null, tint = AppColors.Primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(extension.name, style = AppTypography.TitleMedium, color = AppColors.TextPrimary)
                Text("Version: ${extension.versionName}", style = AppTypography.BodySmall, color = AppColors.TextSecondary)
                Text("Package: ${extension.pkgName}", style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
            }
            // Future: Switch for disable/enable
            Surface(
                color = Color.Green.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "INSTALLED",
                    style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Green,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
