@echo off
chcp 936 >nul
title �����ƻ�ģ�� - ������Ϸ
cd /d "%~dp0"
set "JAVA_HOME=C:\jdk-17"
if exist "%JAVA_HOME%\bin\java.exe" goto java_ready
for /d %%J in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do if exist "%%~fJ\bin\java.exe" set "JAVA_HOME=%%~fJ"
if exist "%JAVA_HOME%\bin\java.exe" goto java_ready
echo ERROR: JDK 17 was not found at C:\jdk-17 or under C:\Program Files\Eclipse Adoptium.
pause
exit /b 1
:java_ready
set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
echo ================================================================
echo   ���� Minecraft ģ�鿪������
echo   ��Ϸ���ڳ���ǰ�벻Ҫ������ڴ���
echo ================================================================
echo.
call gradlew.bat runClient --console=plain
echo.
echo ��Ϸ���˳�������б����������������ݷ����ҡ�
pause
