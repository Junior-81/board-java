package br.com.dio.ui;

import br.com.dio.service.*;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

@AllArgsConstructor
public class BoardMenu {

    private final Connection connection;
    private final Long boardId;
    private final Scanner scanner = new Scanner(System.in);

    public void execute() throws SQLException {
        while (true) {
            try {
                showBoardInfo();
                showBoardMenu();
                var option = getIntInput("Escolha uma opção: ");

                switch (option) {
                    case 1 -> showBoardDetails();
                    case 2 -> createCard();
                    case 3 -> listCards();
                    case 4 -> moveCard();
                    case 5 -> blockCard();
                    case 6 -> unblockCard();
                    case 7 -> cancelCard();
                    case 8 -> showCardDetails();
                    case 9 -> updateCard();
                    case 10 -> deleteCard();
                    case 0 -> {
                        return; // Voltar ao menu principal
                    }
                    default -> System.out.println("Opção inválida! Tente novamente.");
                }
            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
                connection.rollback();
            }
        }
    }

    private void showBoardInfo() throws SQLException {
        var boardQueryService = new BoardQueryService(connection);
        var board = boardQueryService.findBoardById(boardId);
        System.out.println("\n=== BOARD: " + board.getName().toUpperCase() + " ===");
    }

    private void showBoardMenu() {
        System.out.println("1. Visualizar Board Completo");
        System.out.println("2. Criar Card");
        System.out.println("3. Listar Cards");
        System.out.println("4. Mover Card");
        System.out.println("5. Bloquear Card");
        System.out.println("6. Desbloquear Card");
        System.out.println("7. Cancelar Card");
        System.out.println("8. Detalhes do Card");
        System.out.println("9. Editar Card");
        System.out.println("10. Excluir Card");
        System.out.println("0. Voltar ao Menu Principal");
        System.out.println("==============================");
    }

    private void showBoardDetails() throws SQLException {
        var boardQueryService = new BoardQueryService(connection);
        var boardDetails = boardQueryService.getBoardDetails(boardId);

        System.out.println("\n=== DETALHES DO BOARD ===");
        System.out.println("Board: " + boardDetails.name());
        System.out.println("\nColunas:");
        
        boardDetails.columns().forEach(column -> 
            System.out.printf("- %s (%s): %d cards%n", 
                column.name(), 
                column.kind(), 
                column.cardsAmount())
        );
    }

    private void createCard() throws SQLException {
        System.out.println("\n=== CRIAR NOVO CARD ===");
        System.out.print("Título do Card: ");
        var title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Título não pode estar vazio!");
            return;
        }

        System.out.print("Descrição do Card: ");
        var description = scanner.nextLine().trim();

        var cardService = new CardService(connection);
        var card = cardService.createCard(title, description, boardId);
        connection.commit();
        
        System.out.println("Card '" + card.getTitle() + "' criado com sucesso! ID: " + card.getId());
    }

    private void listCards() throws SQLException {
        var cardQueryService = new CardQueryService(connection);
        var cards = cardQueryService.findCardsByBoardId(boardId);

        System.out.println("\n=== LISTA DE CARDS ===");
        if (cards.isEmpty()) {
            System.out.println("Nenhum card encontrado neste board.");
        } else {
            cards.forEach(card -> 
                System.out.printf("%d - %s [%s]%n", 
                    card.getId(), 
                    card.getTitle(), 
                    card.getBoardColumn().getName())
            );
        }
    }

    private void moveCard() throws SQLException {
        System.out.print("ID do Card: ");
        var cardId = getLongInput("");

        var boardColumnQueryService = new BoardColumnQueryService(connection);
        var columns = boardColumnQueryService.findColumnsByBoardId(boardId);

        System.out.println("\nColunas disponíveis:");
        columns.forEach(column -> 
            System.out.printf("%d - %s%n", column.getId(), column.getName())
        );

        System.out.print("ID da Coluna de destino: ");
        var targetColumnId = getLongInput("");

        var cardService = new CardService(connection);
        var card = cardService.moveCard(cardId, targetColumnId);
        connection.commit();
        
        System.out.println("Card '" + card.getTitle() + "' movido com sucesso!");
    }

    private void blockCard() throws SQLException {
        System.out.print("ID do Card: ");
        var cardId = getLongInput("");
        
        System.out.print("Motivo do bloqueio: ");
        var reason = scanner.nextLine().trim();

        if (reason.isEmpty()) {
            System.out.println("Motivo do bloqueio não pode estar vazio!");
            return;
        }

        var cardService = new CardService(connection);
        var card = cardService.blockCard(cardId, reason);
        connection.commit();
        
        System.out.println("Card '" + card.getTitle() + "' bloqueado com sucesso!");
    }

    private void unblockCard() throws SQLException {
        System.out.print("ID do Card: ");
        var cardId = getLongInput("");
        
        System.out.print("Motivo do desbloqueio: ");
        var reason = scanner.nextLine().trim();

        if (reason.isEmpty()) {
            System.out.println("Motivo do desbloqueio não pode estar vazio!");
            return;
        }

        var cardService = new CardService(connection);
        var card = cardService.unblockCard(cardId, reason);
        connection.commit();
        
        System.out.println("Card '" + card.getTitle() + "' desbloqueado com sucesso!");
    }

    private void cancelCard() throws SQLException {
        System.out.print("ID do Card: ");
        var cardId = getLongInput("");

        var cardService = new CardService(connection);
        var card = cardService.cancelCard(cardId);
        connection.commit();
        
        System.out.println("Card '" + card.getTitle() + "' cancelado com sucesso!");
    }

    private void showCardDetails() throws SQLException {
        System.out.print("ID do Card: ");
        var cardId = getLongInput("");

        var cardQueryService = new CardQueryService(connection);
        var cardDetails = cardQueryService.getCardDetails(cardId);

        System.out.println("\n=== DETALHES DO CARD ===");
        System.out.println("ID: " + cardDetails.id());
        System.out.println("Título: " + cardDetails.title());
        System.out.println("Descrição: " + cardDetails.description());
        System.out.println("Coluna: " + cardDetails.columnName());
        System.out.println("Bloqueado: " + (cardDetails.blocked() ? "Sim" : "Não"));
        
        if (cardDetails.blocked()) {
            System.out.println("Bloqueado em: " + cardDetails.blockedAt());
            System.out.println("Motivo: " + cardDetails.blockReason());
        }
        
        System.out.println("Total de bloqueios: " + cardDetails.blocksAmount());
    }

    private void updateCard() throws SQLException {
        System.out.print("ID do Card: ");
        var cardId = getLongInput("");

        System.out.print("Novo título: ");
        var title = scanner.nextLine().trim();

        System.out.print("Nova descrição: ");
        var description = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Título não pode estar vazio!");
            return;
        }

        var cardService = new CardService(connection);
        var card = cardService.updateCard(cardId, title, description);
        connection.commit();
        
        System.out.println("Card '" + card.getTitle() + "' atualizado com sucesso!");
    }

    private void deleteCard() throws SQLException {
        System.out.print("ID do Card: ");
        var cardId = getLongInput("");

        System.out.print("Tem certeza que deseja excluir este card? (s/N): ");
        var confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("s") && !confirmation.equals("sim")) {
            System.out.println("Operação cancelada.");
            return;
        }

        var cardService = new CardService(connection);
        cardService.deleteCard(cardId);
        connection.commit();
        
        System.out.println("Card excluído com sucesso!");
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
