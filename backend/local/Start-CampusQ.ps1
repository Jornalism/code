param([string]$HostAddress='127.0.0.1',[int]$Port=8787,[switch]$AllowLanHttp)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Push-Location $projectRoot

