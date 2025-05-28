package br.com.dio.service;

import br.com.dio.dto.BoardDetailsDTO;
import br.com.dio.dto.BoardColumnDTO;
import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class BoardQueryService {

    private final Connection connection;

    public List<BoardEntity> findAllBoards() throws SQLException {
        var boardDAO = new BoardDAO(connection);
        return boardDAO.findAll();
    }

    public BoardEntity findBoardById(Long id) throws SQLException {
        var boardDAO = new BoardDAO(connection);
        return boardDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Board", id));
    }

    public BoardDetailsDTO getBoardDetails(Long boardId) throws SQLException {
        var boardDAO = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        var cardDAO = new CardDAO(connection);

        var board = boardDAO.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board", boardId));

        var columns = boardColumnDAO.findByBoardId(boardId);
        
        var columnDTOs = columns.stream()
                .map(column -> {
                    try {
                        int cardsAmount = cardDAO.countByBoardColumnId(column.getId());
                        return new BoardColumnDTO(
                                column.getId(),
                                column.getName(),
                                column.getKind(),
                                cardsAmount
                        );
                    } catch (SQLException e) {
                        throw new RuntimeException("Erro ao contar cards da coluna: " + column.getId(), e);
                    }
                })
                .toList();

        return new BoardDetailsDTO(board.getId(), board.getName(), columnDTOs);
    }

}
