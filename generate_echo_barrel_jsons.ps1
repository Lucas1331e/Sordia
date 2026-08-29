$baseDir = "e:\Telum\src\main\resources\assets\telum"

# 1. blockstates/echo_barrel.json
@'
{
  "variants": {
    "": {
      "model": "telum:block/echo_barrel"
    }
  }
}
'@ | Out-File -Encoding utf8 "$baseDir\blockstates\echo_barrel.json"

# 2. blockstates/echo_barrel_projection.json
@'
{
  "variants": {
    "": {
      "model": "telum:block/echo_barrel_projection"
    }
  }
}
'@ | Out-File -Encoding utf8 "$baseDir\blockstates\echo_barrel_projection.json"

# 3. models/block/echo_barrel.json
@'
{
  "parent": "minecraft:block/cube_bottom_top",
  "textures": {
    "top": "telum:block/echo_barrel_top",
    "bottom": "telum:block/echo_barrel_bottom",
    "side": "telum:block/echo_barrel_side"
  }
}
'@ | Out-File -Encoding utf8 "$baseDir\models\block\echo_barrel.json"

# 4. models/block/echo_barrel_projection.json
@'
{
  "parent": "minecraft:block/cube_bottom_top",
  "render_type": "minecraft:translucent",
  "textures": {
    "top": "telum:block/echo_barrel_projection_top",
    "bottom": "telum:block/echo_barrel_projection_top",
    "side": "telum:block/echo_barrel_projection_side"
  }
}
'@ | Out-File -Encoding utf8 "$baseDir\models\block\echo_barrel_projection.json"

# 5. models/item/echo_barrel.json
@'
{
  "parent": "telum:block/echo_barrel"
}
'@ | Out-File -Encoding utf8 "$baseDir\models\item\echo_barrel.json"

# 6. models/item/echo_barrel_projection.json
@'
{
  "parent": "telum:block/echo_barrel_projection"
}
'@ | Out-File -Encoding utf8 "$baseDir\models\item\echo_barrel_projection.json"

# 7. items/echo_barrel.json
@'
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/echo_barrel"
  }
}
'@ | Out-File -Encoding utf8 "$baseDir\items\echo_barrel.json"

# 8. items/echo_barrel_projection.json
@'
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/echo_barrel_projection"
  }
}
'@ | Out-File -Encoding utf8 "$baseDir\items\echo_barrel_projection.json"

Write-Host "Echo Barrel JSON assets generated!"
