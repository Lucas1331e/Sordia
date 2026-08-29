$source = "e:\Telum\run\saves\New World (1)\generated\sordia\structure"
$targetSordia = "e:\Telum\src\main\resources\data\sordia\structure"
$targetTelum = "e:\Telum\src\main\resources\data\telum\structure"

if (Test-Path $source) {
    Get-ChildItem -Recurse $source -Filter '*.nbt' | ForEach-Object {
        $relativePath = $_.FullName.Substring($source.Length)
        
        $destSordia = Join-Path $targetSordia $relativePath
        $destSordiaDir = Split-Path $destSordia
        if (-not (Test-Path $destSordiaDir)) { New-Item -ItemType Directory -Path $destSordiaDir -Force | Out-Null }
        Copy-Item $_.FullName $destSordia -Force
        Write-Host "Updated Sordia structure: $relativePath"

        $destTelum = Join-Path $targetTelum $relativePath
        $destTelumDir = Split-Path $destTelum
        if (-not (Test-Path $destTelumDir)) { New-Item -ItemType Directory -Path $destTelumDir -Force | Out-Null }
        Copy-Item $_.FullName $destTelum -Force
        Write-Host "Updated Telum structure: $relativePath"
    }
} else {
    Write-Host "Source directory not found: $source"
}
