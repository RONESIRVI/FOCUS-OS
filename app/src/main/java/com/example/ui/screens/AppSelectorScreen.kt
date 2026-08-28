package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusDanger
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceVariant
import com.example.ui.theme.FocusTextSecondary
import com.example.ui.viewmodel.FocusViewModel

@Composable
fun AppSelectorScreen(
    viewModel: FocusViewModel,
    onBack: () -> Unit
) {
    val currentProfile by viewModel.currentAppSelectorProfile.collectAsState()
    val appsManual by viewModel.allowedAppsManual.collectAsState()
    val appsStrict by viewModel.allowedAppsStrict.collectAsState()
    val apps = if (currentProfile == "STRICT") appsStrict else appsManual

    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableStateOf("ALL") } // "ALL", "ALLOWED", "BLOCKED"

    val allowedCount = apps.count { it.isAllowed }
    val blockedCount = apps.count { !it.isAllowed }

    val filteredApps = apps.filter { app ->
        val matchesSearch = app.appName.contains(searchQuery, ignoreCase = true) ||
                app.category.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (filterTab) {
            "ALLOWED" -> app.isAllowed
            "BLOCKED" -> !app.isAllowed
            else -> true
        }
        matchesSearch && matchesTab
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (currentProfile == "STRICT") "ALLOWED APPS (STRICT SCHEDULE)" else "ALLOWED APPS (QUICK FOCUS)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = if (currentProfile == "STRICT") "Allowed apps during Scheduled Study Focus" else "Allowed apps during Manual Quick Focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Switcher Tabs (Manual Whitelist vs Strict Schedule Whitelist)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FocusSurface, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val profiles = listOf(
                "MANUAL" to "Quick Focus Whitelist",
                "STRICT" to "Strict Schedule Whitelist"
            )
            profiles.forEach { (profKey, profLabel) ->
                val isSel = currentProfile == profKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            if (isSel) FocusPrimary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setAppSelectorProfile(profKey) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSel) Color.White else FocusTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_apps_input"),
            placeholder = { Text("Search Apps...", color = FocusTextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = FocusPrimary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FocusPrimary,
                unfocusedBorderColor = FocusSurfaceVariant,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "All (${apps.size})",
                "ALLOWED" to "Allowed (${allowedCount})",
                "BLOCKED" to "Blocked (${blockedCount})"
            ).forEach { (key, label) ->
                val isSelected = filterTab == key
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) FocusPrimary else FocusSurface,
                    border = BorderStroke(1.dp, if (isSelected) FocusPrimary else FocusSurfaceVariant),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { filterTab = key }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Apps List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (app.isAllowed) FocusPrimary.copy(alpha = 0.12f) else FocusSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (app.isAllowed) FocusPrimary.copy(alpha = 0.5f) else FocusSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (app.isAllowed) FocusPrimary.copy(alpha = 0.25f) else FocusDanger.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (app.isAllowed) Icons.Default.Check else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (app.isAllowed) FocusPrimary else FocusDanger,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = if (app.isAllowed) "✓ ALLOWED FOR STUDY" else "🔒 BLOCKED DURING SESSION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (app.isAllowed) FocusPrimary else FocusDanger
                                )
                            }
                        }

                        Switch(
                            checked = app.isAllowed,
                            onCheckedChange = { isAllowed ->
                                viewModel.toggleAppAllowed(app.packageName, isAllowed, currentProfile)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = FocusPrimary,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = FocusSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_app_whitelist_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary, contentColor = Color.Black)
        ) {
            Text("SAVE ALLOWED APPS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

