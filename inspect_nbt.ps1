Add-Type -AssemblyName System.IO.Compression.FileSystem

$jarPath = 'C:\Users\pinhe\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged-deobf\26.2\minecraft-merged-deobf-26.2.jar'

# Let's inspect NBT structure using javap or a small tool
Write-Host "NBT file inspector"
