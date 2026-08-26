with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    lines = f.readlines()

# Let's just find the offending lines
for i, line in enumerate(lines):
    if line.strip() == "}":
        if i > 0 and "VIEW FULL SCHEDULE TIMETABLE" in "".join(lines[i-10:i]):
            pass # this might be the one. Wait, it's safer to just fix it with string replacement

content = "".join(lines)
old_str = """        }
        
        }
        
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}"""
new_str = """        }
        
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}"""
if old_str in content:
    content = content.replace(old_str, new_str)
else:
    print("Not found exactly like that")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
