import re

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content = f.read()

# weeks
old_weeks = """                val weeks = listOf(
                    Triple("6/29 ~", "0:50:00", true), Triple("7/6 ~", "0:34:00", true), Triple("7/13 ~", "3:08:00", true), Triple("7/20 ~", "", false), Triple("7/27 ~", "", false),
                    Triple("8/3 ~", "", false), Triple("8/10 ~", "", false), Triple("8/17 ~", "", false), Triple("8/24 ~", "", false), Triple("8/31 ~", "", false),
                    Triple("9/7 ~", "", false), Triple("9/14 ~", "", false), Triple("9/21 ~", "", false), Triple("9/28 ~", "", false), Triple("", "", false)
                )"""
new_weeks = """                val weeks = listOf(
                    Triple("6/29 ~", "", false), Triple("7/6 ~", "", false), Triple("7/13 ~", "", false), Triple("7/20 ~", "", false), Triple("7/27 ~", "", false),
                    Triple("8/3 ~", "", false), Triple("8/10 ~", "", false), Triple("8/17 ~", "", false), Triple("8/24 ~", "", false), Triple("8/31 ~", "", false),
                    Triple("9/7 ~", "", false), Triple("9/14 ~", "", false), Triple("9/21 ~", "", false), Triple("9/28 ~", "", false), Triple("", "", false)
                )"""
content = content.replace(old_weeks, new_weeks)

# months
old_months = """                val months = listOf(
                    Triple("Jan", "105:41:00", true), Triple("Feb", "27:34:16", false), Triple("Mar", "1:39:59", false), Triple("Apr", "5:58:23", false),
                    Triple("May", "4:16:59", false), Triple("Jun", "13:48:36", false), Triple("Jul", "3:42:00", false), Triple("Aug", "", false),
                    Triple("Sep", "", false), Triple("Oct", "", false), Triple("Nov", "", false), Triple("Dec", "", false)
                )"""
new_months = """                val months = listOf(
                    Triple("Jan", "", false), Triple("Feb", "", false), Triple("Mar", "", false), Triple("Apr", "", false),
                    Triple("May", "", false), Triple("Jun", "", false), Triple("Jul", "", false), Triple("Aug", "", false),
                    Triple("Sep", "", false), Triple("Oct", "", false), Triple("Nov", "", false), Triple("Dec", "", false)
                )"""
content = content.replace(old_months, new_months)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content)
