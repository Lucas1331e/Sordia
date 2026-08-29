Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = 'C:\Users\pinhe\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged-deobf\26.2\minecraft-merged-deobf-26.2.jar'
$z = [System.IO.Compression.ZipFile]::OpenRead($jar)
$entry = $z.GetEntry('net/minecraft/client/renderer/entity/SulfurCubeRenderer.class')
if ($entry) {
    Write-Host "Found SulfurCubeRenderer.class"
}
$z.Dispose()
