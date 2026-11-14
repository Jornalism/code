@echo off
setlocal
pushd "%~dp0..\.." || exit /b 1
node backend/local/server.mjs
set "campusq_exit=%errorlevel%"
popd
exit /b %campusq_exit%
