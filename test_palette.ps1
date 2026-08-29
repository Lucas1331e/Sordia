Add-Type -AssemblyName System.Drawing
$palPath = "e:\Telum\src\main\resources\assets\telum\textures\item\part\gold_pallete.png"
$pal = [System.Drawing.Bitmap]::FromFile($palPath)
Write-Host "Palette size: $($pal.Width) x $($pal.Height)"
for ($y = 0; $y -lt $pal.Height; $y++) {
    for ($x = 0; $x -lt $pal.Width; $x++) {
        $c = $pal.GetPixel($x, $y)
        Write-Host "Pixel ($x, $y) : R=$($c.R), G=$($c.G), B=$($c.B), A=$($c.A)"
    }
}
$pal.Dispose()
