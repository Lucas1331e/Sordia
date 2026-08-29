$assetsDir = "e:\Telum\src\main\resources\assets\telum"
$itemsDir  = "$assetsDir\items"
$modelsDir = "$assetsDir\models\item"

$skulkParts = @(
    @{ id = "handle_skulk"; tex = "skulk_stick" },
    @{ id = "grip_skulk";   tex = "skulk_handle" },
    @{ id = "eye_skulk";    tex = "skulk_eye" },
    @{ id = "head_skulk";   tex = "skulk_axe_head" },
    @{ id = "blade_skulk";  tex = "skulk_blade" }
)

foreach ($p in $skulkParts) {
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
