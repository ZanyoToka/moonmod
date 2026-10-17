@echo off
chcp 936 >nul
title 月亮计划模组 - 启动游戏
cd /d "%~dp0"
set "JAVA_HOME=C:\jdk-17"
set "GRADLE_USER_HOME=C:\Users\23586\.gradle"
echo ================================================================
echo   启动 Minecraft 模组开发环境
echo   游戏窗口出现前请不要关这个黑窗口
echo ================================================================
echo.
call gradlew.bat runClient --console=plain
echo.
echo 游戏已退出。如果有报错，请把上面的内容发给我。
pause
