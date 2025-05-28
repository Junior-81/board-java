package br.com.dio.ui;

import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void execute() {
        while (true) {
            try {
                showMainMenu();
                var option = getIntInput("Escolha uma opção: ");

                switch (option) {
                    case 1 -> listBoards();
                    case 2 -> createBoard();
                    case 3 -> openBoard();
                    case 0 -> {
                        System.out.println("Saindo do sistema...");
                        return;
                    }
                    default -> System.out.println("Opção inválida! Tente novamente.");
                }
            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n=== SISTEMA DE BOARD - MENU PRINCIPAL ===");
        System.out.println("1. Listar Boards");
        System.out.println("2. Criar Novo Board");
        System.out.println("3. Abrir Board");
        System.out.println("0. Sair");
        System.out.println("==========================================");
    }

    private void listBoards() throws SQLException {
        try (var connection = getConnection()) {
            var boardQueryService = new BoardQueryService(connection);
            var boards = boardQueryService.findAllBoards();

            System.out.println("\n=== LISTA DE BOARDS ===");
            if (boards.isEmpty()) {
                System.out.println("Nenhum board encontrado.");
            } else {
                boards.forEach(board -> 
                    System.out.printf("%d - %s%n", board.getId(), board.getName())
                );
            }
        }
    }

    private void createBoard() throws SQLException {
        System.out.println("\n=== CRIAR NOVO BOARD ===");
        System.out.print("Nome do Board: ");
        var boardName = scanner.nextLine().trim();

        if (boardName.isEmpty()) {
            System.out.println("Nome do board não pode estar vazio!");
            return;
        }

        System.out.println("\nEscolha o tipo de board:");
        System.out.println("1. Board Padrão (Para Fazer, Em Progresso, Finalizado, Cancelado)");
        System.out.println("2. Board Customizado");
        
        var type = getIntInput("Tipo: ");

        try (var connection = getConnection()) {
            var boardService = new BoardService(connection);
            
            switch (type) {
                case 1 -> {
                    var board = boardService.createBoard(boardName);
                    connection.commit();
                    System.out.println("Board '" + board.getName() + "' criado com sucesso! ID: " + board.getId());
                }
                case 2 -> createCustomBoard(boardService, boardName, connection);
                default -> System.out.println("Tipo inválido!");
            }
        }
    }

    private void createCustomBoard(BoardService boardService, String boardName, java.sql.Connection connection) throws SQLException {
        System.out.print("Quantas colunas deseja criar? ");
        var columnCount = getIntInput("");

        if (columnCount < 2) {
            System.out.println("Um board deve ter pelo menos 2 colunas!");
            return;
        }

        var columnNames = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            System.out.printf("Nome da coluna %d: ", i + 1);
            columnNames[i] = scanner.nextLine().trim();
            
            if (columnNames[i].isEmpty()) {
                System.out.println("Nome da coluna não pode estar vazio!");
                return;
            }
        }

        var board = boardService.createBoardWithCustomColumns(boardName, columnNames);
        connection.commit();
        System.out.println("Board customizado '" + board.getName() + "' criado com sucesso! ID: " + board.getId());
    }

    private void openBoard() throws SQLException {
        System.out.print("ID do Board: ");
        var boardId = getLongInput("");

        try (var connection = getConnection()) {
            var boardQueryService = new BoardQueryService(connection);
            var board = boardQueryService.findBoardById(boardId);
            
            System.out.println("Abrindo board: " + board.getName());
            new BoardMenu(connection, boardId).execute();
        }
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, digite um número válido.");
            }
        }
    }

    private long getLongInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, digite um número válido.");
            }
        }
    }

}
