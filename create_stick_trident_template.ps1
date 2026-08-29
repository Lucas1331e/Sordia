Add-Type -AssemblyName System.Drawing

$srcPath = "e:\Telum\src\main\resources\assets\telum\textures\item\part\prismarine_stick_trident.png"
$dstPath = "e:\Telum\src\main\resources\assets\telum\textures\item\templates\stick_trident_template.png"

$srcBmp = [System.Drawing.Bitmap]::FromFile($srcPath)
$dstBmp = New-Object System.Drawing.Bitmap($srcBmp.Width, $srcBmp.Height)

# Grayscale palette steps matching template palette (255, 173, 112, 61, 20)
# Collect colors from prismarine_stick_trident.png
$colors = [System.Collections.Generic.List[int]]::new()
for ($x = 0; $x -lt $srcBmp.Width; $x++) {
    for ($y = 0; $y -lt $srcBmp.Height; $y++) {
        $px = $srcBmp.GetPixel($x, $y)
        if ($px.A -gt 0 -and !$colors.Contains($px.G)) {
            $colors.Add($px.G)
        }
    }
}
$colors.Sort()
$colors.Reverse()

Write-Host "Prismarine stick trident G levels (lightest to darkest): $($colors -join ', ')"

# Map to standard template grayscale steps: 255, 173, 112, 61, 20
$graySteps = @(255, 173, 112, 61, 20)

for ($x = 0; $x -lt $srcBmp.Width; $x++) {
    for ($y = 0; $y -lt $srcBmp.Height; $y++) {
        $px = $srcBmp.GetPixel($x, $y)
        if ($px.A -eq 0) {
            $dstBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
        } else {
            $rankIndex = $colors.IndexOf($px.G)
            if ($rankIndex -lt 0) { $rankIndex = 0 }
            $gStep = $graySteps[[Math]::Min($rankIndex, $graySteps.Count - 1)]
            $dstBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($px.A, $gStep, $gStep, $gStep))
        }
    }
}

$srcBmp.Dispose()
$dstBmp.Save($dstPath, [System.Drawing.Imaging.ImageFormat]::Png)
$dstBmp.Dispose()

Write-Host "Created stick_trident_template.png at $dstPath"
