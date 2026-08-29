Add-Type -AssemblyName System.Drawing

$baseDir = "e:\Telum\src\main\resources\assets\telum"

# Helper function to write PNG
function Create-BarrelTexture($path, $topColor, $accentColor) {
    $bmp = New-Object System.Drawing.Bitmap 16, 16
    $rng = New-Object System.Random

    for ($x = 0; $x -lt 16; $x++) {
        for ($y = 0; $y -lt 16; $y++) {
            # Barrel plank pattern
            $isRim = ($y -eq 0 -or $y -eq 1 -or $y -eq 14 -or $y -eq 15)
            $isStave = ($x -eq 0 -or $x -eq 4 -or $x -eq 8 -or $x -eq 12)
            $isMetalRing = ($y -eq 3 -or $y -eq 4 -or $y -eq 11 -or $y -eq 12)

            if ($isMetalRing) {
                # Cyan/Gold metallic ring
                $r = [Math]::Min(255, $accentColor.R + $rng.Next(-15, 15))
                $g = [Math]::Min(255, $accentColor.G + $rng.Next(-15, 15))
                $b = [Math]::Min(255, $accentColor.B + $rng.Next(-15, 15))
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $r, $g, $b))
            } elseif ($isStave -or $isRim) {
                # Dark stave border
                $r = [Math]::Max(0, $topColor.R - 40 + $rng.Next(-10, 10))
                $g = [Math]::Max(0, $topColor.G - 40 + $rng.Next(-10, 10))
                $b = [Math]::Max(0, $topColor.B - 40 + $rng.Next(-10, 10))
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $r, $g, $b))
            } else {
                # Plank body
                $r = [Math]::Min(255, [Math]::Max(0, $topColor.R + $rng.Next(-20, 20)))
                $g = [Math]::Min(255, [Math]::Max(0, $topColor.G + $rng.Next(-20, 20)))
                $b = [Math]::Min(255, [Math]::Max(0, $topColor.B + $rng.Next(-20, 20)))
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $r, $g, $b))
            }
        }
    }
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

# Function to write Translucent PNG
function Create-TranslucentBarrelTexture($path, $topColor, $accentColor) {
    $bmp = New-Object System.Drawing.Bitmap 16, 16
    $rng = New-Object System.Random

    for ($x = 0; $x -lt 16; $x++) {
        for ($y = 0; $y -lt 16; $y++) {
            $isMetalRing = ($y -eq 3 -or $y -eq 4 -or $y -eq 11 -or $y -eq 12)
            $alpha = 175 # 70% opacity

            if ($isMetalRing) {
                $r = [Math]::Min(255, $accentColor.R + $rng.Next(-15, 15))
                $g = [Math]::Min(255, $accentColor.G + $rng.Next(-15, 15))
                $b = [Math]::Min(255, $accentColor.B + $rng.Next(-15, 15))
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, $r, $g, $b))
            } else {
                $r = [Math]::Min(255, [Math]::Max(0, $topColor.R + $rng.Next(-20, 20)))
                $g = [Math]::Min(255, [Math]::Max(0, $topColor.G + $rng.Next(-20, 20)))
                $b = [Math]::Min(255, [Math]::Max(0, $topColor.B + $rng.Next(-20, 20)))
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, $r, $g, $b))
            }
        }
    }
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

# Colors for Echo Barrel (Dark Teal Planks with Golden Metal Rings)
$tealColor = [System.Drawing.Color]::FromArgb(255, 30, 80, 100)
$goldColor = [System.Drawing.Color]::FromArgb(255, 230, 180, 50)

Create-BarrelTexture "$baseDir\textures\block\echo_barrel_side.png" $tealColor $goldColor
Create-BarrelTexture "$baseDir\textures\block\echo_barrel_top.png" $tealColor $goldColor
Create-BarrelTexture "$baseDir\textures\block\echo_barrel_bottom.png" $tealColor $goldColor
Create-TranslucentBarrelTexture "$baseDir\textures\block\echo_barrel_projection_side.png" $tealColor $goldColor
Create-TranslucentBarrelTexture "$baseDir\textures\block\echo_barrel_projection_top.png" $tealColor $goldColor

Write-Host "Created Echo Barrel textures successfully!"
