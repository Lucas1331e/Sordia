$blockstatesDir = "e:\Telum\src\main\resources\assets\telum\blockstates"
$modelsBlockDir = "e:\Telum\src\main\resources\assets\telum\models\block"
$modelsItemDir  = "e:\Telum\src\main\resources\assets\telum\models\item"
$itemsDir       = "e:\Telum\src\main\resources\assets\telum\items"
$texturesDir    = "e:\Telum\src\main\resources\assets\telum\textures\block"

New-Item -ItemType Directory -Force -Path $blockstatesDir, $modelsBlockDir, $modelsItemDir, $itemsDir, $texturesDir | Out-Null

# 1. Blockstate
$blockstateJson = @"
{
  "variants": {
    "": {
      "model": "telum:block/cleaning_table"
    }
  }
}
"@
Set-Content -Path "$blockstatesDir\cleaning_table.json" -Value $blockstateJson -Encoding UTF8
Write-Host "Created blockstate: cleaning_table.json"

# 2. Block model (slab-like: 12 pixels tall)
$blockModelJson = @"
{
  "parent": "minecraft:block/block",
  "textures": {
    "top": "telum:block/cleaning_table_top",
    "side": "telum:block/cleaning_table_side",
    "bottom": "telum:block/cleaning_table_bottom",
    "particle": "telum:block/cleaning_table_side"
  },
  "elements": [
    {
      "from": [0, 0, 0],
      "to": [16, 12, 16],
      "faces": {
        "up":    { "texture": "#top",    "cullface": "up" },
        "down":  { "texture": "#bottom", "cullface": "down" },
        "north": { "texture": "#side",   "cullface": "north" },
        "south": { "texture": "#side",   "cullface": "south" },
        "east":  { "texture": "#side",   "cullface": "east" },
        "west":  { "texture": "#side",   "cullface": "west" }
      }
    }
  ]
}
"@
Set-Content -Path "$modelsBlockDir\cleaning_table.json" -Value $blockModelJson -Encoding UTF8
Write-Host "Created block model: cleaning_table.json"

# 3. Item definition (items/cleaning_table.json)
$itemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/cleaning_table"
  }
}
"@
Set-Content -Path "$itemsDir\cleaning_table.json" -Value $itemJson -Encoding UTF8
Write-Host "Created item definition: cleaning_table.json"

# 4. Placeholder 16x16 PNG textures
# These are minimal valid PNG files (1x1 pixel stretched, just placeholders)

function New-PlaceholderPng {
    param([string]$path, [byte]$r, [byte]$g, [byte]$b)

    # Minimal 16x16 PNG with a solid color
    # We'll use System.Drawing to create a proper PNG
    Add-Type -AssemblyName System.Drawing
    $bmp = New-Object System.Drawing.Bitmap(16, 16)
    $color = [System.Drawing.Color]::FromArgb(255, $r, $g, $b)
    for ($x = 0; $x -lt 16; $x++) {
        for ($y = 0; $y -lt 16; $y++) {
            $bmp.SetPixel($x, $y, $color)
        }
    }
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Created placeholder texture: $(Split-Path $path -Leaf)"
}

# Top: light oak color (193, 154, 107)
New-PlaceholderPng -path "$texturesDir\cleaning_table_top.png" -r 193 -g 154 -b 107

# Side: darker wood (139, 105, 70)
New-PlaceholderPng -path "$texturesDir\cleaning_table_side.png" -r 139 -g 105 -b 70

# Bottom: dark wood planks (105, 75, 50)
New-PlaceholderPng -path "$texturesDir\cleaning_table_bottom.png" -r 105 -g 75 -b 50

Write-Host "`nCleaning Table assets created successfully!"
