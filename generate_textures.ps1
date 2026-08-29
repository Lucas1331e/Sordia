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

$tierColors = @{
    1 = @{ r=139; g=90; b=60; lr=180; lg=140; lb=100; dr=100; dg=60; db=30 }
    2 = @{ r=192; g=192; b=192; lr=220; lg=220; lb=220; dr=140; dg=140; db=140 }
    3 = @{ r=80; g=220; b=200; lr=130; lg=240; lb=230; dr=40; dg=180; db=160 }
    4 = @{ r=140; g=70; b=170; lr=180; lg=110; lb=210; dr=90; dg=40; db=120 }
}

$basePath = "e:\Telum\src\main\resources\assets\telum\textures\item\part"

foreach ($tier in 1..4) {
    $c = $tierColors[$tier]

    # HANDLE
    New-Texture16x16 "$basePath\handle_t${tier}.png" {
        param($bmp)
        for ($i = 0; $i -lt 10; $i++) {
            $x = 3 + $i
            $y = 15 - $i
            Set-Pixel $bmp $x $y $c.r $c.g $c.b
            Set-Pixel $bmp ($x+1) $y $c.lr $c.lg $c.lb
            Set-Pixel $bmp $x ($y-1) $c.dr $c.dg $c.db
        }
    }

    # GRIP
    New-Texture16x16 "$basePath\grip_t${tier}.png" {
        param($bmp)
        for ($i = 0; $i -lt 7; $i++) {
            $x = 3 + $i
            $y = 15 - $i
            Set-Pixel $bmp $x $y $c.r $c.g $c.b
            Set-Pixel $bmp ($x+1) $y $c.lr $c.lg $c.lb
            Set-Pixel $bmp ($x+1) ($y-1) $c.r $c.g $c.b
            Set-Pixel $bmp $x ($y-1) $c.dr $c.dg $c.db
        }
        Fill-Rect $bmp 2 14 4 15 $c.dr $c.dg $c.db
        Fill-Rect $bmp 3 14 3 14 $c.lr $c.lg $c.lb
        Fill-Rect $bmp 8 9 12 10 $c.r $c.g $c.b
    }

    # EYE
    New-Texture16x16 "$basePath\eye_t${tier}.png" {
        param($bmp)
        $cx = 7; $cy = 7
        Set-Pixel $bmp $cx ($cy-1) $c.lr $c.lg $c.lb
        Set-Pixel $bmp ($cx-1) $cy $c.r $c.g $c.b
        Set-Pixel $bmp $cx $cy 255 255 255
        Set-Pixel $bmp ($cx+1) $cy $c.r $c.g $c.b
        Set-Pixel $bmp $cx ($cy+1) $c.dr $c.dg $c.db
        Set-Pixel $bmp ($cx-1) ($cy-1) $c.dr $c.dg $c.db
        Set-Pixel $bmp ($cx+1) ($cy-1) $c.dr $c.dg $c.db
        Set-Pixel $bmp ($cx-1) ($cy+1) $c.dr $c.dg $c.db
        Set-Pixel $bmp ($cx+1) ($cy+1) $c.dr $c.dg $c.db
    }

    # UNIFIED HEAD (universal tool head)
    New-Texture16x16 "$basePath\head_t${tier}.png" {
        param($bmp)
        Fill-Rect $bmp 3 2 11 4 $c.r $c.g $c.b
        Fill-Rect $bmp 4 2 10 3 $c.lr $c.lg $c.lb
        Fill-Rect $bmp 5 4 9 4 $c.dr $c.dg $c.db
        # Prongs
        Set-Pixel $bmp 2 3 $c.lr $c.lg $c.lb
        Set-Pixel $bmp 12 3 $c.lr $c.lg $c.lb
        Set-Pixel $bmp 1 4 $c.r $c.g $c.b
        Set-Pixel $bmp 13 4 $c.r $c.g $c.b
    }

    # BLADE
    New-Texture16x16 "$basePath\blade_t${tier}.png" {
        param($bmp)
        for ($i = 0; $i -lt 8; $i++) {
            $x = 9 + $i
            $y = 8 - $i
            if ($x -lt 16 -and $y -ge 0) {
                Set-Pixel $bmp $x $y $c.lr $c.lg $c.lb
                Set-Pixel $bmp ($x-1) $y $c.r $c.g $c.b
                Set-Pixel $bmp $x ($y+1) $c.dr $c.dg $c.db
            }
        }
        Set-Pixel $bmp 14 1 255 255 255 200
        Set-Pixel $bmp 15 0 255 255 255 200
    }
}

Write-Host "Part textures generated!"
