$assetsDir = "e:\Telum\src\main\resources\assets\telum"
$dataDir   = "e:\Telum\src\main\resources\data\telum"

$bsDir          = "$assetsDir\blockstates"
$modelsBlockDir = "$assetsDir\models\block"
$modelsItemDir  = "$assetsDir\models\item"
$itemsDir       = "$assetsDir\items"
$lootDir        = "$dataDir\loot_table\blocks"
$recipeDir      = "$dataDir\recipe"

New-Item -ItemType Directory -Force -Path $bsDir, $modelsBlockDir, $modelsItemDir, $itemsDir, $lootDir, $recipeDir | Out-Null

# -------------------------------------------------------------
# 1. BASE FULL BLOCKS (Block, Bricks, Gilded Block)
# -------------------------------------------------------------
$fullBlocks = @(
    @{ id = "yellow_marmol_block";        tex = "yellow_marmol_block";        baseCraft = "telum:marmol_block" },
    @{ id = "yellow_marmol_bricks";       tex = "yellow_marmol_bricks";       baseCraft = "telum:marmol_bricks" },
    @{ id = "yellow_marmol_gilded_block"; tex = "yellow_marmol_gilded_block"; baseCraft = "telum:marmol_gilded_block" }
)

foreach ($fb in $fullBlocks) {
    $id = $fb.id
    $tex = $fb.tex
    $baseCraft = $fb.baseCraft

    # Blockstate
    $bs = @"
{
  "variants": {
    "": {
      "model": "telum:block/${id}"
    }
  }
}
"@
    Set-Content -Path "$bsDir\${id}.json" -Value $bs -Encoding UTF8

    # Model
    $model = @"
{
  "parent": "minecraft:block/cube_all",
  "render_type": "minecraft:translucent",
  "textures": {
    "all": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${id}.json" -Value $model -Encoding UTF8

    # Item definition & model
    $itemDef = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/${id}"
  }
}
"@
    Set-Content -Path "$itemsDir\${id}.json" -Value $itemDef -Encoding UTF8

    $itemModel = @"
{
  "parent": "telum:block/${id}"
}
"@
    Set-Content -Path "$modelsItemDir\${id}.json" -Value $itemModel -Encoding UTF8

    # Loot table
    $loot = @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "telum:${id}"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}
"@
    Set-Content -Path "$lootDir\${id}.json" -Value $loot -Encoding UTF8

    # Recipe (Yellow Dye + Base Marble Block)
    $recipe = @"
{
  "type": "minecraft:crafting_shapeless",
  "category": "building",
  "ingredients": [
    { "item": "minecraft:yellow_dye" },
    { "item": "${baseCraft}" }
  ],
  "result": {
    "id": "telum:${id}",
    "count": 1
  }
}
"@
    Set-Content -Path "$recipeDir\${id}.json" -Value $recipe -Encoding UTF8
}

# -------------------------------------------------------------
# 2. YELLOW MARMOL PILLAR
# -------------------------------------------------------------
$pillarId = "yellow_marmol_pillar"
$pillarBs = @"
{
  "variants": {
    "axis=x": { "model": "telum:block/${pillarId}", "x": 90, "y": 90 },
    "axis=y": { "model": "telum:block/${pillarId}" },
    "axis=z": { "model": "telum:block/${pillarId}", "x": 90 }
  }
}
"@
Set-Content -Path "$bsDir\${pillarId}.json" -Value $pillarBs -Encoding UTF8

$pillarModel = @"
{
  "parent": "minecraft:block/cube_column",
  "render_type": "minecraft:translucent",
  "textures": {
    "end": "telum:block/yellow_marmol_pillar_top",
    "side": "telum:block/yellow_marmol_pillar"
  }
}
"@
Set-Content -Path "$modelsBlockDir\${pillarId}.json" -Value $pillarModel -Encoding UTF8

$pillarItemDef = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/${pillarId}"
  }
}
"@
Set-Content -Path "$itemsDir\${pillarId}.json" -Value $pillarItemDef -Encoding UTF8

$pillarItemModel = @"
{
  "parent": "telum:block/${pillarId}"
}
"@
Set-Content -Path "$modelsItemDir\${pillarId}.json" -Value $pillarItemModel -Encoding UTF8

$pillarLoot = @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "telum:${pillarId}"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}
"@
Set-Content -Path "$lootDir\${pillarId}.json" -Value $pillarLoot -Encoding UTF8

$pillarRecipe = @"
{
  "type": "minecraft:crafting_shapeless",
  "category": "building",
  "ingredients": [
    { "item": "minecraft:yellow_dye" },
    { "item": "telum:marmol_pillar" }
  ],
  "result": {
    "id": "telum:${pillarId}",
    "count": 1
  }
}
"@
Set-Content -Path "$recipeDir\${pillarId}.json" -Value $pillarRecipe -Encoding UTF8


# -------------------------------------------------------------
# 3. SUBBLOCKS (SLABS, STAIRS, WALLS) FOR BASE & BRICKS
# -------------------------------------------------------------
$subGroups = @(
    @{ prefix = "yellow_marmol";       tex = "yellow_marmol_block";  base = "yellow_marmol_block" },
    @{ prefix = "yellow_marmol_brick"; tex = "yellow_marmol_bricks"; base = "yellow_marmol_bricks" }
)

foreach ($sg in $subGroups) {
    $p = $sg.prefix
    $tex = $sg.tex
    $base = $sg.base

    # SLAB
    $slabId = "${p}_slab"
    $slabBs = @"
{
  "variants": {
    "type=bottom": { "model": "telum:block/${slabId}" },
    "type=double": { "model": "telum:block/${base}" },
    "type=top":    { "model": "telum:block/${slabId}_top" }
  }
}
"@
    Set-Content -Path "$bsDir\${slabId}.json" -Value $slabBs -Encoding UTF8

    $slabBottomModel = @"
{
  "parent": "minecraft:block/slab",
  "render_type": "minecraft:translucent",
  "textures": {
    "bottom": "telum:block/${tex}",
    "side": "telum:block/${tex}",
    "top": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${slabId}.json" -Value $slabBottomModel -Encoding UTF8

    $slabTopModel = @"
{
  "parent": "minecraft:block/slab_top",
  "render_type": "minecraft:translucent",
  "textures": {
    "bottom": "telum:block/${tex}",
    "side": "telum:block/${tex}",
    "top": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${slabId}_top.json" -Value $slabTopModel -Encoding UTF8

    $subItemDef = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/${slabId}"
  }
}
"@
    Set-Content -Path "$itemsDir\${slabId}.json" -Value $subItemDef -Encoding UTF8

    $subItemModel = @"
{
  "parent": "telum:block/${slabId}"
}
"@
    Set-Content -Path "$modelsItemDir\${slabId}.json" -Value $subItemModel -Encoding UTF8

    $slabLoot = @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "telum:${slabId}",
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": 2,
              "conditions": [
                {
                  "condition": "minecraft:block_state_property",
                  "block": "telum:${slabId}",
                  "properties": { "type": "double" }
                }
              ]
            }
          ]
        }
      ],
      "conditions": [ { "condition": "minecraft:survives_explosion" } ]
    }
  ]
}
"@
    Set-Content -Path "$lootDir\${slabId}.json" -Value $slabLoot -Encoding UTF8

    $slabRecipe = @"
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "pattern": [ "BBB" ],
  "key": { "B": "telum:${base}" },
  "result": { "id": "telum:${slabId}", "count": 6 }
}
"@
    Set-Content -Path "$recipeDir\${slabId}.json" -Value $slabRecipe -Encoding UTF8


    # STAIRS
    $stairsId = "${p}_stairs"
    $stairsBs = @"
{
  "variants": {
    "facing=east,half=bottom,shape=straight": { "model": "telum:block/${stairsId}" },
    "facing=west,half=bottom,shape=straight": { "model": "telum:block/${stairsId}", "y": 180, "uvlock": true },
    "facing=south,half=bottom,shape=straight": { "model": "telum:block/${stairsId}", "y": 90, "uvlock": true },
    "facing=north,half=bottom,shape=straight": { "model": "telum:block/${stairsId}", "y": 270, "uvlock": true },
    "facing=east,half=bottom,shape=outer_right": { "model": "telum:block/${stairsId}_outer" },
    "facing=west,half=bottom,shape=outer_right": { "model": "telum:block/${stairsId}_outer", "y": 180, "uvlock": true },
    "facing=south,half=bottom,shape=outer_right": { "model": "telum:block/${stairsId}_outer", "y": 90, "uvlock": true },
    "facing=north,half=bottom,shape=outer_right": { "model": "telum:block/${stairsId}_outer", "y": 270, "uvlock": true },
    "facing=east,half=bottom,shape=outer_left": { "model": "telum:block/${stairsId}_outer", "y": 270, "uvlock": true },
    "facing=west,half=bottom,shape=outer_left": { "model": "telum:block/${stairsId}_outer", "y": 90, "uvlock": true },
    "facing=south,half=bottom,shape=outer_left": { "model": "telum:block/${stairsId}_outer" },
    "facing=north,half=bottom,shape=outer_left": { "model": "telum:block/${stairsId}_outer", "y": 180, "uvlock": true },
    "facing=east,half=bottom,shape=inner_right": { "model": "telum:block/${stairsId}_inner" },
    "facing=west,half=bottom,shape=inner_right": { "model": "telum:block/${stairsId}_inner", "y": 180, "uvlock": true },
    "facing=south,half=bottom,shape=inner_right": { "model": "telum:block/${stairsId}_inner", "y": 90, "uvlock": true },
    "facing=north,half=bottom,shape=inner_right": { "model": "telum:block/${stairsId}_inner", "y": 270, "uvlock": true },
    "facing=east,half=bottom,shape=inner_left": { "model": "telum:block/${stairsId}_inner", "y": 270, "uvlock": true },
    "facing=west,half=bottom,shape=inner_left": { "model": "telum:block/${stairsId}_inner", "y": 90, "uvlock": true },
    "facing=south,half=bottom,shape=inner_left": { "model": "telum:block/${stairsId}_inner" },
    "facing=north,half=bottom,shape=inner_left": { "model": "telum:block/${stairsId}_inner", "y": 180, "uvlock": true },
    "facing=east,half=top,shape=straight": { "model": "telum:block/${stairsId}", "x": 180, "uvlock": true },
    "facing=west,half=top,shape=straight": { "model": "telum:block/${stairsId}", "x": 180, "y": 180, "uvlock": true },
    "facing=south,half=top,shape=straight": { "model": "telum:block/${stairsId}", "x": 180, "y": 90, "uvlock": true },
    "facing=north,half=top,shape=straight": { "model": "telum:block/${stairsId}", "x": 180, "y": 270, "uvlock": true },
    "facing=east,half=top,shape=outer_right": { "model": "telum:block/${stairsId}_outer", "x": 180, "y": 90, "uvlock": true },
    "facing=west,half=top,shape=outer_right": { "model": "telum:block/${stairsId}_outer", "x": 180, "y": 270, "uvlock": true },
    "facing=south,half=top,shape=outer_right": { "model": "telum:block/${stairsId}_outer", "x": 180, "y": 180, "uvlock": true },
    "facing=north,half=top,shape=outer_right": { "model": "telum:block/${stairsId}_outer", "x": 180, "uvlock": true },
    "facing=east,half=top,shape=outer_left": { "model": "telum:block/${stairsId}_outer", "x": 180, "uvlock": true },
    "facing=west,half=top,shape=outer_left": { "model": "telum:block/${stairsId}_outer", "x": 180, "y": 180, "uvlock": true },
    "facing=south,half=top,shape=outer_left": { "model": "telum:block/${stairsId}_outer", "x": 180, "y": 90, "uvlock": true },
    "facing=north,half=top,shape=outer_left": { "model": "telum:block/${stairsId}_outer", "x": 180, "y": 270, "uvlock": true },
    "facing=east,half=top,shape=inner_right": { "model": "telum:block/${stairsId}_inner", "x": 180, "y": 90, "uvlock": true },
    "facing=west,half=top,shape=inner_right": { "model": "telum:block/${stairsId}_inner", "x": 180, "y": 270, "uvlock": true },
    "facing=south,half=top,shape=inner_right": { "model": "telum:block/${stairsId}_inner", "x": 180, "y": 180, "uvlock": true },
    "facing=north,half=top,shape=inner_right": { "model": "telum:block/${stairsId}_inner", "x": 180, "uvlock": true },
    "facing=east,half=top,shape=inner_left": { "model": "telum:block/${stairsId}_inner", "x": 180, "uvlock": true },
    "facing=west,half=top,shape=inner_left": { "model": "telum:block/${stairsId}_inner", "x": 180, "y": 180, "uvlock": true },
    "facing=south,half=top,shape=inner_left": { "model": "telum:block/${stairsId}_inner", "x": 180, "y": 90, "uvlock": true },
    "facing=north,half=top,shape=inner_left": { "model": "telum:block/${stairsId}_inner", "x": 180, "y": 270, "uvlock": true }
  }
}
"@
    Set-Content -Path "$bsDir\${stairsId}.json" -Value $stairsBs -Encoding UTF8

    $stairsStraight = @"
{
  "parent": "minecraft:block/stairs",
  "render_type": "minecraft:translucent",
  "textures": { "bottom": "telum:block/${tex}", "side": "telum:block/${tex}", "top": "telum:block/${tex}" }
}
"@
    Set-Content -Path "$modelsBlockDir\${stairsId}.json" -Value $stairsStraight -Encoding UTF8

    $stairsInner = @"
{
  "parent": "minecraft:block/inner_stairs",
  "render_type": "minecraft:translucent",
  "textures": { "bottom": "telum:block/${tex}", "side": "telum:block/${tex}", "top": "telum:block/${tex}" }
}
"@
    Set-Content -Path "$modelsBlockDir\${stairsId}_inner.json" -Value $stairsInner -Encoding UTF8

    $stairsOuter = @"
{
  "parent": "minecraft:block/outer_stairs",
  "render_type": "minecraft:translucent",
  "textures": { "bottom": "telum:block/${tex}", "side": "telum:block/${tex}", "top": "telum:block/${tex}" }
}
"@
    Set-Content -Path "$modelsBlockDir\${stairsId}_outer.json" -Value $stairsOuter -Encoding UTF8

    $stairItemDef = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/${stairsId}"
  }
}
"@
    Set-Content -Path "$itemsDir\${stairsId}.json" -Value $stairItemDef -Encoding UTF8

    $stairItemModel = @"
{
  "parent": "telum:block/${stairsId}"
}
"@
    Set-Content -Path "$modelsItemDir\${stairsId}.json" -Value $stairItemModel -Encoding UTF8

    $stairsLoot = @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [ { "type": "minecraft:item", "name": "telum:${stairsId}" } ],
      "conditions": [ { "condition": "minecraft:survives_explosion" } ]
    }
  ]
}
"@
    Set-Content -Path "$lootDir\${stairsId}.json" -Value $stairsLoot -Encoding UTF8

    $stairsRecipe = @"
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "pattern": [ "B  ", "BB ", "BBB" ],
  "key": { "B": "telum:${base}" },
  "result": { "id": "telum:${stairsId}", "count": 4 }
}
"@
    Set-Content -Path "$recipeDir\${stairsId}.json" -Value $stairsRecipe -Encoding UTF8


    # WALL
    $wallId = "${p}_wall"
    $wallBs = @"
{
  "multipart": [
    { "when": { "up": "true" }, "apply": { "model": "telum:block/${wallId}_post" } },
    { "when": { "north": "low" }, "apply": { "model": "telum:block/${wallId}_side", "uvlock": true } },
    { "when": { "east": "low" }, "apply": { "model": "telum:block/${wallId}_side", "y": 90, "uvlock": true } },
    { "when": { "south": "low" }, "apply": { "model": "telum:block/${wallId}_side", "y": 180, "uvlock": true } },
    { "when": { "west": "low" }, "apply": { "model": "telum:block/${wallId}_side", "y": 270, "uvlock": true } },
    { "when": { "north": "tall" }, "apply": { "model": "telum:block/${wallId}_side_tall", "uvlock": true } },
    { "when": { "east": "tall" }, "apply": { "model": "telum:block/${wallId}_side_tall", "y": 90, "uvlock": true } },
    { "when": { "south": "tall" }, "apply": { "model": "telum:block/${wallId}_side_tall", "y": 180, "uvlock": true } },
    { "when": { "west": "tall" }, "apply": { "model": "telum:block/${wallId}_side_tall", "y": 270, "uvlock": true } }
  ]
}
"@
    Set-Content -Path "$bsDir\${wallId}.json" -Value $wallBs -Encoding UTF8

    $wallPost = @"
{
  "parent": "minecraft:block/template_wall_post",
  "render_type": "minecraft:translucent",
  "textures": { "wall": "telum:block/${tex}" }
}
"@
    Set-Content -Path "$modelsBlockDir\${wallId}_post.json" -Value $wallPost -Encoding UTF8

    $wallSide = @"
{
  "parent": "minecraft:block/template_wall_side",
  "render_type": "minecraft:translucent",
  "textures": { "wall": "telum:block/${tex}" }
}
"@
    Set-Content -Path "$modelsBlockDir\${wallId}_side.json" -Value $wallSide -Encoding UTF8

    $wallSideTall = @"
{
  "parent": "minecraft:block/template_wall_side_tall",
  "render_type": "minecraft:translucent",
  "textures": { "wall": "telum:block/${tex}" }
}
"@
    Set-Content -Path "$modelsBlockDir\${wallId}_side_tall.json" -Value $wallSideTall -Encoding UTF8

    $wallInv = @"
{
  "parent": "minecraft:block/wall_inventory",
  "render_type": "minecraft:translucent",
  "textures": { "wall": "telum:block/${tex}" }
}
"@
    Set-Content -Path "$modelsBlockDir\${wallId}_inventory.json" -Value $wallInv -Encoding UTF8

    $wallItemDef = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/${wallId}_inventory"
  }
}
"@
    Set-Content -Path "$itemsDir\${wallId}.json" -Value $wallItemDef -Encoding UTF8

    $wallItemModel = @"
{
  "parent": "telum:block/${wallId}_inventory"
}
"@
    Set-Content -Path "$modelsItemDir\${wallId}.json" -Value $wallItemModel -Encoding UTF8

    $wallLoot = @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [ { "type": "minecraft:item", "name": "telum:${wallId}" } ],
      "conditions": [ { "condition": "minecraft:survives_explosion" } ]
    }
  ]
}
"@
    Set-Content -Path "$lootDir\${wallId}.json" -Value $wallLoot -Encoding UTF8

    $wallRecipe = @"
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "pattern": [ "BBB", "BBB" ],
  "key": { "B": "telum:${base}" },
  "result": { "id": "telum:${wallId}", "count": 6 }
}
"@
    Set-Content -Path "$recipeDir\${wallId}.json" -Value $wallRecipe -Encoding UTF8
}

Write-Host "All yellow translucent marble asset JSONs generated successfully!"
