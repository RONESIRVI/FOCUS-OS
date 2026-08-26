import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

# Replace actions in TopAppBar
old_actions = """                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                        Text("AI", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                },"""
new_actions = """                actions = {
                    IconButton(onClick = { /* TODO: Download Statistics */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                },"""
content = content.replace(old_actions, new_actions)

# Update state variables
old_state = """    var showCompareDialog by remember { mutableStateOf(false) }
    var compareModeEnabled by remember { mutableStateOf(true) }"""
new_state = """    var selectedTab by remember { mutableStateOf("Period") }
    var showCompareDialog by remember { mutableStateOf(false) }
    var compareModeEnabled by remember { mutableStateOf(true) }"""
content = content.replace(old_state, new_state)

# Make tabs clickable
old_tab = """                    items(tabs) { tab ->
                        val isSelected = tab == "Period"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) StatBlue else Color.Transparent)
                                .border(1.dp, if (isSelected) Color.Transparent else FocusSurfaceVariant, RoundedCornerShape(20.dp))
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {"""
new_tab = """                    items(tabs) { tab ->
                        val isSelected = tab == selectedTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) StatBlue else Color.Transparent)
                                .border(1.dp, if (isSelected) Color.Transparent else FocusSurfaceVariant, RoundedCornerShape(20.dp))
                                .clickable { selectedTab = tab }
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {"""
content = content.replace(old_tab, new_tab)

# Add conditionals for tab content
old_period_start = """            // Period Selectors
            item {"""
new_period_start = """            if (selectedTab == "Period") {
            // Period Selectors
            item {"""
content = content.replace(old_period_start, new_period_start)

old_period_end = """                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            LegendItem(color = Color(0xFF65A30D), text = "REVISION")
                            LegendItem(color = Color(0xFFEAB308), text = "Value Addit...")
                            LegendItem(color = Color(0xFF22C55E), text = "MOCK Test")
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showCompareDialog) {"""

new_period_end = """                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            LegendItem(color = Color(0xFF65A30D), text = "REVISION")
                            LegendItem(color = Color(0xFFEAB308), text = "Value Addit...")
                            LegendItem(color = Color(0xFF22C55E), text = "MOCK Test")
                        }
                    }
                }
            }
            } else if (selectedTab == "Day") {
                item { DayTabContent() }
            } else if (selectedTab == "Week") {
                item { WeekTabContent() }
            } else if (selectedTab == "Month") {
                item { MonthTabContent() }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Text("Coming Soon", color = FocusTextSecondary)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showCompareDialog) {"""
content = content.replace(old_period_end, new_period_end)

if "import androidx.compose.material.icons.filled.Download" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.AutoAwesome", "import androidx.compose.material.icons.filled.AutoAwesome\nimport androidx.compose.material.icons.filled.Download\nimport androidx.compose.material.icons.filled.KeyboardArrowLeft\nimport androidx.compose.material.icons.filled.KeyboardArrowRight")

new_composables = """
@Composable
fun DayTabContent() {
    Card(
        colors = CardDefaults.cardColors(containerColor = FocusSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                Text("Aug", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(it, color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val days = (27..31).map { it.toString() to false } + (1..31).map { it.toString() to true } + (1..6).map { it.toString() to false }
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (row in 0 until 6) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            if (index < days.size) {
                                val (day, currentMonth) = days[index]
                                val isSelected = currentMonth && day == "26"
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        color = if (currentMonth) Color.White else FocusTextSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF3B82F6), Color(0xFF60A5FA))
                    val labels = listOf("0+", "4+", "7+", "10+", "12+")
                    colors.zip(labels).forEach { (color, label) ->
                        Box(modifier = Modifier.background(color).padding(horizontal = 4.dp, vertical = 2.dp)) {
                            Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                    }
                }
                Text("Aug: 0H 00M", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun WeekTabContent() {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("2026 Q3", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                val weeks = listOf(
                    Triple("6/29 ~", "0:50:00", true), Triple("7/6 ~", "0:34:00", true), Triple("7/13 ~", "3:08:00", true), Triple("7/20 ~", "", false), Triple("7/27 ~", "", false),
                    Triple("8/3 ~", "", false), Triple("8/10 ~", "", false), Triple("8/17 ~", "", false), Triple("8/24 ~", "", false), Triple("8/31 ~", "", false),
                    Triple("9/7 ~", "", false), Triple("9/14 ~", "", false), Triple("9/21 ~", "", false), Triple("9/28 ~", "", false), Triple("", "", false)
                )
                
                Column {
                    for (row in 0 until 3) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 5) {
                                val index = row * 5 + col
                                if (index < weeks.size) {
                                    val (week, time, isHighlighted) = weeks[index]
                                    val isSelected = week == "8/24 ~"
                                    val hasData = time.isNotEmpty()
                                    if (week.isEmpty()) {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                        continue
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(if (hasData) StatBlue.copy(alpha = if (isHighlighted) 0.3f else 0.1f) else Color.Transparent)
                                            .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(2.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(week, color = if (hasData || isSelected) Color.White else FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                                            if (hasData) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(time, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(150.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Mon, Aug 24 ~ Sun, Aug 30", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Text("No study logs.", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun MonthTabContent() {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("2026", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                val months = listOf(
                    Triple("Jan", "105:41:00", true), Triple("Feb", "27:34:16", false), Triple("Mar", "1:39:59", false), Triple("Apr", "5:58:23", false),
                    Triple("May", "4:16:59", false), Triple("Jun", "13:48:36", false), Triple("Jul", "3:42:00", false), Triple("Aug", "", false),
                    Triple("Sep", "", false), Triple("Oct", "", false), Triple("Nov", "", false), Triple("Dec", "", false)
                )
                
                Column {
                    for (row in 0 until 3) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 4) {
                                val index = row * 4 + col
                                val (month, time, isHighlighted) = months[index]
                                val isSelected = month == "Aug"
                                val hasData = time.isNotEmpty()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(if (hasData) StatBlue.copy(alpha = if (isHighlighted) 0.3f else 0.1f) else Color.Transparent)
                                        .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(month, color = if (hasData || isSelected) Color.White else FocusTextSecondary, style = MaterialTheme.typography.bodyMedium)
                                        if (hasData) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(time, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = FocusSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(150.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aug 2026", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Text("No study logs.", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content + "\n" + new_composables)
