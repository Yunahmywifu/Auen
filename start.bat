@echo off
echo ===================================
echo Music Library - Spring Boot App
echo ===================================
echo.

REM Попытка найти Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ОШИБКА] Java не найдена в PATH
    echo Пожалуйста, установите JDK 17 или выше
    echo.
    echo Скачать: https://adoptium.net/
    pause
    exit /b 1
)

echo [OK] Java найдена
java -version
echo.

echo Запуск приложения...
echo После запуска откройте браузер: http://localhost:8080
echo.
echo Для остановки нажмите Ctrl+C
echo.

REM Запуск через Maven Wrapper
call mvnw.cmd spring-boot:run

pause

