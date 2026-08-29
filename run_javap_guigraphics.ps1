$jars = Get-ChildItem -Path "C:\Users\pinhe\.gradle\caches\fabric-loom" -Filter "*.jar" -Recurse | Select-Object -ExpandProperty FullName
$cp = $jars -join ";"
javap -cp $cp net.minecraft.client.gui.GuiGraphics
