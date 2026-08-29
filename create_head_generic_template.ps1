Add-Type -AssemblyName System.Drawing

$dstPath = "e:\Telum\src\main\resources\assets\telum\textures\item\templates\head_generic_template.png"

$bmp = New-Object System.Drawing.Bitmap(16, 16)

# Colors: 255 (white highlight), 173 (light gray), 112 (mid gray), 61 (dark gray), 20 (black outline)
$w = [System.Drawing.Color]::FromArgb(255, 255, 255, 255)
$l = [System.Drawing.Color]::FromArgb(255, 173, 173, 173)
$m = [System.Drawing.Color]::FromArgb(255, 112, 112, 112)
$d = [System.Drawing.Color]::FromArgb(255, 61, 61, 61)
$b = [System.Drawing.Color]::FromArgb(255, 20, 20, 20)
$t = [System.Drawing.Color]::FromArgb(0, 0, 0, 0)

# Fill transparent
for ($x=0; $x -lt 16; $x++) {
    for ($y=0; $y -lt 16; $y++) {
        $bmp.SetPixel($x, $y, $t)
    }
}

# Draw generic tool head sprite (diagonal pickaxe/axe head shape)
# Row 3: outline
$bmp.SetPixel(9, 3, $b); $bmp.SetPixel(10, 3, $b); $bmp.SetPixel(11, 3, $b)
# Row 4:
$bmp.SetPixel(7, 4, $b); $bmp.SetPixel(8, 4, $b); $bmp.SetPixel(9, 4, $w); $bmp.SetPixel(10, 4, $l); $bmp.SetPixel(11, 4, $m); $bmp.SetPixel(12, 4, $b)
# Row 5:
$bmp.SetPixel(5, 5, $b); $bmp.SetPixel(6, 5, $b); $bmp.SetPixel(7, 5, $w); $bmp.SetPixel(8, 5, $w); $bmp.SetPixel(9, 5, $l); $bmp.SetPixel(10, 5, $m); $bmp.SetPixel(11, 5, $d); $bmp.SetPixel(12, 5, $b)
# Row 6:
$bmp.SetPixel(4, 6, $b); $bmp.SetPixel(5, 6, $w); $bmp.SetPixel(6, 6, $w); $bmp.SetPixel(7, 6, $l); $bmp.SetPixel(8, 6, $m); $bmp.SetPixel(9, 6, $d); $bmp.SetPixel(10, 6, $b)
# Row 7:
$bmp.SetPixel(3, 7, $b); $bmp.SetPixel(4, 7, $l); $bmp.SetPixel(5, 7, $l); $bmp.SetPixel(6, 7, $m); $bmp.SetPixel(7, 7, $d); $bmp.SetPixel(8, 7, $b)
# Row 8:
$bmp.SetPixel(3, 8, $b); $bmp.SetPixel(4, 8, $m); $bmp.SetPixel(5, 8, $m); $bmp.SetPixel(6, 8, $d); $bmp.SetPixel(7, 8, $b)
# Row 9: socket / connector
$bmp.SetPixel(4, 9, $b); $bmp.SetPixel(5, 9, $d); $bmp.SetPixel(6, 9, $b)
$bmp.SetPixel(5, 10, $b)

$bmp.Save($dstPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()

Write-Host "Created head_generic_template.png at $dstPath"
