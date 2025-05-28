package br.com.dio.persistence.dao;

import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.entity.BlockEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;
import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toTimestamp;

@AllArgsConstructor
public class BlockDAO {

    private final Connection connection;

    public BlockEntity save(BlockEntity block) throws SQLException {
        if (block.getId() == null) {
            return insert(block);
        } else {
            return update(block);
        }
    }

    private BlockEntity insert(BlockEntity block) throws SQLException {
        var sql = "INSERT INTO block_card (blocked_at, block_reason, unblocked_at, unblock_reason) VALUES (?, ?, ?, ?)";
        try (var stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, toTimestamp(block.getBlockedAt()));
            stmt.setString(2, block.getBlockReason());
            stmt.setTimestamp(3, toTimestamp(block.getUnblockedAt()));
            stmt.setString(4, block.getUnblockReason());
            stmt.executeUpdate();
            
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    block.setId(rs.getLong(1));
                }
            }
        }
        return block;
    }

    private BlockEntity update(BlockEntity block) throws SQLException {
        var sql = "UPDATE block_card SET blocked_at = ?, block_reason = ?, unblocked_at = ?, unblock_reason = ? WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, toTimestamp(block.getBlockedAt()));
            stmt.setString(2, block.getBlockReason());
            stmt.setTimestamp(3, toTimestamp(block.getUnblockedAt()));
            stmt.setString(4, block.getUnblockReason());
            stmt.setLong(5, block.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Block", block.getId());
            }
        }
        return block;
    }

    public Optional<BlockEntity> findById(Long id) throws SQLException {
        var sql = "SELECT id, blocked_at, block_reason, unblocked_at, unblock_reason FROM block_card WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var block = new BlockEntity();
                    block.setId(rs.getLong("id"));
                    block.setBlockedAt(toOffsetDateTime(rs.getTimestamp("blocked_at")));
                    block.setBlockReason(rs.getString("block_reason"));
                    block.setUnblockedAt(toOffsetDateTime(rs.getTimestamp("unblocked_at")));
                    block.setUnblockReason(rs.getString("unblock_reason"));
                    return Optional.of(block);
                }
            }
        }
        return Optional.empty();
    }

    public List<BlockEntity> findByCardId(Long cardId) throws SQLException {
        var blocks = new ArrayList<BlockEntity>();
        var sql = """
            SELECT bc.id, bc.blocked_at, bc.block_reason, bc.unblocked_at, bc.unblock_reason
            FROM block_card bc
            INNER JOIN card_block cb ON bc.id = cb.block_id
            WHERE cb.card_id = ?
            ORDER BY bc.blocked_at DESC
            """;
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    var block = new BlockEntity();
                    block.setId(rs.getLong("id"));
                    block.setBlockedAt(toOffsetDateTime(rs.getTimestamp("blocked_at")));
                    block.setBlockReason(rs.getString("block_reason"));
                    block.setUnblockedAt(toOffsetDateTime(rs.getTimestamp("unblocked_at")));
                    block.setUnblockReason(rs.getString("unblock_reason"));
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public List<BlockEntity> findAll() throws SQLException {
        var blocks = new ArrayList<BlockEntity>();
        var sql = "SELECT id, blocked_at, block_reason, unblocked_at, unblock_reason FROM block_card ORDER BY blocked_at DESC";
        
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                var block = new BlockEntity();
                block.setId(rs.getLong("id"));
                block.setBlockedAt(toOffsetDateTime(rs.getTimestamp("blocked_at")));
                block.setBlockReason(rs.getString("block_reason"));
                block.setUnblockedAt(toOffsetDateTime(rs.getTimestamp("unblocked_at")));
                block.setUnblockReason(rs.getString("unblock_reason"));
                blocks.add(block);
            }
        }
        return blocks;
    }

    public void deleteById(Long id) throws SQLException {
        var sql = "DELETE FROM block_card WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Block", id);
            }
        }
    }

    public boolean existsById(Long id) throws SQLException {
        var sql = "SELECT 1 FROM block_card WHERE id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (var rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void linkCardToBlock(Long cardId, Long blockId) throws SQLException {
        var sql = "INSERT INTO card_block (card_id, block_id) VALUES (?, ?)";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            stmt.setLong(2, blockId);
            stmt.executeUpdate();
        }
    }

    public void unlinkCardFromBlock(Long cardId, Long blockId) throws SQLException {
        var sql = "DELETE FROM card_block WHERE card_id = ? AND block_id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            stmt.setLong(2, blockId);
            stmt.executeUpdate();
        }
    }

    public Optional<BlockEntity> findActiveBlockByCardId(Long cardId) throws SQLException {
        var sql = """
            SELECT bc.id, bc.blocked_at, bc.block_reason, bc.unblocked_at, bc.unblock_reason
            FROM block_card bc
            INNER JOIN card_block cb ON bc.id = cb.block_id
            WHERE cb.card_id = ? AND bc.unblocked_at IS NULL
            ORDER BY bc.blocked_at DESC
            LIMIT 1
            """;
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var block = new BlockEntity();
                    block.setId(rs.getLong("id"));
                    block.setBlockedAt(toOffsetDateTime(rs.getTimestamp("blocked_at")));
                    block.setBlockReason(rs.getString("block_reason"));
                    block.setUnblockedAt(toOffsetDateTime(rs.getTimestamp("unblocked_at")));
                    block.setUnblockReason(rs.getString("unblock_reason"));
                    return Optional.of(block);
                }
            }
        }
        return Optional.empty();
    }

    public int countBlocksByCardId(Long cardId) throws SQLException {
        var sql = "SELECT COUNT(*) FROM card_block WHERE card_id = ?";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

}
