import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

# SubjectRatioCard
old_ratio = """                // Legend list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors1 = listOf(Color(0xFF2563EB), Color(0xFF0EA5E9), Color(0xFFDC2626), Color(0xFF65A30D), Color(0xFFEAB308), Color(0xFF22C55E))
                    val names1 = listOf("RAS Self", "Advance RAS", "PYQS Test", "REVISION", "Value Addition", "MOCK Test")
                    val times1 = listOf("3:21", "3:16", "3:16", "2:19", "0:50", "0:43")
                    val percents1 = listOf("24%", "24%", "24%", "17%", "6%", "5%")
                    
                    val colors2 = listOf(Color(0xFF65A30D), Color(0xFF2563EB), Color(0xFF0EA5E9), Color(0xFFDC2626), Color(0xFFEAB308), Color(0xFF22C55E))
                    val names2 = listOf("REVISION", "RAS Self", "Advance RAS", "PYQS Test", "Value Addition", "MOCK Test")
                    val times2 = listOf("3:53", "3:21", "3:16", "3:16", "2:58", "0:43")
                    val percents2 = listOf("22%", "19%", "19%", "19%", "17%", "4%")

                    val c = if (isSecond) colors2 else colors1
                    val n = if (isSecond) names2 else names1
                    val t = if (isSecond) times2 else times1
                    val p = if (isSecond) percents2 else percents1

                    for (i in 0 until 6) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(c[i], CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(n[i], color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${t[i]} · ${p[i]}", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }"""
new_ratio = """                // Legend list (Empty)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No data available", color = FocusTextSecondary, style = MaterialTheme.typography.bodySmall)
                }"""
content = content.replace(old_ratio, new_ratio)

# DonutChart
old_donut = """        val colors1 = listOf(Color(0xFF2563EB), Color(0xFF0EA5E9), Color(0xFFDC2626), Color(0xFF65A30D), Color(0xFFEAB308), Color(0xFF22C55E))
        val sweeps1 = listOf(86f, 86f, 86f, 62f, 22f, 18f) // Approx percentages
        
        val colors2 = listOf(Color(0xFF65A30D), Color(0xFF2563EB), Color(0xFF0EA5E9), Color(0xFFDC2626), Color(0xFFEAB308), Color(0xFF22C55E))
        val sweeps2 = listOf(79f, 68f, 68f, 68f, 61f, 16f)

        val sweeps = if (isSecond) sweeps2 else sweeps1
        val colors = if (isSecond) colors2 else colors1
        
        var startAngle = -90f
        for (i in 0 until 6) {
            drawArc(
                color = colors[i],
                startAngle = startAngle,
                sweepAngle = sweeps[i],
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweeps[i]
        }
        
        // Draw text inside donut
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        // Draw some percentage labels directly on the canvas as an approximation
        // To be precise we calculate the middle angle
        var lblAngle = -90f
        val radius = size.width / 2
        for (i in 0 until 6) {
            val mid = lblAngle + (sweeps[i] / 2)
            lblAngle += sweeps[i]
            
            if (sweeps[i] > 20f) {
                val r = radius - strokeWidth/2
                val rx = radius + r * kotlin.math.cos(Math.toRadians(mid.toDouble())).toFloat()
                val ry = radius + r * kotlin.math.sin(Math.toRadians(mid.toDouble())).toFloat()
                val pct = if (isSecond) listOf("22%", "19%", "19%", "19%", "17%", "4%")[i] else listOf("24%", "24%", "24%", "17%", "6%", "5%")[i]
                drawContext.canvas.nativeCanvas.drawText(pct, rx, ry + 10f, paint)
            }
        }"""
new_donut = """        drawArc(
            color = FocusSurfaceVariant,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )"""
content = content.replace(old_donut, new_donut)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
