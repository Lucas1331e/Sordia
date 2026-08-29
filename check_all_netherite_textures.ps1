Add-Type -AssemblyName System.Drawing

$files = Get-ChildItem -Path "E:\Telum\src\main\resources\assets\telum\textures\item\part" -Filter "*netherite*.png"

foreach ($file in $files) {
    $bmp = [System.Drawing.Bitmap]::FromFile($file.FullName)
    Write-Host "=== $($file.Name) (Width: $($bmp.Width), Height: $($bmp.Height)) ==="
    $nonEmpty = 0
    $avgR = 0; $avgG = 0; $avgB = 0
    for ($y = 0; $y -lt $bmp.Height; $y++) {
        for ($x = 0; $x -lt $bmp.Width; $x++) {
            $c = $bmp.GetPixel($x, $y)
            if ($c.A -gt 0) {
                $nonEmpty++
                $avgR += $c.R
                $avgG += $c.G
                $avgB += $c.B
            }
        }
    }
    if ($nonEmpty -gt 0) {
        $avgR = [math]::Round($avgR / $nonEmpty)
        $avgG = [math]::Round($avgG / $nonEmpty)
        $avgB = [math]::Round($avgB / $nonEmpty)
        Write-Host "Average Color: R=$avgR, G=$avgG, B=$avgB (Hex: #$($avgR.ToString('X2'))$($avgG.ToString('X2'))$($avgB.ToString('X2')))"
    }
    $bmp.Dispose()
}
