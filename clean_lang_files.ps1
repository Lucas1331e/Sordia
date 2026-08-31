$c2 = [char]0x00C2
$e2 = [char]0x00E2
$section = [char]0x00A7

function Clean-File($filePath) {
    Write-Host "Cleaning: $filePath"
    $bytes = [System.IO.File]::ReadAllBytes($filePath)
    $text = [System.Text.Encoding]::UTF8.GetString($bytes)

    # Remove C2/E2 artifacts before section signs
    $text = $text.Replace("$c2$section", "$section")
    $text = $text.Replace("$e2$section", "$section")

    # Clean double encoded bullet prefixes
    $text = $text -replace 'â€¢\s*', ''
    $text = $text -replace 'â˜…', ''
    $text = $text -replace 'ðŸ“‘', ''

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($filePath, $text, $utf8NoBom)
    Write-Host "Cleaned $filePath"
}

Clean-File "e:\Telum\src\main\resources\assets\telum\lang\es_es.json"
Clean-File "e:\Telum\src\main\resources\assets\telum\lang\en_us.json"
