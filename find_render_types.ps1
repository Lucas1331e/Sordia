Add-Type -AssemblyName System.IO.Compression.FileSystem

$jars = Get-ChildItem -Path 'C:\Users\pinhe\.gradle\caches\fabric-loom' -Recurse -Filter '*.jar'
foreach ($j in $jars) {
    Write-Host "Checking: $($j.Name)"
    $zip = [System.IO.Compression.ZipFile]::OpenRead($j.FullName)
    $matches = $zip.Entries | Where-Object { $_.FullName -like '*ItemBlockRenderTypes*' -or $_.FullName -like '*RenderType*' -or $_.FullName -like '*ChunkRenderType*' }
    foreach ($m in $matches) {
        Write-Host "  Found: $($m.FullName)"
    }
    $zip.Dispose()
}
