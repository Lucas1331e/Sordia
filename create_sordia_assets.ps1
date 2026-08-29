$itemsDir = "e:\Telum\src\main\resources\assets\telum\items"
$modelsDir = "e:\Telum\src\main\resources\assets\telum\models\item"

# Piece of Sordia
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

Write-Host "Created items & model JSON for Piece of Sordia!"
