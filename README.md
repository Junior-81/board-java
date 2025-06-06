# Board - Sistema de Gerenciamento de Quadros Kanban

Este projeto é uma implementação em Java de um sistema de gerenciamento de quadros (boards) inspirado no estilo Kanban, baseado no repositório da DIO (Digital Innovation One).

## Funcionalidades

### 🎯 Principais Features
- **Gerenciamento de Boards**: Criar boards com colunas personalizáveis
- **Sistema Kanban**: Colunas com tipos específicos (INITIAL, PENDING, FINAL, CANCEL)
- **Gerenciamento de Cards**: Criar, editar, mover, bloquear, desbloquear e cancelar cards
- **Sistema de Bloqueios**: Controle de bloqueios com histórico e motivos
- **Persistência em MySQL**: Banco de dados relacional com migrations automáticas
- **Interface de Linha de Comando**: Menus interativos para todas as operações

### 🗂️ Estrutura do Projeto

```
src/main/java/br/com/dio/
├── Main.java                      # Classe principal
├── dto/                          # Data Transfer Objects
│   ├── BoardColumnDTO.java
│   ├── BoardColumnInfoDTO.java
│   ├── BoardDetailsDTO.java
│   └── CardDetailsDTO.java
├── exception/                    # Exceções customizadas
│   ├── CardBlockedException.java
│   ├── CardFinishedException.java
│   └── EntityNotFoundException.java
├── persistence/                  # Camada de persistência
│   ├── config/
│   │   └── ConnectionConfig.java
│   ├── converter/
│   │   └── OffsetDateTimeConverter.java
│   ├── dao/                     # Data Access Objects
│   │   ├── BlockDAO.java
│   │   ├── BoardColumnDAO.java
│   │   ├── BoardDAO.java
│   │   └── CardDAO.java
│   ├── entity/                  # Entidades do domínio
│   │   ├── BlockEntity.java
│   │   ├── BoardColumnEntity.java
│   │   ├── BoardColumnKindEnum.java
│   │   ├── BoardEntity.java
│   │   └── CardEntity.java
│   └── migration/
│       └── MigrationStrategy.java
├── service/                     # Camada de negócios
│   ├── BoardColumnQueryService.java
│   ├── BoardQueryService.java
│   ├── BoardService.java
│   ├── CardQueryService.java
│   └── CardService.java
└── ui/                         # Interface do usuário
    ├── BoardMenu.java
    └── MainMenu.java
```

## 📋 Pré-requisitos

### Software Necessário
- **Java 17+**: Para executar o projeto
- **MySQL 8.0+**: Banco de dados
- **Gradle 8.0+**: Para build (opcional - wrapper incluído)

### Configuração do Banco de Dados

1. **Instalar MySQL**:
   ```bash
   # Windows (via Chocolatey)
   choco install mysql

   # ou baixar de: https://dev.mysql.com/downloads/mysql/
   ```

2. **Criar usuário e banco**:
   ```sql
   CREATE DATABASE board;
   CREATE USER 'board'@'localhost' IDENTIFIED BY 'board';
   GRANT ALL PRIVILEGES ON board.* TO 'board'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configurar conexão**:
   - URL: `jdbc:mysql://localhost/board`
   - Usuário: `board`
   - Senha: `board`

### Opção 3: Via IDE
1. Importe o projeto na sua IDE favorita (IntelliJ IDEA, Eclipse, VS Code)
2. Configure o classpath com as dependências
3. Execute a classe `Main.java`

## 📚 Dependências

O projeto utiliza as seguintes dependências principais:

```kotlin
dependencies {
    implementation("org.liquibase:liquibase-core:4.29.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}
```

## 🎮 Como Usar

### Menu Principal
Ao iniciar o sistema, você verá:

```
=== SISTEMA DE BOARD - MENU PRINCIPAL ===
1. Listar Boards
2. Criar Novo Board
3. Abrir Board
0. Sair
==========================================
```

### Criando um Board
1. Escolha a opção "2" no menu principal
2. Digite o nome do board
3. Escolha entre:
   - **Board Padrão**: 4 colunas (Para Fazer, Em Progresso, Finalizado, Cancelado)
   - **Board Customizado**: Defina suas próprias colunas

### Gerenciando Cards
Dentro de um board, você pode:
- **Criar cards**: Adicionar novos itens ao board
- **Mover cards**: Transferir entre colunas
- **Bloquear/Desbloquear**: Controlar acesso aos cards
- **Cancelar**: Mover para coluna de cancelamento
- **Editar**: Alterar título e descrição
- **Visualizar detalhes**: Ver histórico de bloqueios

## 🏗️ Arquitetura

### Padrões Utilizados
- **MVC**: Separação clara entre Model, View e Controller
- **DAO Pattern**: Acesso aos dados encapsulado
- **DTO Pattern**: Transferência de dados otimizada
- **Service Layer**: Lógica de negócio centralizada
- **Factory Pattern**: Criação de objetos padronizada

### Banco de Dados
O sistema utiliza **Liquibase** para versionamento do schema:

```yaml
# Estrutura das tabelas
board                 # Boards principais
├── board_column     # Colunas do board
│   └── card         # Cards nas colunas
└── block_card       # Sistema de bloqueios
    └── card_block   # Relacionamento card-bloqueio
```

### Tipos de Colunas
- **INITIAL**: Coluna inicial (onde cards são criados)
- **PENDING**: Colunas intermediárias (em progresso)
- **FINAL**: Coluna final (trabalho concluído)
- **CANCEL**: Coluna de cancelamento

## 🔧 Configuração Avançada

### Personalizar Conexão do Banco
Edite o arquivo `ConnectionConfig.java`:

```java
public static Connection getConnection() throws SQLException {
    var url = "jdbc:mysql://localhost/board";
    var user = "board";
    var password = "board";
    // ...
}
```

### Adicionar Novas Funcionalidades
1. **Novas entidades**: Adicione em `persistence/entity/`
2. **Novos DAOs**: Implemente em `persistence/dao/`
3. **Novos services**: Crie em `service/`
4. **Novas telas**: Adicione em `ui/`

## 🐛 Troubleshooting

### Erro de Conexão com Banco
```
SQLException: Access denied for user 'board'@'localhost'
```
**Solução**: Verifique se o usuário e banco foram criados corretamente.

### Erro de Dependências
```
ClassNotFoundException: com.mysql.cj.jdbc.Driver
```
**Solução**: Certifique-se de que o MySQL Connector está no classpath.

### Erro de Migração
```
LiquibaseException: Could not find changelog file
```
**Solução**: Verifique se o arquivo `db.changelog-master.yml` está em `src/main/resources/`.

## 📝 Logs

O sistema gera logs do Liquibase em `liquibase.log` para debug de migrações.


## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para detalhes.

## 👥 Créditos

Baseado no projeto original da [Digital Innovation One (DIO)](https://github.com/digitalinnovationone/board).



