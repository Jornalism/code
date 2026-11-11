param([string]$CaptureDirectory = "$PSScriptRoot/evidence/final-captures")
Add-Type -AssemblyName System.Drawing
foreach ($variant in @('phone', 'compact-large-text')) {
    $files = @(Get-ChildItem $CaptureDirectory -Filter "*-$variant.png" | Sort-Object Name)
    for ($offset = 0; $offset -lt $files.Count; $offset += 6) {
        $batch = @($files | Select-Object -Skip $offset -First 6)
        $canvas = New-Object System.Drawing.Bitmap 900, 1380
        $graphics = [System.Drawing.Graphics]::FromImage($canvas)
        $graphics.Clear([System.Drawing.Color]::White)
        $font = New-Object System.Drawing.Font 'Arial', 12
        for ($index = 0; $index -lt $batch.Count; $index++) {
            $image = [System.Drawing.Image]::FromFile($batch[$index].FullName)
            $x = ($index % 3) * 300
            $y = [math]::Floor($index / 3) * 690
            $graphics.DrawString($batch[$index].BaseName, $font, [System.Drawing.Brushes]::Black, $x, $y)
            $height = [math]::Min(650, 294 * $image.Height / $image.Width)
            $width = $height * $image.Width / $image.Height
            $graphics.DrawImage($image, [single]$x, [single]($y + 30), [single]$width, [single]$height)
            $image.Dispose()
        }
        $page = [int]($offset / 6) + 1
        $canvas.Save("$CaptureDirectory/overview-$variant-$page.png", [System.Drawing.Imaging.ImageFormat]::Png)
        $font.Dispose()
        $graphics.Dispose()
        $canvas.Dispose()
    }
}
