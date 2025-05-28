package br.com.dio.persistence.dao;

import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardDAO {

    private final Connection connection;

    public BoardEntity save(BoardEntity board) throws SQLException {
        if (board.getId() == null) {
            return insert(board);
        } else {
            return update(board);
        }
    }

    private BoardEntity insert(BoardEntity board) throws SQLException {
        var sql = "INSERT INTO board (name) VALUES (?)";
        try (var stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, board.getName());
            stmt.executeUpdate();
            
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    board.setId(rs.getLong(1));
                }
            }
        }
        return board;
    }

    private BoardEntity update(BoardEntity board) throws SQLException {
        var sql = "UPDATE board SET name = ? WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, board.getName());
            stmt.setLong(2, board.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Board", board.getId());
            }
        }
        return board;
    }

    public Optional<BoardEntity> findById(Long id) throws SQLException {
        var sql = "SELECT id, name FROM board WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var board = new BoardEntity();
                    board.setId(rs.getLong("id"));
                    board.setName(rs.getString("name"));
                    return Optional.of(board);
                }
            }
        }
        return Optional.empty();
    }

    public List<BoardEntity> findAll() throws SQLException {
        var boards = new ArrayList<BoardEntity>();
        var sql = "SELECT id, name FROM board ORDER BY name";
        
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                var board = new BoardEntity();
                board.setId(rs.getLong("id"));
                board.setName(rs.getString("name"));
                boards.add(board);
            }
        }
        return boards;
    }

    public void deleteById(Long id) throws SQLException {
        var sql = "DELETE FROM board WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Board", id);
            }
        }
    }

    public boolean existsById(Long id) throws SQLException {
        var sql = "SELECT 1 FROM board WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (var rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

}
