package br.com.dio.persistence.dao;

import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class CardDAO {

    private final Connection connection;

    public CardEntity save(CardEntity card) throws SQLException {
        if (card.getId() == null) {
            return insert(card);
        } else {
            return update(card);
        }
    }

    private CardEntity insert(CardEntity card) throws SQLException {
        var sql = "INSERT INTO card (title, description, board_column_id) VALUES (?, ?, ?)";
        try (var stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, card.getTitle());
            stmt.setString(2, card.getDescription());
            stmt.setLong(3, card.getBoardColumn().getId());
            stmt.executeUpdate();
            
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    card.setId(rs.getLong(1));
                }
            }
        }
        return card;
    }

    private CardEntity update(CardEntity card) throws SQLException {
        var sql = "UPDATE card SET title = ?, description = ?, board_column_id = ? WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, card.getTitle());
            stmt.setString(2, card.getDescription());
            stmt.setLong(3, card.getBoardColumn().getId());
            stmt.setLong(4, card.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Card", card.getId());
            }
        }
        return card;
    }

    public Optional<CardEntity> findById(Long id) throws SQLException {
        var sql = """
            SELECT c.id, c.title, c.description, c.board_column_id,
                   bc.name as column_name, bc.column_order, bc.kind, bc.board_id,
                   b.name as board_name
            FROM card c 
            INNER JOIN board_column bc ON c.board_column_id = bc.id 
            INNER JOIN board b ON bc.board_id = b.id
            WHERE c.id = ?
            """;
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var card = new CardEntity();
                    card.setId(rs.getLong("id"));
                    card.setTitle(rs.getString("title"));
                    card.setDescription(rs.getString("description"));
                    
                    var boardColumn = new BoardColumnEntity();
                    boardColumn.setId(rs.getLong("board_column_id"));
                    boardColumn.setName(rs.getString("column_name"));
                    boardColumn.setOrder(rs.getInt("column_order"));
                    boardColumn.setKind(BoardColumnKindEnum.findByName(rs.getString("kind")));
                    
                    var board = new BoardEntity();
                    board.setId(rs.getLong("board_id"));
                    board.setName(rs.getString("board_name"));
                    boardColumn.setBoard(board);
                    
                    card.setBoardColumn(boardColumn);
                    
                    return Optional.of(card);
                }
            }
        }
        return Optional.empty();
    }

    public List<CardEntity> findByBoardColumnId(Long boardColumnId) throws SQLException {
        var cards = new ArrayList<CardEntity>();
        var sql = """
            SELECT c.id, c.title, c.description, c.board_column_id,
                   bc.name as column_name, bc.column_order, bc.kind, bc.board_id,
                   b.name as board_name
            FROM card c 
            INNER JOIN board_column bc ON c.board_column_id = bc.id 
            INNER JOIN board b ON bc.board_id = b.id
            WHERE c.board_column_id = ?
            ORDER BY c.title
            """;
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, boardColumnId);
            
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    var card = new CardEntity();
                    card.setId(rs.getLong("id"));
                    card.setTitle(rs.getString("title"));
                    card.setDescription(rs.getString("description"));
                    
                    var boardColumn = new BoardColumnEntity();
                    boardColumn.setId(rs.getLong("board_column_id"));
                    boardColumn.setName(rs.getString("column_name"));
                    boardColumn.setOrder(rs.getInt("column_order"));
                    boardColumn.setKind(BoardColumnKindEnum.findByName(rs.getString("kind")));
                    
                    var board = new BoardEntity();
                    board.setId(rs.getLong("board_id"));
                    board.setName(rs.getString("board_name"));
                    boardColumn.setBoard(board);
                    
                    card.setBoardColumn(boardColumn);
                    cards.add(card);
                }
            }
        }
        return cards;
    }

    public List<CardEntity> findByBoardId(Long boardId) throws SQLException {
        var cards = new ArrayList<CardEntity>();
        var sql = """
            SELECT c.id, c.title, c.description, c.board_column_id,
                   bc.name as column_name, bc.column_order, bc.kind, bc.board_id,
                   b.name as board_name
            FROM card c 
            INNER JOIN board_column bc ON c.board_column_id = bc.id 
            INNER JOIN board b ON bc.board_id = b.id
            WHERE bc.board_id = ?
            ORDER BY bc.column_order, c.title
            """;
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, boardId);
            
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    var card = new CardEntity();
                    card.setId(rs.getLong("id"));
                    card.setTitle(rs.getString("title"));
                    card.setDescription(rs.getString("description"));
                    
                    var boardColumn = new BoardColumnEntity();
                    boardColumn.setId(rs.getLong("board_column_id"));
                    boardColumn.setName(rs.getString("column_name"));
                    boardColumn.setOrder(rs.getInt("column_order"));
                    boardColumn.setKind(BoardColumnKindEnum.findByName(rs.getString("kind")));
                    
                    var board = new BoardEntity();
                    board.setId(rs.getLong("board_id"));
                    board.setName(rs.getString("board_name"));
                    boardColumn.setBoard(board);
                    
                    card.setBoardColumn(boardColumn);
                    cards.add(card);
                }
            }
        }
        return cards;
    }

    public List<CardEntity> findAll() throws SQLException {
        var cards = new ArrayList<CardEntity>();
        var sql = """
            SELECT c.id, c.title, c.description, c.board_column_id,
                   bc.name as column_name, bc.column_order, bc.kind, bc.board_id,
                   b.name as board_name
            FROM card c 
            INNER JOIN board_column bc ON c.board_column_id = bc.id 
            INNER JOIN board b ON bc.board_id = b.id
            ORDER BY b.name, bc.column_order, c.title
            """;
        
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                var card = new CardEntity();
                card.setId(rs.getLong("id"));
                card.setTitle(rs.getString("title"));
                card.setDescription(rs.getString("description"));
                
                var boardColumn = new BoardColumnEntity();
                boardColumn.setId(rs.getLong("board_column_id"));
                boardColumn.setName(rs.getString("column_name"));
                boardColumn.setOrder(rs.getInt("column_order"));
                boardColumn.setKind(BoardColumnKindEnum.findByName(rs.getString("kind")));
                
                var board = new BoardEntity();
                board.setId(rs.getLong("board_id"));
                board.setName(rs.getString("board_name"));
                boardColumn.setBoard(board);
                
                card.setBoardColumn(boardColumn);
                cards.add(card);
            }
        }
        return cards;
    }

    public void deleteById(Long id) throws SQLException {
        var sql = "DELETE FROM card WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Card", id);
            }
        }
    }

    public boolean existsById(Long id) throws SQLException {
        var sql = "SELECT 1 FROM card WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (var rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countByBoardColumnId(Long boardColumnId) throws SQLException {
        var sql = "SELECT COUNT(*) FROM card WHERE board_column_id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, boardColumnId);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

}
