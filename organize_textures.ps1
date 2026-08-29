$itemTexDir  = "e:\Telum\src\main\resources\assets\telum\textures\item"
$partTexDir  = "$itemTexDir\part"
$tmplDir     = "$itemTexDir\templates"
$palDir      = "$itemTexDir\palettes"
$modelsItem  = "e:\Telum\src\main\resources\assets\telum\models\item"

New-Item -ItemType Directory -Force -Path $tmplDir | Out-Null
New-Item -ItemType Directory -Force -Path $palDir  | Out-Null

# 1. Move Templates
$templates = @(
    "blade_template.png",
    "eye_template.png",
    "eye_trident_template.png",
    "handle_template.png",
    "handle_sword_template.png",
    "head_axe_template.png",
    "head_hoe_template.png",
    "head_pickaxe_left_template.png",
    "head_pickaxe_right_template.png",
    "head_shovel_template.png",
    "stick_template.png"
)

foreach ($t in $templates) {
    $src = Join-Path $partTexDir $t
    if (Test-Path $src) {
        Move-Item -Path $src -Destination (Join-Path $tmplDir $t) -Force
        Write-Host "Moved template to templates/: $t"
    }
}

# 2. Move Palettes
$palettes = @(
    "wood_pallete.png",
    "stone_pallete.png",
    "copper_pallete.png",
    "prismarine_pallete.png",
    "iron_pallete.png",
    "gold_pallete.png",
    "diamon_pallete.png",
    "diamond_pallete.png",
    "netherite_pallete.png"
)

foreach ($p in $palettes) {
    $src = Join-Path $partTexDir $p
    if (Test-Path $src) {
        Move-Item -Path $src -Destination (Join-Path $palDir $p) -Force
        Write-Host "Moved palette to palettes/: $p"
    }
}

# 3. Update Head Item Models to point to generated part textures
$headModelMap = @{
    "head_wood.json"      = "wooden_axe_head"
    "head_stone.json"     = "stone_axe_head"
    "head_copper.json"    = "copper_axe_head"
    "head_iron.json"      = "iron_axe_head"
    "head_gold.json"      = "golden_axe_head"
    "head_diamond.json"   = "diamond_axe_head"
    "head_netherite.json" = "netherite_axe_head"
    "head_tier_1.json"    = "wooden_axe_head"
    "head_tier_2.json"    = "iron_axe_head"
    "head_tier_3.json"    = "diamond_axe_head"
    "head_tier_4.json"    = "netherite_axe_head"
}

foreach ($modelFile in $headModelMap.Keys) {
    $jsonPath = Join-Path $modelsItem $modelFile
    $texName = $headModelMap[$modelFile]
    if (Test-Path $jsonPath) {
        $jsonContent = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/part/${texName}"
  }
}
"@
        Set-Content -Path $jsonPath -Value $jsonContent -Encoding UTF8
        Write-Host "Updated item model: $modelFile -> $texName"
    }
}

# 4. Remove unused legacy textures
$unusedFiles = @(
    "head_copper.png",
    "head_diamond.png",
    "head_gold.png",
    "head_iron.png",
    "head_netherite.png",
    "head_stone.png",
    "head_t1.png",
    "head_t2.png",
    "head_t3.png",
    "head_t4.png",
    "head_generic_tempate.png"
)

foreach ($u in $unusedFiles) {
    $src = Join-Path $partTexDir $u
    if (Test-Path $src) {
        Remove-Item -Path $src -Force
        Write-Host "Removed unused legacy texture: $u"
    }
}

Write-Host "Texture reorganization completed successfully!"
