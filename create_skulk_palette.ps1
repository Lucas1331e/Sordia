Add-Type -AssemblyName System.Drawing

$palPath = "e:\Telum\src\main\resources\assets\telum\textures\item\palettes\skulk_pallete.png"

$bmp = New-Object System.Drawing.Bitmap(1, 5)

# Palette colors (5 shade steps)
$c0 = [System.Drawing.Color]::FromArgb(255, 186, 227, 227) # Highlight (Sculk cyan)
$c1 = [System.Drawing.Color]::FromArgb(255, 22, 156, 157)  # Light teal
$c2 = [System.Drawing.Color]::FromArgb(255, 13, 98, 114)   # Medium teal
$c3 = [System.Drawing.Color]::FromArgb(255, 5, 36, 42)     # Deep shadow
$c4 = [System.Drawing.Color]::FromArgb(255, 2, 11, 16)     # Outline dark

$bmp.SetPixel(0, 0, $c0)
$bmp.SetPixel(0, 1, $c1)
$bmp.SetPixel(0, 2, $c2)
$bmp.SetPixel(0, 3, $c3)
$bmp.SetPixel(0, 4, $c4)

$bmp.Save($palPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()

Write-Host "Created skulk_pallete.png at $palPath"
