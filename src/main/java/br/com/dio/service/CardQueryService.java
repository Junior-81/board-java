package br.com.dio.service;

import br.com.dio.dto.CardDetailsDTO;
import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.dao.BlockDAO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class CardQueryService {

    private final Connection connection;

    public List<CardEntity> findCardsByBoardId(Long boardId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        return cardDAO.findByBoardId(boardId);
    }

    public List<CardEntity> findCardsByColumnId(Long columnId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        return cardDAO.findByBoardColumnId(columnId);
    }

    public CardEntity findCardById(Long cardId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        return cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));
    }

    public CardDetailsDTO getCardDetails(Long cardId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        var blockDAO = new BlockDAO(connection);

        var card = cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));

        // Verificar se o card está bloqueado
        var activeBlock = blockDAO.findActiveBlockByCardId(cardId);
        boolean isBlocked = activeBlock.isPresent();
        
        // Contar total de bloqueios
        int blocksAmount = blockDAO.countBlocksByCardId(cardId);

        return new CardDetailsDTO(
                card.getId(),
                card.getTitle(),
                card.getDescription(),
                isBlocked,
                isBlocked ? activeBlock.get().getBlockedAt() : null,
                isBlocked ? activeBlock.get().getBlockReason() : null,
                blocksAmount,
                card.getBoardColumn().getId(),
                card.getBoardColumn().getName()
        );
    }

    public List<CardEntity> findAllCards() throws SQLException {
        var cardDAO = new CardDAO(connection);
        return cardDAO.findAll();
    }

    public List<CardEntity> findBlockedCards() throws SQLException {
        var cardDAO = new CardDAO(connection);
        var blockDAO = new BlockDAO(connection);
        
        return cardDAO.findAll().stream()
                .filter(card -> {
                    try {
                        return blockDAO.findActiveBlockByCardId(card.getId()).isPresent();
                    } catch (SQLException e) {
                        throw new RuntimeException("Erro ao verificar bloqueio do card: " + card.getId(), e);
                    }
                })
                .toList();
    }

}
