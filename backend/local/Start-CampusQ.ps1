param([string]$HostAddress='127.0.0.1',[int]$Port=8787,[switch]$AllowLanHttp)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Push-Location $projectRoot
try {
  $env:CAMPUSQ_HOST=$HostAddress
  $env:CAMPUSQ_PORT=$Port.ToString()
  if($AllowLanHttp){$env:CAMPUSQ_ALLOW_LAN_HTTP='true'}else{$env:CAMPUSQ_ALLOW_LAN_HTTP='false'}
  node backend/local/server.mjs
  if($LASTEXITCODE -ne 0){throw 'CampusQ server failed.'}
} finally { Pop-Location }
