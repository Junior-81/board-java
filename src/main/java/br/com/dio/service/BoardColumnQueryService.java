package br.com.dio.service;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class BoardColumnQueryService {

    private final Connection connection;

    public List<BoardColumnEntity> findColumnsByBoardId(Long boardId) throws SQLException {
        var boardColumnDAO = new BoardColumnDAO(connection);
        return boardColumnDAO.findByBoardId(boardId);
    }

    public BoardColumnEntity findColumnById(Long columnId) throws SQLException {
        var boardColumnDAO = new BoardColumnDAO(connection);
        return boardColumnDAO.findById(columnId)
                .orElseThrow(() -> new EntityNotFoundException("BoardColumn", columnId));
    }

    public List<BoardColumnInfoDTO> getColumnsInfo(Long boardId) throws SQLException {
        var boardColumnDAO = new BoardColumnDAO(connection);
        var columns = boardColumnDAO.findByBoardId(boardId);
        
        return columns.stream()
                .map(column -> new BoardColumnInfoDTO(
                        column.getId(),
                        column.getOrder(),
                        column.getKind()
                ))
                .toList();
    }

    public List<BoardColumnEntity> findAllColumns() throws SQLException {
        var boardColumnDAO = new BoardColumnDAO(connection);
        return boardColumnDAO.findAll();
    }

}
