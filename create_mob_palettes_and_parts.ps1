Add-Type -AssemblyName System.Drawing

$baseDir = "e:\Telum\src\main\resources\assets\telum"
$palDir  = "$baseDir\textures\item\palettes"
$partsDir= "$baseDir\textures\item\part"
$tmplDir = "$baseDir\textures\item\templates"
$itemsDir= "$baseDir\items"
$modelsDir= "$baseDir\models\item"

New-Item -ItemType Directory -Force -Path $palDir | Out-Null
New-Item -ItemType Directory -Force -Path $partsDir | Out-Null
New-Item -ItemType Directory -Force -Path $itemsDir | Out-Null
New-Item -ItemType Directory -Force -Path $modelsDir | Out-Null

# 1. Create/Update Palettes
function Create-Palette($fileName, $hexColors) {
    $path = Join-Path $palDir $fileName
    $bmp = New-Object System.Drawing.Bitmap(1, $hexColors.Length)
    for ($y = 0; $y -lt $hexColors.Length; $y++) {
        $c = [System.Drawing.ColorTranslator]::FromHtml($hexColors[$y])
        $bmp.SetPixel(0, $y, $c)
    }
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Created palette: $fileName"
}

Create-Palette "spider_palette.png"   @("#20080C", "#4A1018", "#8B1E2B", "#D03243")
Create-Palette "skeleton_palette.png" @("#4A4A4A", "#858585", "#C8C8C8", "#F0F0F0")
Create-Palette "zombie_palette.png"   @("#1B2D16", "#345228", "#578243", "#8BB870")
Create-Palette "creeper_palette.png"  @("#143818", "#28632E", "#4BA653", "#82E88A")
Create-Palette "enderman_palette.png" @("#0A0612", "#1A102C", "#3D1B5E", "#B545FF")

# 2. Map Palette function
function MapPalette($tmplFile, $palFile, $outFile) {
    $tmplPath = Join-Path $tmplDir $tmplFile
    $palPath  = Join-Path $palDir $palFile
    $outPath  = Join-Path $partsDir $outFile

    if (!(Test-Path $tmplPath)) {
        Write-Warning "Template missing: $tmplFile"
        return
    }
    if (!(Test-Path $palPath)) {
        Write-Warning "Palette missing: $palFile"
        return
    }

    $tmplBmp = [System.Drawing.Bitmap]::FromFile($tmplPath)
    $palBmp  = [System.Drawing.Bitmap]::FromFile($palPath)

    $grayValues = [System.Collections.Generic.List[int]]::new()
    for ($x = 0; $x -lt $tmplBmp.Width; $x++) {
        for ($y = 0; $y -lt $tmplBmp.Height; $y++) {
            $px = $tmplBmp.GetPixel($x, $y)
            if ($px.A -gt 0 -and !$grayValues.Contains($px.R)) {
                $grayValues.Add($px.R)
            }
        }
    }
    $grayValues.Sort()
    $grayValues.Reverse()

    $outBmp = New-Object System.Drawing.Bitmap($tmplBmp.Width, $tmplBmp.Height)

    for ($x = 0; $x -lt $tmplBmp.Width; $x++) {
        for ($y = 0; $y -lt $tmplBmp.Height; $y++) {
            $px = $tmplBmp.GetPixel($x, $y)
            if ($px.A -eq 0) {
                $outBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
            } else {
                $rankIndex = $grayValues.IndexOf($px.R)
                if ($rankIndex -lt 0) {
                    $bestDist = 999
                    $rankIndex = 0
                    for ($i = 0; $i -lt $grayValues.Count; $i++) {
                        $dist = [Math]::Abs($px.R - $grayValues[$i])
                        if ($dist -lt $bestDist) {
                            $bestDist = $dist
                            $rankIndex = $i
                        }
                    }
                }

                $palY = [Math]::Min($rankIndex, $palBmp.Height - 1)
                $targetColor = $palBmp.GetPixel(0, $palY)
                $finalColor = [System.Drawing.Color]::FromArgb($px.A, $targetColor.R, $targetColor.G, $targetColor.B)
                $outBmp.SetPixel($x, $y, $finalColor)
            }
        }
    }

    $tmplBmp.Dispose()
    $palBmp.Dispose()

    $outBmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $outBmp.Dispose()
    Write-Host "Generated texture: $outFile"
}

# Generate 5 mob part textures
MapPalette "eye_template.png"          "spider_palette.png"   "eye_spider.png"
MapPalette "stick_template.png"        "skeleton_palette.png" "handle_skeleton.png"
MapPalette "handle_template.png"       "zombie_palette.png"   "grip_zombie.png"
MapPalette "head_generic_template.png" "creeper_palette.png"  "head_creeper.png"
MapPalette "head_generic_template.png" "creeper_palette.png"  "creeper_trident_head.png"
MapPalette "stick_template.png"        "enderman_palette.png" "handle_enderman.png"

# Copy existing exclusive creeper trident head if present
$existingToolHead = "$baseDir\textures\item\tool\creeper_trident_head.png"
if (Test-Path $existingToolHead) {
    Copy-Item $existingToolHead -Destination "$partsDir\creeper_trident_head.png" -Force
    Copy-Item $existingToolHead -Destination "$partsDir\head_creeper.png" -Force
    Write-Host "Copied custom creeper trident head to parts directory!"
}

# 3. Generate Item JSONs and Model JSONs
$mobParts = @("eye_spider", "handle_skeleton", "grip_zombie", "head_creeper", "handle_enderman")

foreach ($p in $mobParts) {
    # Item JSON
    $itemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/${p}"
  }
}
"@
    Set-Content -Path "$itemsDir\${p}.json" -Value $itemJson -Encoding UTF8

    # Model JSON
    $modelJson = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/part/${p}"
  }
}
"@
    Set-Content -Path "$modelsDir\${p}.json" -Value $modelJson -Encoding UTF8
    Write-Host "Created JSON assets for ${p}"
}

Write-Host "Mob palettes, textures, and JSON assets successfully generated!"
