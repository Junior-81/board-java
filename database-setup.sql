-- Script de configuração do banco de dados MySQL para o projeto Board
-- Execute os comandos abaixo no MySQL como administrador

-- 1. Conectar ao MySQL
-- mysql -u root -p

-- 2. Criar o banco de dados
CREATE DATABASE IF NOT EXISTS board;

-- 3. Criar usuário específico para a aplicação
CREATE USER IF NOT EXISTS 'board'@'localhost' IDENTIFIED BY 'board';

-- 4. Conceder privilégios
GRANT ALL PRIVILEGES ON board.* TO 'board'@'localhost';

-- 5. Aplicar as mudanças
FLUSH PRIVILEGES;

-- 6. Verificar se o usuário foi criado corretamente
SELECT User, Host FROM mysql.user WHERE User = 'board';

-- 7. Verificar se o banco foi criado
SHOW DATABASES LIKE 'board';

-- Instruções de uso:
-- 1. Copie e cole estes comandos no MySQL Workbench ou no terminal do MySQL
-- 2. Execute um comando por vez ou todo o script de uma vez
-- 3. A aplicação criará automaticamente as tabelas na primeira execução

-- Configurações da aplicação:
-- URL: jdbc:mysql://localhost/board
-- Usuário: board  
-- Senha: board
-- Porta: 3306 (padrão)

-- Para resetar (CUIDADO - apaga todos os dados):
-- DROP DATABASE board;
-- DROP USER 'board'@'localhost';

SHOW TABLES;
