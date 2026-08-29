$shell = New-Object -ComObject Shell.Application
$rb = $shell.Namespace(0xA)
foreach ($item in $rb.Items()) {
    if ($item.Name -like "*head_generic*" -or $item.Name -like "*tempate*") {
        Write-Host "Found in Recycle Bin: $($item.Name)"
        # Restore item
        $item.InvokeVerb("restore")
        Write-Host "Restored item $($item.Name)!"
    }
}
