$models = @(
    "deepslate_temporal_polished.json",
    "deepslate_temporal_tiles.json",
    "temporal_deepslate_brick.json",
    "temporal_barrel.json",
    "suspicious_temporal_sculk1.json",
    "suspicious_temporal_sculk2.json",
    "suspicious_temporal_sculk3.json",
    "suspicious_temporal_sculk4.json",
    "sculk_temporal_shrieker.json",
    "sculk_temporal_shrieker_can_summon.json"
)

$baseDir = "e:\Telum\src\main\resources\assets\telum\models\block"

foreach ($name in $models) {
    $path = Join-Path $baseDir $name
    if (Test-Path $path) {
        $json = Get-Content $path -Raw | ConvertFrom-Json
        $json | Add-Member -NotePropertyName "render_type" -NotePropertyValue "minecraft:translucent" -Force
        $json | ConvertTo-Json -Depth 5 | Set-Content $path
        Write-Host "Added render_type to: $name"
    }
}
