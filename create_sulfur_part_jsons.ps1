$assetsDir = "e:\Telum\src\main\resources\assets\telum"
$itemsDir  = "$assetsDir\items"
$modelsDir = "$assetsDir\models\item"

$sulfurParts = @(
    @{ id = "handle_sulfur"; tex = "sulfur_stick" },
    @{ id = "grip_sulfur";   tex = "sulfur_handle" },
    @{ id = "eye_sulfur";    tex = "sulfur_eye" },
    @{ id = "head_sulfur";   tex = "sulfur_axe_head" },
    @{ id = "blade_sulfur";  tex = "sulfur_blade" }
)

foreach ($p in $sulfurParts) {
    $id = $p.id
    $tex = $p.tex

    $itemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/${id}"
  }
}
"@
    Set-Content -Path "$itemsDir\${id}.json" -Value $itemJson -Encoding UTF8

    $modelJson = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/part/${tex}"
  }
}
"@
    Set-Content -Path "$modelsDir\${id}.json" -Value $modelJson -Encoding UTF8
    Write-Host "Created item and model for ${id}"
}
