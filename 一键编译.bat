@echo off
chcp 936 >nul
title 月亮计划模组 - 编译
cd /d "%~dp0"
rem 关键：必须用工作区外的 JDK（工作区内的 JDK 会让进程继承沙箱、写不了 ~/.gradle）
set "JAVA_HOME=C:\jdk-17"
set "GRADLE_USER_HOME=C:\Users\23586\.gradle"
echo ================================================================
echo   月亮计划模组 - 编译
echo   JAVA_HOME=%JAVA_HOME%
echo ================================================================
echo.
call gradlew.bat compileJava --console=plain
if not errorlevel 1 goto ok
echo.
echo [重试] 改用工作区缓存
set "GRADLE_USER_HOME=%~dp0.gradle-home"
call gradlew.bat compileJava --console=plain
if not errorlevel 1 goto ok
echo.
echo ================================================================
echo   失败了，请把上面全部内容发给我
echo ================================================================
pause
exit /b 1
:ok
echo.
echo ================================================================
echo   编译成功 BUILD SUCCESSFUL
echo   下一步：双击桌面的「启动模组游戏」
echo ================================================================
pause
exit /b 0
