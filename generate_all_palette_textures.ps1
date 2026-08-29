Add-Type -AssemblyName System.Drawing

$itemTexDir  = "e:\Telum\src\main\resources\assets\telum\textures\item"
$partsTexDir = "$itemTexDir\part"
$tmplDir     = "$itemTexDir\templates"
$palDir      = "$itemTexDir\palettes"

# Palette mappings (support diamon_pallete / diamond_pallete)
$materials = @(
    @{ name = "wood";      pal = "wood_pallete.png";      stick = "wood_stick.png";      stick_sword = "wood_stick_sword.png";      stick_trident = "wood_stick_trident.png";      handle = "wood_handle.png";      sword_handle = "wooden_sword_handle.png";      eye = "wooden_eye.png";      eye_trident = "wooden_eye_trident.png";      blade = "wooden_blade.png";      axe = "wooden_axe_head.png";      shovel = "wooden_shovel_head.png";      hoe = "wooden_hoe_head.png";      pick_l = "wooden_pickaxe_head_left.png";      pick_r = "wooden_pickaxe_head_right.png";      head_gen = "wood_head_generic.png" },
    @{ name = "stone";     pal = "stone_pallete.png";     stick = "stone_stick.png";     stick_sword = "stone_stick_sword.png";     stick_trident = "stone_stick_trident.png";     handle = "stone_handle.png";     sword_handle = "stone_sword_handle.png";     eye = "stone_eye.png";     eye_trident = "stone_eye_trident.png";     blade = "stone_blade.png";     axe = "stone_axe_head.png";     shovel = "stone_shovel_head.png";     hoe = "stone_hoe_head.png";     pick_l = "stone_pickaxe_head_left.png";     pick_r = "stone_pickaxe_head_right.png";     head_gen = "stone_head_generic.png" },
    @{ name = "copper";    pal = "copper_pallete.png";    stick = "copper_stick.png";    stick_sword = "copper_stick_sword.png";    stick_trident = "copper_stick_trident.png";    handle = "copper_handle.png";    sword_handle = "copper_sword_handle.png";    eye = "copper_eye.png";    eye_trident = "copper_eye_trident.png";    blade = "copper_blade.png";    axe = "copper_axe_head.png";    shovel = "copper_shovel_head.png";    hoe = "copper_hoe_head.png";    pick_l = "copper_pickaxe_head_left.png";    pick_r = "copper_pickaxe_head_right.png";    head_gen = "copper_head_generic.png" },
    @{ name = "prismarine";pal = "prismarine_pallete.png";stick = "prismarine_stick.png";stick_sword = "prismarine_stick_sword.png";stick_trident = "prismarine_stick_trident.png";handle = "prismarine_handle.png";sword_handle = "prismarine_sword_handle.png";eye = "prismarine_eye.png";eye_trident = "prismarine_eye_trident.png";blade = "prismarine_blade.png";axe = "prismarine_axe_head.png";shovel = "prismarine_shovel_head.png";hoe = "prismarine_hoe_head.png";pick_l = "prismarine_pickaxe_head_left.png";pick_r = "prismarine_pickaxe_head_right.png";head_gen = "prismarine_head_generic.png" },
    @{ name = "skulk";     pal = "skulk_pallete.png";     stick = "skulk_stick.png";     stick_sword = "skulk_stick_sword.png";     stick_trident = "skulk_stick_trident.png";     handle = "skulk_handle.png";     sword_handle = "skulk_sword_handle.png";     eye = "skulk_eye.png";     eye_trident = "skulk_eye_trident.png";     blade = "skulk_blade.png";     axe = "skulk_axe_head.png";     shovel = "skulk_shovel_head.png";     hoe = "skulk_hoe_head.png";     pick_l = "skulk_pickaxe_head_left.png";     pick_r = "skulk_pickaxe_head_right.png";     head_gen = "skulk_head_generic.png" },
    @{ name = "wind";      pal = "wind_pallete.png";      stick = "wind_stick.png";      stick_sword = "wind_stick_sword.png";      stick_trident = "wind_stick_trident.png";      handle = "wind_handle.png";      sword_handle = "wind_sword_handle.png";      eye = "wind_eye.png";      eye_trident = "wind_eye_trident.png";      blade = "wind_blade.png";      axe = "wind_axe_head.png";      shovel = "wind_shovel_head.png";      hoe = "wind_hoe_head.png";      pick_l = "wind_pickaxe_head_left.png";      pick_r = "wind_pickaxe_head_right.png";      head_gen = "wind_head_generic.png" },
    @{ name = "iron";      pal = "iron_pallete.png";      stick = "iron_stick.png";      stick_sword = "iron_stick_sword.png";      stick_trident = "iron_stick_trident.png";      handle = "iron_handle.png";      sword_handle = "iron_sword_handle.png";      eye = "iron_eye.png";      eye_trident = "iron_eye_trident.png";      blade = "iron_blade.png";      axe = "iron_axe_head.png";      shovel = "iron_shovel_head.png";      hoe = "iron_hoe_head.png";      pick_l = "iron_pickaxe_head_left.png";      pick_r = "iron_pickaxe_head_right.png";      head_gen = "iron_head_generic.png" },
    @{ name = "gold";      pal = "gold_pallete.png";      stick = "gold_stick.png";      stick_sword = "gold_stick_sword.png";      stick_trident = "gold_stick_trident.png";      handle = "gold_handle.png";      sword_handle = "golden_sword_handle.png";      eye = "gold_eye.png";      eye_trident = "gold_eye_trident.png";      blade = "golden_blade.png";      axe = "golden_axe_head.png";      shovel = "golden_shovel_head.png";      hoe = "golden_hoe_head.png";      pick_l = "golden_pickaxe_head_left.png";      pick_r = "golden_pickaxe_head_right.png";      head_gen = "gold_head_generic.png" },
    @{ name = "diamond";   pal = "diamon_pallete.png";    stick = "diamond_stick.png";   stick_sword = "diamond_stick_sword.png";   stick_trident = "diamond_stick_trident.png";   handle = "diamond_handle.png";   sword_handle = "diamond_sword_handle.png";   eye = "diamond_eye.png";   eye_trident = "diamond_eye_trident.png";   blade = "diamond_blade.png";   axe = "diamond_axe_head.png";   shovel = "diamond_shovel_head.png";   hoe = "diamond_hoe_head.png";   pick_l = "diamond_pickaxe_head_left.png";   pick_r = "diamond_pickaxe_head_right.png";   head_gen = "diamond_head_generic.png" },
    @{ name = "netherite"; pal = "netherite_pallete.png"; stick = "netherite_stick.png"; stick_sword = "netherite_stick_sword.png"; stick_trident = "netherite_stick_trident.png"; handle = "netherite_handle.png"; sword_handle = "netherite_sword_handle.png"; eye = "netherite_eye.png"; eye_trident = "netherite_eye_trident.png"; blade = "netherite_blade.png"; axe = "netherite_axe_head.png"; shovel = "netherite_shovel_head.png"; hoe = "netherite_hoe_head.png"; pick_l = "netherite_pickaxe_head_left.png"; pick_r = "netherite_pickaxe_head_right.png"; head_gen = "netherite_head_generic.png" },
    @{ name = "blaze";     pal = "blaze_pallete.png";     stick = "blaze_stick.png";     stick_sword = "blaze_stick_sword.png";     stick_trident = "blaze_stick_trident.png";     handle = "blaze_handle.png";     sword_handle = "blaze_sword_handle.png";     eye = "blaze_eye.png";      eye_trident = "blaze_eye_trident.png";      blade = "blaze_blade.png";      axe = "blaze_axe_head.png";      shovel = "blaze_shovel_head.png";      hoe = "blaze_hoe_head.png";      pick_l = "blaze_pickaxe_head_left.png";      pick_r = "blaze_pickaxe_head_right.png";      head_gen = "blaze_head_generic.png" },
    @{ name = "sulfur";    pal = "sulfure_pallete.png";   stick = "sulfur_stick.png";    stick_sword = "sulfur_stick_sword.png";    stick_trident = "sulfur_stick_trident.png";    handle = "sulfur_handle.png";   sword_handle = "sulfur_sword_handle.png";   eye = "sulfur_eye.png";     eye_trident = "sulfur_eye_trident.png";     blade = "sulfur_blade.png";     axe = "sulfur_axe_head.png";     shovel = "sulfur_shovel_head.png";     hoe = "sulfur_hoe_head.png";     pick_l = "sulfur_pickaxe_head_left.png";    pick_r = "sulfur_pickaxe_head_right.png";    head_gen = "sulfur_head_generic.png" }
)

function MapPalette($tmplFile, $palFile, $outFile) {
    $tmplPath = Join-Path $tmplDir $tmplFile
    $palPath  = Join-Path $palDir $palFile
    $outPath  = Join-Path $partsTexDir $outFile

    if (Test-Path $outPath) {
        if ($outFile -eq "blaze_trident_eye.png" -or $outFile -eq "blaze_trident_head.png") {
            Write-Host "Skipping existing exclusive texture: $outFile"
            return
        }
    }

    if (!(Test-Path $tmplPath)) {
        Write-Warning "Template missing: $tmplFile"
        return
    }
    if (!(Test-Path $palPath)) {
        if ($palFile -eq "diamond_pallete.png" -and (Test-Path (Join-Path $palDir "diamon_pallete.png"))) {
            $palPath = Join-Path $palDir "diamon_pallete.png"
        } else {
            Write-Warning "Palette missing: $palFile"
            return
        }
    }

    $tmplBmp = [System.Drawing.Bitmap]::FromFile($tmplPath)
    $palBmp  = [System.Drawing.Bitmap]::FromFile($palPath)

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
    $grayValues.Reverse()

    $outBmp = New-Object System.Drawing.Bitmap($tmplBmp.Width, $tmplBmp.Height)

    for ($x = 0; $x -lt $tmplBmp.Width; $x++) {
        for ($y = 0; $y -lt $tmplBmp.Height; $y++) {
            $px = $tmplBmp.GetPixel($x, $y)
            if ($px.A -eq 0) {
                $outBmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
            } else {
                $rankIndex = $grayValues.IndexOf($px.R)
                if ($rankIndex -lt 0) {
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

                $palY = [Math]::Min($rankIndex, $palBmp.Height - 1)
                $targetColor = $palBmp.GetPixel(0, $palY)
                $finalColor = [System.Drawing.Color]::FromArgb($px.A, $targetColor.R, $targetColor.G, $targetColor.B)
                $outBmp.SetPixel($x, $y, $finalColor)
            }
        }
    }

    $tmplBmp.Dispose()
    $palBmp.Dispose()

    $outBmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $outBmp.Dispose()
    Write-Host "Generated: $outFile"
}

$count = 0
foreach ($m in $materials) {
    $pal = $m.pal
    Write-Host "--- Generating textures for material: $($m.name) using $pal ---"
    
    MapPalette "stick_template.png"               $pal $m.stick;         $count++
    MapPalette "stick_sword_template.png"         $pal $m.stick_sword;   $count++
    MapPalette "stick_trident_template.png"       $pal $m.stick_trident; $count++
    MapPalette "handle_template.png"              $pal $m.handle;        $count++
    MapPalette "handle_sword_template.png"        $pal $m.sword_handle;  $count++
    MapPalette "eye_template.png"                 $pal $m.eye;           $count++
    MapPalette "eye_trident_template.png"         $pal $m.eye_trident;   $count++
    MapPalette "blade_template.png"               $pal $m.blade;         $count++
    MapPalette "head_axe_template.png"            $pal $m.axe;           $count++
    MapPalette "head_shovel_template.png"         $pal $m.shovel;        $count++
    MapPalette "head_hoe_template.png"            $pal $m.hoe;           $count++
    MapPalette "head_pickaxe_left_template.png"   $pal $m.pick_l;        $count++
    MapPalette "head_pickaxe_right_template.png"  $pal $m.pick_r;        $count++
    MapPalette "head_generic_template.png"        $pal $m.head_gen;      $count++
}

Write-Host "Finished processing all $count part textures across all materials!"
