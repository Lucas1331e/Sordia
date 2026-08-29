Add-Type -AssemblyName System.IO.Compression.FileSystem

$jarPath = 'C:\Users\pinhe\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged-deobf\26.2\minecraft-merged-deobf-26.2.jar'

# Run javap on RenderType and RenderTypes
javap -cp $jarPath net.minecraft.client.renderer.rendertype.RenderType
javap -cp $jarPath net.minecraft.client.renderer.rendertype.RenderTypes
