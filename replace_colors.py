import os

color_map = {
    'FocusCyan': 'FocusPrimary',
    'FocusCyanDark': 'FocusPrimaryDark',
    'FocusSlateBg': 'FocusBackground',
    'FocusAccentOrange': 'FocusWarning',
    'FocusCoralRed': 'FocusDanger',
    'FocusCoralRedDark': 'FocusDangerDark',
    'FocusGold': 'FocusPrimary',
    'FocusGreen': 'FocusPrimary',
    'FocusPurple': 'FocusSurfaceVariant'
}

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    modified = False
    for old, new in color_map.items():
        if old in content:
            content = content.replace(old, new)
            modified = True
            
    # Also replace whitelistedApps with whitelistedAppsManual for now to fix compile errors
    if "whitelistedApps.value.size" in content:
        content = content.replace("whitelistedApps.value.size", "whitelistedAppsManual.value.size")
        modified = True
    if "whitelistedApps.value" in content:
        content = content.replace("whitelistedApps.value", "whitelistedAppsManual.value")
        modified = True
    if "viewModel.whitelistedApps" in content:
        content = content.replace("viewModel.whitelistedApps", "viewModel.whitelistedAppsManual")
        modified = True

    if modified:
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, _, files in os.walk("app/src/main/java/com/example/ui"):
    for file in files:
        if file.endswith(".kt"):
            process_file(os.path.join(root, file))

