@echo off
setlocal
if "%~1"=="" (
  echo Usage: Backup-CampusQ.cmd NEW_BACKUP_PATH
  exit /b 1
)

