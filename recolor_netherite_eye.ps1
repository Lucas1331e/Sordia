Add-Type -AssemblyName System.Drawing

$eyePath = "E:\Telum\src\main\resources\assets\telum\textures\item\part\netherite_eye.png"
$eyeTridentPath = "E:\Telum\src\main\resources\assets\telum\textures\item\part\netherite_eye_trident.png"

# Palette mapping from old wrong brownish/copper colors to Netherite dark charcoal
# 86 4F 32 -> 5A 50 52 (highlight)
# 4F 86 .. -> 4D 43 45 (base)
# 32 .. .. -> 37 30 32 (shadow)
# 11 1B 21 -> 25 20 21 (deep shadow)

function Recolor-NetheriteEye($path) {
    if (-not (Test-Path $path)) { return }
    $bmp = [System.Drawing.Bitmap]::FromFile($path)
    $newBmp = new-object System.Drawing.Bitmap($bmp.Width, $bmp.Height)
    
    for ($y = 0; $y -lt $bmp.Height; $y++) {
        for ($x = 0; $x -lt $bmp.Width; $x++) {
            $c = $bmp.GetPixel($x, $y)
            if ($c.A -gt 0) {
                # Determine brightness or old color pattern and recolor to netherite charcoal
                if ($c.R -gt 100) {
                    # Bright highlight pixel
                    $newColor = [System.Drawing.Color]::FromArgb($c.A, 0x5A, 0x50, 0x52)
                } elseif ($c.R -gt 60) {
                    # Midtone pixel
                    $newColor = [System.Drawing.Color]::FromArgb($c.A, 0x4D, 0x43, 0x45)
                } else {
                    # Dark shadow pixel
                    $newColor = [System.Drawing.Color]::FromArgb($c.A, 0x37, 0x30, 0x32)
                }
                $newBmp.SetPixel($x, $y, $newColor)
            }
        }
    }
    $bmp.Dispose()
    $newBmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $newBmp.Dispose()
    Write-Host "Recolored $path successfully."
}

Recolor-NetheriteEye $eyePath
Recolor-NetheriteEye $eyeTridentPath
