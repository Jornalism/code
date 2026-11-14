param([Parameter(Mandatory=$true)][string]$Destination)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Push-Location $projectRoot
try {
  # SQLite online backup includes committed WAL records. Does not copy a live WAL file unsafely.
  node backend/local/backup.mjs $Destination
  if($LASTEXITCODE -ne 0){throw 'Backup failed.'}
} finally { Pop-Location }
