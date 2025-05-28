@echo off
echo ========================================
echo    Board - Sistema de Gerenciamento
echo         de Quadros Kanban
echo ========================================
echo.

REM Verificar se o Java está instalado
java -version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Java nao encontrado!
    echo Por favor, instale o Java 17+ e tente novamente.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

echo [INFO] Java encontrado. Verificando MySQL...

REM Verificar se o MySQL está rodando
netstat -an | find "3306" >nul
if errorlevel 1 (
    echo.
    echo AVISO: MySQL pode nao estar rodando na porta 3306
    echo Certifique-se de que o MySQL esteja iniciado.
    echo.
)

echo [INFO] Iniciando o sistema...
echo.

REM Compilar e executar usando Gradle
if exist "gradlew.bat" (
    echo [INFO] Usando Gradle Wrapper...
    gradlew.bat run
) else (
    echo [INFO] Gradle Wrapper nao encontrado. Tentando compilacao manual...
    
    REM Criar diretório de build
    if not exist "build\classes" mkdir build\classes
    
    REM Baixar dependências (simplificado - em produção usar Maven/Gradle)
    echo [WARN] Dependencias nao configuradas para compilacao manual.
    echo [INFO] Recomendamos usar uma IDE ou configurar o Gradle.
    pause
)

pause
