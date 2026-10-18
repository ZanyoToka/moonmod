@echo off
chcp 936 >nul
title �����ƻ�ģ�� - ����
cd /d "%~dp0"
rem �ؼ��������ù�������� JDK���������ڵ� JDK ���ý��̼̳�ɳ�䡢д���� ~/.gradle��
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
echo   �����ƻ�ģ�� - ����
echo   JAVA_HOME=%JAVA_HOME%
echo ================================================================
echo.
call gradlew.bat compileJava --console=plain
if not errorlevel 1 goto ok
echo.
echo [����] ���ù���������
set "GRADLE_USER_HOME=%~dp0.gradle-home"
call gradlew.bat compileJava --console=plain
if not errorlevel 1 goto ok
echo.
echo ================================================================
echo   ʧ���ˣ��������ȫ�����ݷ�����
echo ================================================================
pause
exit /b 1
:ok
echo.
echo ================================================================
echo   ����ɹ� BUILD SUCCESSFUL
echo   ��һ����˫������ġ�����ģ����Ϸ��
echo ================================================================
pause
exit /b 0
