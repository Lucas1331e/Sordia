$nbtFiles = Get-ChildItem -Path 'e:\Telum\src\main\resources\data\telum\structure\ancient_city' -Recurse -Filter '*.nbt'
foreach ($f in $nbtFiles) {
    Write-Host "NBT file: $($f.FullName) - Size: $($f.Length) bytes"
}
