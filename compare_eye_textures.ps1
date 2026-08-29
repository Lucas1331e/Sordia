Add-Type -AssemblyName System.Drawing

$files = Get-ChildItem -Path "E:\Telum\src\main\resources\assets\telum\textures\item\part" -Filter "*eye*.png"

foreach ($file in $files) {
    $bmp = [System.Drawing.Bitmap]::FromFile($file.FullName)
    Write-Host "=== $($file.Name) ==="
    for ($y = 0; $y -lt $bmp.Height; $y++) {
        $line = ""
        for ($x = 0; $x -lt $bmp.Width; $x++) {
            $c = $bmp.GetPixel($x, $y)
            if ($c.A -eq 0) {
                $line += " . "
            } else {
                $line += "$($c.R.ToString('X2'))$($c.G.ToString('X2'))$($c.B.ToString('X2')) "
            }
        }
        if ($line.Trim().Length -gt 0) {
            Write-Host "Row ${y}: $line"
        }
    }
    $bmp.Dispose()
}
