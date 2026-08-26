import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

# StackedBarChart
old_bar = """        // Day 21 (index 2): Light Green
        drawStackedBar(2, listOf(cLGreen to 40f))
        
        // Day 22 (index 3): Red, Green
        drawStackedBar(3, listOf(cRed to 60f, cGreen to 60f))
        
        // Day 23 (index 4): Blue, LGreen, Green
        drawStackedBar(4, listOf(cRas to 90f, cLGreen to 5f, cGreen to 25f))
        
        // Day 24 (index 5): Blue, Red, Green
        drawStackedBar(5, listOf(cRas to 50f, cRed to 15f, cGreen to 50f))
        
        // Day 28 (index 9): Red
        drawStackedBar(9, listOf(cRed to 120f))
        
        // Day 29 (index 10): Yellow
        drawStackedBar(10, listOf(cYellow to 50f))"""
new_bar = """        // No study logs yet"""
content = content.replace(old_bar, new_bar)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
