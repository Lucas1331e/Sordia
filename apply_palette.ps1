param(
    [string]$templatePath = "e:\Telum\src\main\resources\assets\telum\textures\item\part\head_axe_template.png",
    [string]$palettePath  = "e:\Telum\src\main\resources\assets\telum\textures\item\part\gold_pallete.png",
    [string]$outputPath   = "e:\Telum\src\main\resources\assets\telum\textures\item\part\golden_axe_head.png"
)

Add-Type -AssemblyName System.Drawing

if (!(Test-Path $templatePath) -or !(Test-Path $palettePath)) {
    Write-Error "Template or palette file not found!"
    exit 1
}

$tmplBmp = [System.Drawing.Bitmap]::FromFile($templatePath)
$palBmp  = [System.Drawing.Bitmap]::FromFile($palettePath)

# Collect all non-transparent grayscale R values from template and sort descending (lightest to darkest)
$grayValues = [System.Collections.Generic.List[int]]::new()
for ($x = 0; $x -lt $tmplBmp.Width; $x++) {
    for ($y = 0; $y -lt $tmplBmp.Height; $y++) {
        $px = $tmplBmp.GetPixel($x, $y)
        if ($px.A -gt 0 -and !$grayValues.Contains($px.R)) {
            $grayValues.Add($px.R)
        }
    }
}
$grayValues.Sort()
$grayValues.Reverse() # Index 0 = brightest (255), Index 4 = darkest (20)

Write-Host "Template grayscale levels (lightest to darkest): $($grayValues -join ', ')"
Write-Host "Palette height: $($palBmp.Height) colors"

$outBmp = New-Object System.Drawing.Bitmap($tmplBmp.Width, $tmplBmp.Height)

for ($x = 0; $x -lt $tmplBmp.Width; $x++) {
    for ($y = 0; $y -lt $tmplBmp.Height; $y++) {
        $px = $tmplBmp.GetPixel($x, $y)
        if ($px.A -eq 0) {
            $outBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
        } else {
            # Find closest grayscale rank index
            $rankIndex = $grayValues.IndexOf($px.R)
            if ($rankIndex -lt 0) {
                # Fallback to nearest index
                $bestDist = 999
                $rankIndex = 0
                for ($i = 0; $i -lt $grayValues.Count; $i++) {
                    $dist = [Math]::Abs($px.R - $grayValues[$i])
                    if ($dist -lt $bestDist) {
                        $bestDist = $dist
                        $rankIndex = $i
                    }
                }
            }

            # Map rank index (0..4) to palette y position (0..paletteHeight-1)
            $palY = [Math]::Min($rankIndex, $palBmp.Height - 1)
            $targetColor = $palBmp.GetPixel(0, $palY)

            # Preserve alpha channel from template pixel
            $finalColor = [System.Drawing.Color]::FromArgb($px.A, $targetColor.R, $targetColor.G, $targetColor.B)
            $outBmp.SetPixel($x, $y, $finalColor)
        }
    }
}

$tmplBmp.Dispose()
$palBmp.Dispose()

$outBmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
$outBmp.Dispose()

Write-Host "Successfully generated palette-mapped texture: $outputPath"
