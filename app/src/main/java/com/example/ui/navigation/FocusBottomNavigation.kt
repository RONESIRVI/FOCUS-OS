package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FocusBackground
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusTextSecondary

@Composable
fun FocusBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onQuickFocus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp)
            .background(FocusSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = currentRoute == FocusRoutes.HOME,
            onClick = { onNavigate(FocusRoutes.HOME) }
        )
        
        BottomNavItem(
            icon = Icons.Default.CalendarToday,
            label = "Schedule",
            isSelected = currentRoute == FocusRoutes.SCHEDULE_MAIN,
            onClick = { onNavigate(FocusRoutes.SCHEDULE_MAIN) }
        )
        
        // FAB (Quick Focus)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(FocusPrimary)
                .clickable { onQuickFocus() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick Focus",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
        
        BottomNavItem(
            icon = Icons.Default.BarChart,
            label = "Stats",
            isSelected = currentRoute == FocusRoutes.STATS,
            onClick = { onNavigate(FocusRoutes.STATS) }
        )
        
        BottomNavItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            isSelected = currentRoute == FocusRoutes.SETTINGS,
            onClick = { onNavigate(FocusRoutes.SETTINGS) }
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val color = if (isSelected) FocusPrimary else FocusTextSecondary
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
