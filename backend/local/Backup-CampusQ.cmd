@echo off
setlocal
if "%~1"=="" (
  echo Usage: Backup-CampusQ.cmd NEW_BACKUP_PATH
  exit /b 1
)
pushd "%~dp0..\.." || exit /b 1
node backend/local/backup.mjs "%~1"
set "campusq_exit=%errorlevel%"
popd
exit /b %campusq_exit%
