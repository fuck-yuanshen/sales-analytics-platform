@echo off
setlocal
chcp 65001 >nul

set "ROOT=%~dp0"

echo 正在启动后端...
start "销售分析-后端" cmd /k "cd /d \"backend\" && mvn spring-boot:run"

echo 正在启动前端...
start "销售分析-前端" cmd /k "cd /d \"frontend\" && npm run serve"

echo 已启动前后端，请保持新打开的两个窗口不要关闭。
exit /b 0
