import sys
with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    text = f.read()
o = text.count('{')
c = text.count('}')
print(f"Open: {o}, Close: {c}, Diff: {o-c}")
