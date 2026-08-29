$blockstatesDir = "e:\Telum\src\main\resources\assets\telum\blockstates"
$modelsBlockDir = "e:\Telum\src\main\resources\assets\telum\models\block"
$modelsItemDir = "e:\Telum\src\main\resources\assets\telum\models\item"
$itemsDir = "e:\Telum\src\main\resources\assets\telum\items"

New-Item -ItemType Directory -Force -Path $blockstatesDir, $modelsBlockDir, $modelsItemDir, $itemsDir | Out-Null

# Blockstate
$bsJson = @"
{
  "variants": {
    "dusted=0": { "model": "telum:block/suspicious_end_stone_0" },
    "dusted=1": { "model": "telum:block/suspicious_end_stone_1" },
    "dusted=2": { "model": "telum:block/suspicious_end_stone_2" },
    "dusted=3": { "model": "telum:block/suspicious_end_stone_3" }
  }
}
"@
Set-Content -Path "$blockstatesDir\suspicious_end_stone.json" -Value $bsJson -Encoding UTF8

# Block models 0..3
for ($i = 0; $i -le 3; $i++) {
    $bmJson = @"
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "telum:block/suspicious_end_stone_${i}"
  }
}
"@
    Set-Content -Path "$modelsBlockDir\suspicious_end_stone_${i}.json" -Value $bmJson -Encoding UTF8
}

# Item JSONs for 26.2
$itemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/suspicious_end_stone"
  }
}
"@
Set-Content -Path "$itemsDir\suspicious_end_stone.json" -Value $itemJson -Encoding UTF8

$itemModelJson = @"
{
  "parent": "telum:block/suspicious_end_stone_0"
}
"@
Set-Content -Path "$modelsItemDir\suspicious_end_stone.json" -Value $itemModelJson -Encoding UTF8

Write-Host "Suspicious End Stone assets created successfully!"
