import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

old_chart = """        // Draw Line 1 (Blue)
        val bluePath = Path().apply {
            moveTo(padLeft + stepX * 0.5f, h) // start near 0
            lineTo(padLeft + stepX * 0.8f, h - stepY * 1.5f)
            lineTo(padLeft + stepX * 1.5f, h - stepY * 4.0f)
            lineTo(padLeft + stepX * 2f, h - stepY * 4.8f)
        }
        drawPath(path = bluePath, color = StatBlue, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        if (showCompare) {
            val greenPath = Path().apply {
                moveTo(padLeft + stepX * 0.7f, h)
                lineTo(padLeft + stepX * 1.0f, h - stepY * 1.5f)
                lineTo(padLeft + stepX * 1.6f, h - stepY * 4.7f)
                lineTo(padLeft + stepX * 2.5f, h - stepY * 4.7f)
                lineTo(padLeft + stepX * 2.8f, h - stepY * 5.0f)
                lineTo(padLeft + stepX * 3.5f, h - stepY * 6.0f)
                lineTo(padLeft + stepX * 7.0f, h - stepY * 6.0f)
            }
            drawPath(path = greenPath, color = StatGreen, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }"""

new_chart = """        // Draw Line 1 (Blue) - Empty Data
        val bluePath = Path().apply {
            moveTo(padLeft, h) // start at 0
            lineTo(w + padLeft, h) // Flat line at 0
        }
        drawPath(path = bluePath, color = StatBlue, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        if (showCompare) {
            val greenPath = Path().apply {
                moveTo(padLeft, h)
                lineTo(w + padLeft, h) // Flat line at 0
            }
            drawPath(path = greenPath, color = StatGreen, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }"""

content = content.replace(old_chart, new_chart)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
