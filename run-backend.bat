@echo off
chcp 65001 >nul
title Backend Al-Zahrawi (GESTCABMED)
cd /d "%~dp0"

echo ========================================================
echo   Lancement du Backend Al-Zahrawi (Ports 8081 et 8082)
echo ========================================================
echo.

set CP=target\classes;%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.13.1\gson-2.13.1.jar;%USERPROFILE%\.m2\repository\org\mariadb\jdbc\mariadb-java-client\3.5.6\mariadb-java-client-3.5.6.jar;%USERPROFILE%\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar

java -cp "%CP%" com.enicarthage.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERREUR] Le serveur s'est arrêté avec le code %ERRORLEVEL%.
    pause
)
