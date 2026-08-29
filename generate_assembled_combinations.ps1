$partsTexturesDir = "e:\Telum\src\main\resources\assets\telum\textures\item\part"
$partModelsDir   = "e:\Telum\src\main\resources\assets\telum\models\item\part"
$itemsDir        = "e:\Telum\src\main\resources\assets\telum\items"
$assembledDir    = "e:\Telum\src\main\resources\assets\telum\models\item\assembled"

# Delete obsolete assembled directory if it exists
if (Test-Path $assembledDir) {
    Remove-Item -Path $assembledDir -Recurse -Force -ErrorAction SilentlyContinue
}

# Ensure part models directory exists
New-Item -ItemType Directory -Force -Path $partModelsDir | Out-Null

# Get all PNG texture files in textures/item/part
$textureFiles = Get-ChildItem -Path $partsTexturesDir -Filter "*.png"

$partNames = [System.Collections.Generic.List[string]]::new()

foreach ($file in $textureFiles) {
    $baseName = $file.BaseName
    $partNames.Add($baseName)

    $partModelJson = @"
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "telum:item/part/$baseName"
  }
}
"@
    Set-Content -Path "$partModelsDir\$baseName.json" -Value $partModelJson -Encoding UTF8
}

# Build select cases for each layer
$casesList = [System.Collections.Generic.List[string]]::new()
foreach ($name in $partNames) {
    $casesList.Add(@"
      {
        "when": "$name",
        "model": {
          "type": "minecraft:model",
          "model": "telum:item/part/$name"
        }
      }
"@)
}

$casesString = $casesList -join ",`n"

# Generate composite assembled_tool.json for 4 layers
$assembledToolJson = @"
{
  "model": {
    "type": "minecraft:composite",
    "models": [
      {
        "type": "minecraft:select",
        "property": "minecraft:custom_model_data",
        "index": 0,
        "fallback": {
          "type": "minecraft:model",
          "model": "telum:item/empty"
        },
        "cases": [
${casesString}
        ]
      },
      {
        "type": "minecraft:select",
        "property": "minecraft:custom_model_data",
        "index": 1,
        "fallback": {
          "type": "minecraft:model",
          "model": "telum:item/empty"
        },
        "cases": [
${casesString}
        ]
      },
      {
        "type": "minecraft:select",
        "property": "minecraft:custom_model_data",
        "index": 2,
        "fallback": {
          "type": "minecraft:model",
          "model": "telum:item/empty"
        },
        "cases": [
${casesString}
        ]
      },
      {
        "type": "minecraft:select",
        "property": "minecraft:custom_model_data",
        "index": 3,
        "fallback": {
          "type": "minecraft:model",
          "model": "telum:item/empty"
        },
        "cases": [
${casesString}
        ]
      }
    ]
  }
}
"@

Set-Content -Path "$itemsDir\assembled_tool.json" -Value $assembledToolJson -Encoding UTF8

Write-Host "Generated $($partNames.Count) part models in models/item/part/ and updated assembled_tool.json with 4-layer composite select model!"
