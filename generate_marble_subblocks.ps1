$assetsDir = "e:\Telum\src\main\resources\assets\telum"
$dataDir   = "e:\Telum\src\main\resources\data\telum"

$bsDir          = "$assetsDir\blockstates"
$modelsBlockDir = "$assetsDir\models\block"
$modelsItemDir  = "$assetsDir\models\item"
$itemsDir       = "$assetsDir\items"
$lootDir        = "$dataDir\loot_table\blocks"
$recipeDir      = "$dataDir\recipe"

New-Item -ItemType Directory -Force -Path $bsDir, $modelsBlockDir, $modelsItemDir, $itemsDir, $lootDir, $recipeDir | Out-Null

$baseBlocks = @(
    @{ prefix = "marmol";       tex = "marmol_block";  base = "marmol_block" },
    @{ prefix = "marmol_brick"; tex = "marmol_bricks"; base = "marmol_bricks" }
)

foreach ($b in $baseBlocks) {
    $p = $b.prefix
    $tex = $b.tex
    $base = $b.base

    # -------------------------------------------------------------
    # 1. SLAB ($p_slab)
    # -------------------------------------------------------------
    $slabId = "${p}_slab"

    # Blockstate
    $slabBs = @"
{
  "variants": {
    "type=bottom": {
      "model": "telum:block/${slabId}"
    },
    "type=double": {
      "model": "telum:block/${base}"
    },
    "type=top": {
      "model": "telum:block/${slabId}_top"
    }
  }
}
"@
    Set-Content -Path "$bsDir\${slabId}.json" -Value $slabBs -Encoding UTF8

    # Block models
    $slabBottomModel = @"
{
  "parent": "minecraft:block/slab",
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
  "textures": {
    "bottom": "telum:block/${tex}",
    "side": "telum:block/${tex}",
    "top": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${slabId}_top.json" -Value $slabTopModel -Encoding UTF8

    # Item definition & model
    $itemDef = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/${slabId}"
  }
}
"@
    Set-Content -Path "$itemsDir\${slabId}.json" -Value $itemDef -Encoding UTF8

    $itemModel = @"
{
  "parent": "telum:block/${slabId}"
}
"@
    Set-Content -Path "$modelsItemDir\${slabId}.json" -Value $itemModel -Encoding UTF8

    # Loot table
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
                  "properties": {
                    "type": "double"
                  }
                }
              ]
            }
          ]
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
    Set-Content -Path "$lootDir\${slabId}.json" -Value $slabLoot -Encoding UTF8

    # Recipe
    $slabRecipe = @"
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "pattern": [
    "BBB"
  ],
  "key": {
    "B": "telum:${base}"
  },
  "result": {
    "id": "telum:${slabId}",
    "count": 6
  }
}
"@
    Set-Content -Path "$recipeDir\${slabId}.json" -Value $slabRecipe -Encoding UTF8


    # -------------------------------------------------------------
    # 2. STAIRS ($p_stairs)
    # -------------------------------------------------------------
    $stairsId = if ($p -eq "marmol") { "marmol_stairs" } else { "${p}_stairs" }

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

    # Block models
    $stairsStraight = @"
{
  "parent": "minecraft:block/stairs",
  "textures": {
    "bottom": "telum:block/${tex}",
    "side": "telum:block/${tex}",
    "top": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${stairsId}.json" -Value $stairsStraight -Encoding UTF8

    $stairsInner = @"
{
  "parent": "minecraft:block/inner_stairs",
  "textures": {
    "bottom": "telum:block/${tex}",
    "side": "telum:block/${tex}",
    "top": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${stairsId}_inner.json" -Value $stairsInner -Encoding UTF8

    $stairsOuter = @"
{
  "parent": "minecraft:block/outer_stairs",
  "textures": {
    "bottom": "telum:block/${tex}",
    "side": "telum:block/${tex}",
    "top": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${stairsId}_outer.json" -Value $stairsOuter -Encoding UTF8

    # Item definition & model
    $stairsItemDef = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:block/${stairsId}"
  }
}
"@
    Set-Content -Path "$itemsDir\${stairsId}.json" -Value $stairsItemDef -Encoding UTF8

    $stairsItemModel = @"
{
  "parent": "telum:block/${stairsId}"
}
"@
    Set-Content -Path "$modelsItemDir\${stairsId}.json" -Value $stairsItemModel -Encoding UTF8

    # Loot table
    $stairsLoot = @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "telum:${stairsId}"
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
    Set-Content -Path "$lootDir\${stairsId}.json" -Value $stairsLoot -Encoding UTF8

    # Recipe
    $stairsRecipe = @"
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "pattern": [
    "B  ",
    "BB ",
    "BBB"
  ],
  "key": {
    "B": "telum:${base}"
  },
  "result": {
    "id": "telum:${stairsId}",
    "count": 4
  }
}
"@
    Set-Content -Path "$recipeDir\${stairsId}.json" -Value $stairsRecipe -Encoding UTF8


    # -------------------------------------------------------------
    # 3. WALL ($p_wall)
    # -------------------------------------------------------------
    $wallId = "${p}_wall"

    $wallBs = @"
{
  "multipart": [
    {
      "when": { "up": "true" },
      "apply": { "model": "telum:block/${wallId}_post" }
    },
    {
      "when": { "north": "low" },
      "apply": { "model": "telum:block/${wallId}_side", "uvlock": true }
    },
    {
      "when": { "east": "low" },
      "apply": { "model": "telum:block/${wallId}_side", "y": 90, "uvlock": true }
    },
    {
      "when": { "south": "low" },
      "apply": { "model": "telum:block/${wallId}_side", "y": 180, "uvlock": true }
    },
    {
      "when": { "west": "low" },
      "apply": { "model": "telum:block/${wallId}_side", "y": 270, "uvlock": true }
    },
    {
      "when": { "north": "tall" },
      "apply": { "model": "telum:block/${wallId}_side_tall", "uvlock": true }
    },
    {
      "when": { "east": "tall" },
      "apply": { "model": "telum:block/${wallId}_side_tall", "y": 90, "uvlock": true }
    },
    {
      "when": { "south": "tall" },
      "apply": { "model": "telum:block/${wallId}_side_tall", "y": 180, "uvlock": true }
    },
    {
      "when": { "west": "tall" },
      "apply": { "model": "telum:block/${wallId}_side_tall", "y": 270, "uvlock": true }
    }
  ]
}
"@
    Set-Content -Path "$bsDir\${wallId}.json" -Value $wallBs -Encoding UTF8

    # Block models
    $wallPost = @"
{
  "parent": "minecraft:block/template_wall_post",
  "textures": {
    "wall": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${wallId}_post.json" -Value $wallPost -Encoding UTF8

    $wallSide = @"
{
  "parent": "minecraft:block/template_wall_side",
  "textures": {
    "wall": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${wallId}_side.json" -Value $wallSide -Encoding UTF8

    $wallSideTall = @"
{
  "parent": "minecraft:block/template_wall_side_tall",
  "textures": {
    "wall": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${wallId}_side_tall.json" -Value $wallSideTall -Encoding UTF8

    $wallInv = @"
{
  "parent": "minecraft:block/wall_inventory",
  "textures": {
    "wall": "telum:block/${tex}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\${wallId}_inventory.json" -Value $wallInv -Encoding UTF8

    # Item definition & model
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

    # Loot table
    $wallLoot = @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "telum:${wallId}"
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
    Set-Content -Path "$lootDir\${wallId}.json" -Value $wallLoot -Encoding UTF8

    # Recipe
    $wallRecipe = @"
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "pattern": [
    "BBB",
    "BBB"
  ],
  "key": {
    "B": "telum:${base}"
  },
  "result": {
    "id": "telum:${wallId}",
    "count": 6
  }
}
"@
    Set-Content -Path "$recipeDir\${wallId}.json" -Value $wallRecipe -Encoding UTF8
}

Write-Host "All subblock assets (slabs, stairs, walls) generated successfully!"
