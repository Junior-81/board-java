package br.com.dio.service;

import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class BoardService {

    private final Connection connection;

    public BoardEntity createBoard(String name) throws SQLException {
        var boardDAO = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);

        // Criar o board
        var board = new BoardEntity();
        board.setName(name);
        board = boardDAO.save(board);

        // Criar as colunas padrão
        createDefaultColumns(board, boardColumnDAO);

        return board;
    }

    private void createDefaultColumns(BoardEntity board, BoardColumnDAO boardColumnDAO) throws SQLException {
        // Coluna inicial
        var initialColumn = new BoardColumnEntity();
        initialColumn.setName("Para Fazer");
        initialColumn.setOrder(1);
        initialColumn.setKind(BoardColumnKindEnum.INITIAL);
        initialColumn.setBoard(board);
        boardColumnDAO.save(initialColumn);

        // Coluna em progresso
        var progressColumn = new BoardColumnEntity();
        progressColumn.setName("Em Progresso");
        progressColumn.setOrder(2);
        progressColumn.setKind(BoardColumnKindEnum.PENDING);
        progressColumn.setBoard(board);
        boardColumnDAO.save(progressColumn);

        // Coluna finalizada
        var finalColumn = new BoardColumnEntity();
        finalColumn.setName("Finalizado");
        finalColumn.setOrder(3);
        finalColumn.setKind(BoardColumnKindEnum.FINAL);
        finalColumn.setBoard(board);
        boardColumnDAO.save(finalColumn);

        // Coluna cancelada
        var cancelColumn = new BoardColumnEntity();
        cancelColumn.setName("Cancelado");
        cancelColumn.setOrder(4);
        cancelColumn.setKind(BoardColumnKindEnum.CANCEL);
        cancelColumn.setBoard(board);
        boardColumnDAO.save(cancelColumn);
    }

    public BoardEntity updateBoard(Long id, String name) throws SQLException {
        var boardDAO = new BoardDAO(connection);
        
        var board = boardDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Board", id));
        
        board.setName(name);
        return boardDAO.save(board);
    }

    public void deleteBoard(Long id) throws SQLException {
        var boardDAO = new BoardDAO(connection);
        
        if (!boardDAO.existsById(id)) {
            throw new EntityNotFoundException("Board", id);
        }
        
        boardDAO.deleteById(id);
    }

    public BoardEntity findById(Long id) throws SQLException {
        var boardDAO = new BoardDAO(connection);
        return boardDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Board", id));
    }

    public BoardEntity createBoardWithCustomColumns(String boardName, String[] columnNames) throws SQLException {
        var boardDAO = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);

        // Criar o board
        var board = new BoardEntity();
        board.setName(boardName);
        board = boardDAO.save(board);

        // Criar colunas customizadas
        for (int i = 0; i < columnNames.length; i++) {
            var column = new BoardColumnEntity();
            column.setName(columnNames[i]);
            column.setOrder(i + 1);
            
            // Definir o tipo da coluna baseado na posição
            if (i == 0) {
                column.setKind(BoardColumnKindEnum.INITIAL);
            } else if (i == columnNames.length - 1) {
                column.setKind(BoardColumnKindEnum.FINAL);
            } else {
                column.setKind(BoardColumnKindEnum.PENDING);
            }
            
            column.setBoard(board);
            boardColumnDAO.save(column);
        }

        // Sempre criar uma coluna de cancelamento
        var cancelColumn = new BoardColumnEntity();
        cancelColumn.setName("Cancelado");
        cancelColumn.setOrder(columnNames.length + 1);
        cancelColumn.setKind(BoardColumnKindEnum.CANCEL);
        cancelColumn.setBoard(board);
        boardColumnDAO.save(cancelColumn);

        return board;
    }

}
