Add-Type -AssemblyName System.Drawing

$dir = "e:\Telum\src\main\resources\assets\telum\textures\item\part"
$sticks = @("wood_stick", "stone_stick", "copper_stick", "iron_stick", "gold_stick", "diamond_stick", "netherite_stick")

foreach ($s in $sticks) {
    $srcPath = "$dir\$s.png"
    $dstPath = "$dir\${s}_sword.png"

    $img = [System.Drawing.Image]::FromFile($srcPath)
    $src = New-Object System.Drawing.Bitmap($img)
    $img.Dispose()

    $w = $src.Width
    $h = $src.Height
    $dst = New-Object System.Drawing.Bitmap($w, $h)

    for ($x = 0; $x -lt $w; $x++) {
        for ($y = 0; $y -lt $h; $y++) {
            $col = $src.GetPixel($x, $y)
            if ($col.A -gt 0) {
                $nx = $x - 1
                if ($nx -ge 0) {
                    $dst.SetPixel($nx, $y, $col)
                }
            }
        }
    }

    $src.Dispose()
    $dst.Save($dstPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $dst.Dispose()
    Write-Host "Created shifted stick texture for sword: $dstPath"
}
