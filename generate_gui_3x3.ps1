Add-Type -AssemblyName System.Drawing

$guiBmp = New-Object System.Drawing.Bitmap(256, 256)
$g = [System.Drawing.Graphics]::FromImage($guiBmp)

$g.Clear([System.Drawing.Color]::FromArgb(255, 50, 50, 55))

# Main container area (176x166)
$brush1 = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 85, 85, 90))
$g.FillRectangle($brush1, 0, 0, 176, 166)

# Inner background
$brush2 = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 75, 75, 80))
$g.FillRectangle($brush2, 4, 4, 168, 158)

# Title area
$brush3 = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 60, 60, 65))
$g.FillRectangle($brush3, 7, 4, 162, 14)

# Slot Brushes
$slotBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 40, 40, 45))
$borderBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 30, 30, 35))

# 3x3 Input Grid (30, 48, 66 x 17, 35, 53)
for ($row = 0; $row -lt 3; $row++) {
    for ($col = 0; $col -lt 3; $col++) {
        $x = 30 + $col * 18
        $y = 17 + $row * 18
        $g.FillRectangle($borderBrush, ($x - 1), ($y - 1), 18, 18)
        $g.FillRectangle($slotBrush, $x, $y, 16, 16)
    }
}

# Arrow to result slot
$arrowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 120, 120, 125))
$g.FillRectangle($arrowBrush, 96, 41, 24, 4)
for ($i = 0; $i -lt 6; $i++) {
    $g.FillRectangle($arrowBrush, (120 + $i), (43 - $i - 1), 1, (2 * $i + 2))
}

# Result slot at 140,35
$resultBorder = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 200, 160, 60))
$g.FillRectangle($resultBorder, 137, 32, 24, 24)
$g.FillRectangle($slotBrush, 139, 34, 20, 20)

# Inventory area
$invBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 65, 65, 70))
$g.FillRectangle($invBrush, 7, 80, 162, 80)

# Inventory slots
for ($row = 0; $row -lt 3; $row++) {
    for ($col = 0; $col -lt 9; $col++) {
        $x = 8 + $col * 18
        $y = 84 + $row * 18
        $g.FillRectangle($borderBrush, ($x - 1), ($y - 1), 18, 18)
        $g.FillRectangle($slotBrush, $x, $y, 16, 16)
    }
}

# Hotbar slots
for ($col = 0; $col -lt 9; $col++) {
    $x = 8 + $col * 18
    $y = 142
    $g.FillRectangle($borderBrush, ($x - 1), ($y - 1), 18, 18)
    $g.FillRectangle($slotBrush, $x, $y, 16, 16)
}

$g.Dispose()
$guiPath = "e:\Telum\src\main\resources\assets\telum\textures\gui\forge.png"
$guiBmp.Save($guiPath, [System.Drawing.Imaging.ImageFormat]::Png)
$guiBmp.Dispose()
Write-Host "Created 3x3 Grid Forge GUI texture at: $guiPath"
