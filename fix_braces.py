with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

# I replaced `Dialog(...) { Box(...) {` with `Box(...) {`
# Which means there's an extra `}` later.
# Let's count braces to find the exact extra `}`.

def find_extra_brace(content, start_idx):
    # start_idx is where the new `Box(...) {` ends.
    count = 1
    idx = start_idx
    while idx < len(content):
        if content[idx] == '{':
            count += 1
        elif content[idx] == '}':
            count -= 1
            if count == 0:
                return idx
        idx += 1
    return -1

# Find where Box(...) { starts
box_str = "            Box(\n                modifier = Modifier\n                    .fillMaxSize()\n                    .background(Color.Black.copy(alpha = 0.96f))"
start_idx = content.find(box_str)

if start_idx != -1:
    brace_idx = content.find('{', start_idx)
    end_box_idx = find_extra_brace(content, brace_idx + 1)
    
    if end_box_idx != -1:
        # the next non-whitespace character should be `}` for the Dialog.
        next_brace = content.find('}', end_box_idx + 1)
        
        # let's remove next_brace
        if next_brace != -1:
            content = content[:next_brace] + content[next_brace+1:]
            with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
                f.write(content)
            print("Extra brace removed.")
        else:
            print("Could not find extra brace.")
    else:
        print("Could not parse Box braces.")
else:
    print("Could not find Box start.")
