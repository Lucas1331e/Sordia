Add-Type -AssemblyName System.Drawing

function New-Texture16x16 {
    param(
        [string]$Path,
        [scriptblock]$DrawAction
    )
    $bmp = New-Object System.Drawing.Bitmap(16, 16)
    for ($x = 0; $x -lt 16; $x++) {
        for ($y = 0; $y -lt 16; $y++) {
            $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, 0, 0, 0))
        }
    }
    & $DrawAction $bmp
    $bmp.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Created: $Path"
}

function Set-Pixel {
    param($bmp, $x, $y, $r, $g, $b, $a = 255)
    if ($x -ge 0 -and $x -lt 16 -and $y -ge 0 -and $y -lt 16) {
        $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($a, $r, $g, $b))
    }
}

function Fill-Rect {
    param($bmp, $x1, $y1, $x2, $y2, $r, $g, $b, $a = 255)
    for ($x = $x1; $x -le $x2; $x++) {
        for ($y = $y1; $y -le $y2; $y++) {
            Set-Pixel $bmp $x $y $r $g $b $a
        }
    }
}

$dir = "e:\Telum\src\main\resources\assets\telum\textures\block"

# TEMPORAL BARREL SIDE
New-Texture16x16 "$dir\temporal_barrel_side.png" {
    param($bmp)
    # Dark Sculk wood background
    Fill-Rect $bmp 0 0 15 15 15 25 35
    # Plank vertical lines
    for ($y = 0; $y -lt 16; $y++) {
        Set-Pixel $bmp 4 $y 10 15 25
        Set-Pixel $bmp 8 $y 10 15 25
        Set-Pixel $bmp 12 $y 10 15 25
    }
    # Metallic cyan hoops (top & bottom)
    Fill-Rect $bmp 0 2 15 3 0 200 220
    Fill-Rect $bmp 0 12 15 13 0 200 220
    # Cyan highlights
    Fill-Rect $bmp 0 2 15 2 0 240 255
    Fill-Rect $bmp 0 12 15 12 0 240 255
    # Sculk temporal emblem in center
    Fill-Rect $bmp 6 6 9 9 0 230 255
    Fill-Rect $bmp 7 7 8 8 255 255 255
}

# TEMPORAL BARREL TOP
New-Texture16x16 "$dir\temporal_barrel_top.png" {
    param($bmp)
    # Dark Sculk wood background
    Fill-Rect $bmp 0 0 15 15 15 25 35
    # Outer ring
    Fill-Rect $bmp 0 0 15 0 0 200 220
    Fill-Rect $bmp 0 15 15 15 0 200 220
    Fill-Rect $bmp 0 0 0 15 0 200 220
    Fill-Rect $bmp 15 0 15 15 0 200 220
    # Inner circular ring cyan glow
    Fill-Rect $bmp 3 3 12 12 0 180 200
    Fill-Rect $bmp 4 4 11 11 15 25 35
    # Center temporal core
    Fill-Rect $bmp 6 6 9 9 0 240 255
    Fill-Rect $bmp 7 7 8 8 255 255 255
}

# TEMPORAL BARREL BOTTOM
New-Texture16x16 "$dir\temporal_barrel_bottom.png" {
    param($bmp)
    # Dark Sculk wood background
    Fill-Rect $bmp 0 0 15 15 15 25 35
    # Outer ring
    Fill-Rect $bmp 0 0 15 0 0 160 180
    Fill-Rect $bmp 0 15 15 15 0 160 180
    Fill-Rect $bmp 0 0 0 15 0 160 180
    Fill-Rect $bmp 15 0 15 15 0 160 180
}

Write-Host "Created temporal barrel textures!"
