package br.com.dio.persistence.dao;

import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardColumnDAO {

    private final Connection connection;

    public BoardColumnEntity save(BoardColumnEntity boardColumn) throws SQLException {
        if (boardColumn.getId() == null) {
            return insert(boardColumn);
        } else {
            return update(boardColumn);
        }
    }

    private BoardColumnEntity insert(BoardColumnEntity boardColumn) throws SQLException {
        var sql = "INSERT INTO board_column (name, column_order, kind, board_id) VALUES (?, ?, ?, ?)";
        try (var stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, boardColumn.getName());
            stmt.setInt(2, boardColumn.getOrder());
            stmt.setString(3, boardColumn.getKind().name());
            stmt.setLong(4, boardColumn.getBoard().getId());
            stmt.executeUpdate();
            
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    boardColumn.setId(rs.getLong(1));
                }
            }
        }
        return boardColumn;
    }

    private BoardColumnEntity update(BoardColumnEntity boardColumn) throws SQLException {
        var sql = "UPDATE board_column SET name = ?, column_order = ?, kind = ?, board_id = ? WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, boardColumn.getName());
            stmt.setInt(2, boardColumn.getOrder());
            stmt.setString(3, boardColumn.getKind().name());
            stmt.setLong(4, boardColumn.getBoard().getId());
            stmt.setLong(5, boardColumn.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("BoardColumn", boardColumn.getId());
            }
        }
        return boardColumn;
    }

    public Optional<BoardColumnEntity> findById(Long id) throws SQLException {
        var sql = """
            SELECT bc.id, bc.name, bc.column_order, bc.kind, bc.board_id,
                   b.name as board_name
            FROM board_column bc 
            INNER JOIN board b ON bc.board_id = b.id 
            WHERE bc.id = ?
            """;
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var boardColumn = new BoardColumnEntity();
                    boardColumn.setId(rs.getLong("id"));
                    boardColumn.setName(rs.getString("name"));
                    boardColumn.setOrder(rs.getInt("column_order"));
                    boardColumn.setKind(BoardColumnKindEnum.findByName(rs.getString("kind")));
                    
                    var board = new BoardEntity();
                    board.setId(rs.getLong("board_id"));
                    board.setName(rs.getString("board_name"));
                    boardColumn.setBoard(board);
                    
                    return Optional.of(boardColumn);
                }
            }
        }
        return Optional.empty();
    }

    public List<BoardColumnEntity> findByBoardId(Long boardId) throws SQLException {
        var columns = new ArrayList<BoardColumnEntity>();
        var sql = """
            SELECT bc.id, bc.name, bc.column_order, bc.kind, bc.board_id,
                   b.name as board_name
            FROM board_column bc 
            INNER JOIN board b ON bc.board_id = b.id 
            WHERE bc.board_id = ? 
            ORDER BY bc.column_order
            """;
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, boardId);
            
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    var boardColumn = new BoardColumnEntity();
                    boardColumn.setId(rs.getLong("id"));
                    boardColumn.setName(rs.getString("name"));
                    boardColumn.setOrder(rs.getInt("column_order"));
                    boardColumn.setKind(BoardColumnKindEnum.findByName(rs.getString("kind")));
                    
                    var board = new BoardEntity();
                    board.setId(rs.getLong("board_id"));
                    board.setName(rs.getString("board_name"));
                    boardColumn.setBoard(board);
                    
                    columns.add(boardColumn);
                }
            }
        }
        return columns;
    }

    public List<BoardColumnEntity> findAll() throws SQLException {
        var columns = new ArrayList<BoardColumnEntity>();
        var sql = """
            SELECT bc.id, bc.name, bc.column_order, bc.kind, bc.board_id,
                   b.name as board_name
            FROM board_column bc 
            INNER JOIN board b ON bc.board_id = b.id 
            ORDER BY b.name, bc.column_order
            """;
        
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                var boardColumn = new BoardColumnEntity();
                boardColumn.setId(rs.getLong("id"));
                boardColumn.setName(rs.getString("name"));
                boardColumn.setOrder(rs.getInt("column_order"));
                boardColumn.setKind(BoardColumnKindEnum.findByName(rs.getString("kind")));
                
                var board = new BoardEntity();
                board.setId(rs.getLong("board_id"));
                board.setName(rs.getString("board_name"));
                boardColumn.setBoard(board);
                
                columns.add(boardColumn);
            }
        }
        return columns;
    }

    public void deleteById(Long id) throws SQLException {
        var sql = "DELETE FROM board_column WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("BoardColumn", id);
            }
        }
    }

    public boolean existsById(Long id) throws SQLException {
        var sql = "SELECT 1 FROM board_column WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (var rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Optional<BoardColumnEntity> findByBoardIdAndKind(Long boardId, BoardColumnKindEnum kind) throws SQLException {
        var sql = """
            SELECT bc.id, bc.name, bc.column_order, bc.kind, bc.board_id,
                   b.name as board_name
            FROM board_column bc 
            INNER JOIN board b ON bc.board_id = b.id 
            WHERE bc.board_id = ? AND bc.kind = ?
            """;
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, boardId);
            stmt.setString(2, kind.name());
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var boardColumn = new BoardColumnEntity();
                    boardColumn.setId(rs.getLong("id"));
                    boardColumn.setName(rs.getString("name"));
                    boardColumn.setOrder(rs.getInt("column_order"));
                    boardColumn.setKind(BoardColumnKindEnum.findByName(rs.getString("kind")));
                    
                    var board = new BoardEntity();
                    board.setId(rs.getLong("board_id"));
                    board.setName(rs.getString("board_name"));
                    boardColumn.setBoard(board);
                    
                    return Optional.of(boardColumn);
                }
            }
        }
        return Optional.empty();
    }

}
