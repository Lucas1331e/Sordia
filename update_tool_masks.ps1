Add-Type -AssemblyName System.Drawing

function New-Texture16x16 {
    param(
        [string]$Path,
        [scriptblock]$DrawAction
    )
    $bmp = New-Object System.Drawing.Bitmap(16, 16)
    for ($x = 0; $x -lt 16; $x++) {
        for ($y = 0; $y -lt 16; $y++) {
            $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
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

$toolPath = "e:\Telum\src\main\resources\assets\telum\textures\item\tool"

# Tool base (Layer 0 - Handle/Grip mask)
New-Texture16x16 "$toolPath\tool_base.png" {
    param($bmp)
    for ($i = 0; $i -lt 10; $i++) {
        $x = 3 + $i
        $y = 15 - $i
        Set-Pixel $bmp $x $y 255 255 255
        Set-Pixel $bmp ($x+1) $y 220 220 220
        Set-Pixel $bmp $x ($y-1) 180 180 180
    }
}

# Tool eye (Layer 1 - Core Eye mask)
New-Texture16x16 "$toolPath\tool_eye.png" {
    param($bmp)
    $cx = 7; $cy = 7
    Set-Pixel $bmp $cx ($cy-1) 255 255 255
    Set-Pixel $bmp ($cx-1) $cy 230 230 230
    Set-Pixel $bmp $cx $cy 255 255 255
    Set-Pixel $bmp ($cx+1) $cy 230 230 230
    Set-Pixel $bmp $cx ($cy+1) 180 180 180
    Set-Pixel $bmp ($cx-1) ($cy-1) 180 180 180
    Set-Pixel $bmp ($cx+1) ($cy-1) 180 180 180
    Set-Pixel $bmp ($cx-1) ($cy+1) 180 180 180
    Set-Pixel $bmp ($cx+1) ($cy+1) 180 180 180
}

# Tool head (Layer 2 - Head/Blade mask)
New-Texture16x16 "$toolPath\tool_head.png" {
    param($bmp)
    Fill-Rect $bmp 3 2 11 4 255 255 255
    Fill-Rect $bmp 4 2 10 3 230 230 230
    Fill-Rect $bmp 5 4 9 4 180 180 180
    Set-Pixel $bmp 2 3 255 255 255
    Set-Pixel $bmp 12 3 255 255 255
    Set-Pixel $bmp 1 4 220 220 220
    Set-Pixel $bmp 13 4 220 220 220
}

Write-Host "Updated tool base/eye/head layer textures!"
