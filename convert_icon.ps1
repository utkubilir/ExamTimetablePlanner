
Add-Type -AssemblyName System.Drawing

$source = "icon.png"
$dest = "icon.ico"

if (-not (Test-Path $source)) {
    Write-Error "Source file $source not found."
    exit 1
}

$bmp = [System.Drawing.Bitmap]::FromFile($source)
$icon = [System.Drawing.Icon]::FromHandle($bmp.GetHicon())

$fileStream = New-Object System.IO.FileStream($dest, [System.IO.FileMode]::Create)
$icon.Save($fileStream)

$fileStream.Close()
$icon.Dispose()
$bmp.Dispose()

Write-Host "Converted $source to $dest"
