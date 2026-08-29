Add-Type -AssemblyName System.Drawing

$texDir = "e:\Telum\src\main\resources\assets\telum\textures\block"

$texturePairs = @(
    @{ src = "marmol_block.png";        dst = "yellow_marmol_block.png" },
    @{ src = "marmol_bricks.png";       dst = "yellow_marmol_bricks.png" },
    @{ src = "marmol_gilded_block.png"; dst = "yellow_marmol_gilded_block.png" },
    @{ src = "marmol_pillar.png";       dst = "yellow_marmol_pillar.png" },
    @{ src = "marmol_pillar_top.png";   dst = "yellow_marmol_pillar_top.png" }
)

foreach ($pair in $texturePairs) {
    $srcPath = Join-Path $texDir $pair.src
    $dstPath = Join-Path $texDir $pair.dst

    if (!(Test-Path $srcPath)) {
        Write-Error "Source texture not found: $srcPath"
        continue
    }

    # Load original marble bitmap
    $srcImg = [System.Drawing.Bitmap]::FromFile($srcPath)
    $dstBmp = New-Object System.Drawing.Bitmap($srcImg.Width, $srcImg.Height)

    for ($x = 0; $x -lt $srcImg.Width; $x++) {
        for ($y = 0; $y -lt $srcImg.Height; $y++) {
            $px = $srcImg.GetPixel($x, $y)

            if ($px.A -eq 0) {
                $dstBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
            } else {
                # Soft, warm golden yellow tint (less garish / less bright)
                $r = [Math]::Min(255, [int]($px.R * 0.98))
                $g = [Math]::Min(255, [int]($px.G * 0.90))
                $b = [Math]::Min(255, [int]($px.B * 0.58))

                # Make translucent (Alpha = 70% of original alpha or ~175)
                $a = [int]($px.A * 0.70)

                $dstBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($a, $r, $g, $b))
            }
        }
    }

    $srcImg.Dispose()
    # Save tinted texture
    $dstBmp.Save($dstPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $dstBmp.Dispose()
    Write-Host "Generated yellow translucent texture from original: $dstPath"
}

Write-Host "All yellow translucent marble textures updated using exact marble base textures!"
