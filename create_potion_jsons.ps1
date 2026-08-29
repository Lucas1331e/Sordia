$assetsDir = "e:\Telum\src\main\resources\assets\telum"

# item json
$itemJson = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "telum:item/temporal_recall_potion"
  }
}
"@
Set-Content -Path "$assetsDir\items\temporal_recall_potion.json" -Value $itemJson -Encoding UTF8

# model json
$modelJson = @"
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "telum:item/temporal_recall_potion"
  }
}
"@
Set-Content -Path "$assetsDir\models\item\temporal_recall_potion.json" -Value $modelJson -Encoding UTF8

Write-Host "Created temporal_recall_potion item & model JSONs!"
