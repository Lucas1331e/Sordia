$itemsDir = "e:\Telum\src\main\resources\assets\telum\items"
$modelsDir = "e:\Telum\src\main\resources\assets\telum\models\item"
$texturesDir = "e:\Telum\src\main\resources\assets\telum\textures\item\part"

New-Item -ItemType Directory -Force -Path $itemsDir, $modelsDir, $texturesDir | Out-Null

$parts = @("handle", "grip", "eye", "head", "blade")
$materials = @("wood", "stone", "copper", "iron", "gold", "diamond", "netherite")

$textureMap = @{
    "handle_wood"      = "wood_stick"
    "handle_stone"     = "stone_stick"
    "handle_copper"    = "copper_stick"
    "handle_iron"      = "iron_stick"
    "handle_gold"      = "gold_stick"
    "handle_diamond"   = "diamond_stick"
    "handle_netherite" = "netherite_stick"

    "grip_wood"      = "wood_handle"
    "grip_stone"     = "stone_handle"
    "grip_copper"    = "copper_handle"
    "grip_iron"      = "iron_handle"
    "grip_gold"      = "gold_handle"
    "grip_diamond"   = "diamond_handle"
    "grip_netherite" = "netherite_handle"

    "head_wood"      = "head_t1"
    "head_stone"     = "head_stone"
    "head_copper"    = "head_copper"
    "head_iron"      = "head_iron"
    "head_gold"      = "head_gold"
    "head_diamond"   = "head_diamond"
    "head_netherite" = "head_netherite"

    "eye_wood"      = "wooden_eye"
    "eye_stone"     = "stone_eye"
    "eye_copper"    = "copper_eye"
    "eye_iron"      = "iron_eye"
    "eye_gold"      = "gold_eye"
    "eye_diamond"   = "diamond_eye"
    "eye_netherite" = "netherite_eye"

    "blade_wood"      = "wooden_blade"
    "blade_stone"     = "stone_blade"
    "blade_copper"    = "copper_blade"
    "blade_iron"      = "iron_blade"
    "blade_gold"      = "golden_blade"
    "blade_diamond"   = "diamond_blade"
    "blade_netherite" = "netherite_blade"
}

# 1. Generate items and models for parts (5 types x 7 materials = 35 part items)
foreach ($part in $parts) {
    foreach ($mat in $materials) {
        $itemId = "${part}_${mat}"
        $tex = if ($textureMap.ContainsKey($itemId)) { $textureMap[$itemId] } else { "${part}_${mat}" }

        # assets/telum/items/item_id.json
        $itemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/${itemId}"
  }
}
"@
        Set-Content -Path "$itemsDir\${itemId}.json" -Value $itemJson -Encoding UTF8

        # assets/telum/models/item/item_id.json
        $modelJson = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/part/${tex}"
  }
}
"@
        Set-Content -Path "$modelsDir\${itemId}.json" -Value $modelJson -Encoding UTF8

        Write-Host "Created item & model for: $itemId"
    }
}

# 2. Dragon Scale
$scaleItemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/dragon_scale"
  }
}
"@
Set-Content -Path "$itemsDir\dragon_scale.json" -Value $scaleItemJson -Encoding UTF8

$scaleModelJson = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/dragon_scale"
  }
}
"@
Set-Content -Path "$modelsDir\dragon_scale.json" -Value $scaleModelJson -Encoding UTF8

# 3. Piece of Sordia
$sordiaItemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/piece_of_sordia"
  }
}
"@
Set-Content -Path "$itemsDir\piece_of_sordia.json" -Value $sordiaItemJson -Encoding UTF8

$sordiaModelJson = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/piece_of_sordia"
  }
}
"@
Set-Content -Path "$modelsDir\piece_of_sordia.json" -Value $sordiaModelJson -Encoding UTF8

# 4. Forge Block Item
$forgeItemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/telum_forge"
  }
}
"@
Set-Content -Path "$itemsDir\telum_forge.json" -Value $forgeItemJson -Encoding UTF8

$forgeModelJson = @"
{
  "parent": "telum:block/telum_forge"
}
"@
Set-Content -Path "$modelsDir\telum_forge.json" -Value $forgeModelJson -Encoding UTF8

# 5. Suspicious End Stone Item
$stoneItemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/suspicious_end_stone"
  }
}
"@
Set-Content -Path "$itemsDir\suspicious_end_stone.json" -Value $stoneItemJson -Encoding UTF8

$stoneModelJson = @"
{
  "parent": "telum:block/suspicious_end_stone"
}
"@
Set-Content -Path "$modelsDir\suspicious_end_stone.json" -Value $stoneModelJson -Encoding UTF8

Write-Host "`nAll 35 part items and models generated successfully!"
