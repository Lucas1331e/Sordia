Add-Type -AssemblyName System.Drawing

$textureFiles = @(
    "yellow_marmol_block.png",
    "yellow_marmol_bricks.png",
    "yellow_marmol_gilded_block.png",
    "yellow_marmol_pillar.png",
    "yellow_marmol_pillar_top.png",
    "deepslate_temporal_polished.png",
    "deepslate_temporal_tiles.png",
    "temporal_deepslate_brick.png",
    "temporal_barrel_side.png",
    "temporal_barrel_top.png",
    "temporal_barrel_bottom.png",
    "suspicious_temporal_sculk1.png",
    "suspicious_temporal_sculk2.png",
    "suspicious_temporal_sculk3.png",
    "suspicious_temporal_sculk4.png",
    "sculk_temporal_bottom.png",
    "sculk_temporal_side.png",
    "sculk_temporal_top.png",
    "sculk_temporal_inner_top.png",
    "sculk_temporal_can_summon_inner_top.png"
)

$baseDir = "e:\Telum\src\main\resources\assets\telum\textures\block"

foreach ($fileName in $textureFiles) {
    $filePath = Join-Path $baseDir $fileName
    if (Test-Path $filePath) {
        $bmp = [System.Drawing.Bitmap]::FromFile($filePath)
        $newBmp = New-Object System.Drawing.Bitmap($bmp.Width, $bmp.Height)
        
        for ($x = 0; $x -lt $bmp.Width; $x++) {
            for ($y = 0; $y -lt $bmp.Height; $y++) {
                $pixel = $bmp.GetPixel($x, $y)
                if ($pixel.A -gt 0) {
                    # Set alpha to 70% opacity (~180 out of 255) for translucent look
                    $alpha = [math]::Min($pixel.A, 180)
                    $newColor = [System.Drawing.Color]::FromArgb($alpha, $pixel.R, $pixel.G, $pixel.B)
                    $newBmp.SetPixel($x, $y, $newColor)
                } else {
                    $newBmp.SetPixel($x, $y, $pixel)
                }
            }
        }
        
        $bmp.Dispose()
        $newBmp.Save($filePath, [System.Drawing.Imaging.ImageFormat]::Png)
        $newBmp.Dispose()
        Write-Host "Made translucent (70% opacity): $fileName"
    } else {
        Write-Host "File not found: $fileName"
    }
}

Write-Host "All temporal textures updated to translucent alpha!"
