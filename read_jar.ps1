Add-Type -AssemblyName System.IO.Compression.FileSystem

$jarPath = 'C:\Users\pinhe\.gradle\caches\modules-2\files-2.1\net.fabricmc.fabric-api\fabric-rendering-v1\16.2.10+0290ad933e\657de941c907f9b32363371698671e977cd2cec0\fabric-rendering-v1-16.2.10+0290ad933e-sources.jar'
$zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
$entry = $zip.GetEntry('net/fabricmc/fabric/mixin/client/rendering/ItemBlockRenderTypesMixin.java')
$stream = $entry.Open()
$reader = New-Object System.IO.StreamReader($stream)
$text = $reader.ReadToEnd()
$reader.Close()
$stream.Close()
$zip.Dispose()

Write-Host $text
