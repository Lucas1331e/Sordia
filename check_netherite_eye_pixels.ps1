Add-Type -AssemblyName System.Drawing

$path = "E:\Telum\src\main\resources\assets\telum\textures\item\part\netherite_eye.png"
if (Test-Path $path) {
    $bmp = [System.Drawing.Bitmap]::FromFile($path)
    Write-Host "Width: $($bmp.Width), Height: $($bmp.Height)"
    for ($y = 0; $y -lt $bmp.Height; $y++) {
        $line = ""
        for ($x = 0; $x -lt $bmp.Width; $x++) {
            $c = $bmp.GetPixel($x, $y)
            if ($c.A -eq 0) {
                $line += " . "
            } else {
                $line += "$($c.R.ToString('X2')) "
            }
        }
        Write-Host $line
    }
    $bmp.Dispose()
}
