$recipePath = "e:\Telum\src\main\resources\data\telum\recipe"

Remove-Item -Path "$recipePath\*.json" -ErrorAction SilentlyContinue

$materials = @{
    "wood"      = @{ primary = "minecraft:oak_planks"; secondary = "minecraft:stick"; extra = "minecraft:oak_planks" }
    "stone"     = @{ primary = "minecraft:cobblestone"; secondary = "minecraft:stick"; extra = "minecraft:stone" }
    "copper"    = @{ primary = "minecraft:copper_ingot"; secondary = "minecraft:stick"; extra = "minecraft:copper_ingot" }
    "iron"      = @{ primary = "minecraft:iron_ingot"; secondary = "minecraft:stick"; extra = "minecraft:copper_ingot" }
    "gold"      = @{ primary = "minecraft:gold_ingot"; secondary = "minecraft:stick"; extra = "minecraft:copper_ingot" }
    "diamond"   = @{ primary = "minecraft:diamond"; secondary = "minecraft:stick"; extra = "minecraft:gold_ingot" }
    "netherite" = @{ primary = "minecraft:netherite_ingot"; secondary = "minecraft:stick"; extra = "telum:dragon_scale" }
    "blaze"     = @{ primary = "minecraft:blaze_rod"; secondary = "minecraft:stick"; extra = "minecraft:blaze_powder" }
}

function Write-Recipe {
    param([string]$Name, [string]$Content)
    $path = Join-Path $recipePath "$Name.json"
    Set-Content -Path $path -Value $Content -Encoding UTF8
    Write-Host "Created recipe: $Name"
}

# 1. Forge recipe
Write-Recipe "telum_forge" @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "III",
    " S ",
    " S "
  ],
  "key": {
    "I": { "item": "minecraft:iron_ingot" },
    "S": { "item": "minecraft:stone" }
  },
  "result": {
    "id": "telum:telum_forge",
    "count": 1
  }
}
"@

# 2. Part recipes (5 part types x 7 materials = 35 recipes)
foreach ($matName in $materials.Keys) {
    $t = $materials[$matName]
    $P = $t.primary
    $S = $t.secondary
    $E = $t.extra

    # HANDLE
    Write-Recipe "handle_${matName}" @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "P",
    "S",
    "S"
  ],
  "key": {
    "P": { "item": "$P" },
    "S": { "item": "$S" }
  },
  "result": { "id": "telum:handle_${matName}", "count": 2 }
}
"@

    # GRIP
    Write-Recipe "grip_${matName}" @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "PP",
    "SP",
    "SS"
  ],
  "key": {
    "P": { "item": "$P" },
    "S": { "item": "$S" }
  },
  "result": { "id": "telum:grip_${matName}", "count": 2 }
}
"@

    # EYE
    Write-Recipe "eye_${matName}" @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    " P ",
    "PEP",
    " P "
  ],
  "key": {
    "P": { "item": "$P" },
    "E": { "item": "$E" }
  },
  "result": { "id": "telum:eye_${matName}", "count": 2 }
}
"@

    # HEAD
    Write-Recipe "head_${matName}" @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "PPP",
    " E "
  ],
  "key": {
    "P": { "item": "$P" },
    "E": { "item": "$E" }
  },
  "result": { "id": "telum:head_${matName}", "count": 1 }
}
"@

    # BLADE
    Write-Recipe "blade_${matName}" @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    " P",
    "PE",
    "P "
  ],
  "key": {
    "P": { "item": "$P" },
    "E": { "item": "$E" }
  },
  "result": { "id": "telum:blade_${matName}", "count": 1 }
}
"@
}

Write-Host "`nAll 35 part recipes updated!"
