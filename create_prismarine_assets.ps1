Add-Type -AssemblyName System.Drawing

$assetsDir = "e:\Telum\src\main\resources\assets\telum"
$itemsDir  = "$assetsDir\items"
$modelsDir = "$assetsDir\models\item"
$partsTexDir = "$assetsDir\textures\item\part"

# 1. Item JSONs & Model JSONs for 5 Prismarine Parts
$parts = @(
    @{ id = "handle_prismarine"; tex = "prismarine_stick" },
    @{ id = "grip_prismarine";   tex = "prismarine_handle" },
    @{ id = "eye_prismarine";    tex = "prismarine_eye" },
    @{ id = "head_prismarine";   tex = "prismarine_head_trident" },
    @{ id = "blade_prismarine";  tex = "prismarine_blade" }
)

foreach ($p in $parts) {
    $id = $p.id
    $tex = $p.tex

    # item json
    $itemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/${id}"
  }
}
"@
    Set-Content -Path "$itemsDir\${id}.json" -Value $itemJson -Encoding UTF8

    # model json
    $modelJson = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/part/${tex}"
  }
}
"@
    Set-Content -Path "$modelsDir\${id}.json" -Value $modelJson -Encoding UTF8
    Write-Host "Created item & model for ${id}"
}

# 2. Ensure all textures exist in textures/item/part (create cyan tinted textures for any missing ones)
function TintTexture($srcFile, $dstFile) {
    $srcPath = Join-Path $partsTexDir $srcFile
    $dstPath = Join-Path $partsTexDir $dstFile
    if ((Test-Path $srcPath) -and !(Test-Path $dstPath)) {
        $srcBmp = [System.Drawing.Bitmap]::FromFile($srcPath)
        $dstBmp = New-Object System.Drawing.Bitmap($srcBmp.Width, $srcBmp.Height)
        for ($x = 0; $x -lt $srcBmp.Width; $x++) {
            for ($y = 0; $y -lt $srcBmp.Height; $y++) {
                $px = $srcBmp.GetPixel($x, $y)
                if ($px.A -gt 0) {
                    # Apply cyan/prismarine tint (R: 0.35, G: 0.85, B: 0.80)
                    $r = [Math]::Min(255, [int]($px.R * 0.35))
                    $g = [Math]::Min(255, [int]($px.G * 0.85))
                    $b = [Math]::Min(255, [int]($px.B * 0.80))
                    $dstBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($px.A, $r, $g, $b))
                } else {
                    $dstBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
                }
            }
        }
        $srcBmp.Dispose()
        $dstBmp.Save($dstPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $dstBmp.Dispose()
        Write-Host "Tinted texture created: $dstFile"
    }
}

TintTexture "wood_handle.png" "prismarine_handle.png"
TintTexture "wooden_blade.png" "prismarine_blade.png"
TintTexture "wood_stick_sword.png" "prismarine_stick_sword.png"
TintTexture "wooden_sword_handle.png" "prismarine_sword_handle.png"
TintTexture "wooden_pickaxe_head_left.png" "prismarine_pickaxe_head_left.png"
TintTexture "wooden_pickaxe_head_right.png" "prismarine_pickaxe_head_right.png"
TintTexture "wooden_axe_head.png" "prismarine_axe_head.png"
TintTexture "wooden_shovel_head.png" "prismarine_shovel_head.png"
TintTexture "wooden_hoe_head.png" "prismarine_hoe_head.png"

Write-Host "Prismarine assets created successfully!"
