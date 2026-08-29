Add-Type -AssemblyName System.Drawing

$srcPath = "e:\Telum\src\main\resources\assets\telum\textures\item\templates\stick_template.png"
$dstPath = "e:\Telum\src\main\resources\assets\telum\textures\item\templates\stick_sword_template.png"

$srcBmp = [System.Drawing.Bitmap]::FromFile($srcPath)
$dstBmp = New-Object System.Drawing.Bitmap($srcBmp.Width, $srcBmp.Height)

for ($x = 0; $x -lt $srcBmp.Width; $x++) {
    for ($y = 0; $y -lt $srcBmp.Height; $y++) {
        # Shift 1 pixel to the left (read from x + 1)
        if ($x + 1 -lt $srcBmp.Width) {
            $px = $srcBmp.GetPixel($x + 1, $y)
            $dstBmp.SetPixel($x, $y, $px)
        } else {
            $dstBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
        }
    }
}

$srcBmp.Dispose()
$dstBmp.Save($dstPath, [System.Drawing.Imaging.ImageFormat]::Png)
$dstBmp.Dispose()

Write-Host "Created stick_sword_template.png (shifted 1px left) at $dstPath"
