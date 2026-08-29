$modelsDir = "e:\Telum\src\main\resources\assets\telum\models\item"

$map = @{
    "head_wood.json"      = "wood_head_generic"
    "head_stone.json"     = "stone_head_generic"
    "head_copper.json"    = "copper_head_generic"
    "head_prismarine.json"= "prismarine_head_generic"
    "head_skulk.json"     = "skulk_head_generic"
    "head_iron.json"      = "iron_head_generic"
    "head_gold.json"      = "gold_head_generic"
    "head_diamond.json"   = "diamond_head_generic"
    "head_netherite.json" = "netherite_head_generic"
    "head_blaze.json"     = "blaze_head_generic"
    "head_tier_1.json"    = "wood_head_generic"
    "head_tier_2.json"    = "iron_head_generic"
    "head_tier_3.json"    = "diamond_head_generic"
    "head_tier_4.json"    = "netherite_head_generic"
}

foreach ($file in $map.Keys) {
    $path = Join-Path $modelsDir $file
    $tex = $map[$file]
    $json = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/part/${tex}"
  }
}
"@
    Set-Content -Path $path -Value $json -Encoding UTF8
    Write-Host "Updated $file -> $tex"
}
