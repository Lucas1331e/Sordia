Add-Type -AssemblyName System.IO.Compression.FileSystem

$jarPath = 'C:\Users\pinhe\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged-deobf\26.2\minecraft-merged-deobf-26.2.jar'
$zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
$entries = $zip.Entries | Where-Object { $_.FullName -like '*Render*' -or $_.FullName -like '*Block*' }
foreach ($e in $entries) {
    if ($e.FullName -like '*RenderType*' -or $e.FullName -like '*RenderLayer*' -or $e.FullName -like '*BlockRender*') {
        Write-Host "Found: $($e.FullName)"
    }
}
$zip.Dispose()
