package br.com.dio.service;

import br.com.dio.exception.CardBlockedException;
import br.com.dio.exception.CardFinishedException;
import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.dao.BlockDAO;
import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.entity.BlockEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static br.com.dio.persistence.entity.BoardColumnKindEnum.*;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity createCard(String title, String description, Long boardId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);

        // Buscar a coluna inicial do board
        var initialColumn = boardColumnDAO.findByBoardIdAndKind(boardId, INITIAL)
                .orElseThrow(() -> new EntityNotFoundException("Coluna inicial não encontrada para o board com ID " + boardId));

        var card = new CardEntity();
        card.setTitle(title);
        card.setDescription(description);
        card.setBoardColumn(initialColumn);

        return cardDAO.save(card);
    }

    public CardEntity updateCard(Long cardId, String title, String description) throws SQLException {
        var cardDAO = new CardDAO(connection);
        var blockDAO = new BlockDAO(connection);

        var card = cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));

        // Verificar se o card está bloqueado
        if (isCardBlocked(cardId, blockDAO)) {
            throw new CardBlockedException(cardId);
        }

        // Verificar se o card está finalizado
        if (isCardFinished(card)) {
            throw new CardFinishedException(cardId);
        }

        card.setTitle(title);
        card.setDescription(description);

        return cardDAO.save(card);
    }

    public CardEntity moveCard(Long cardId, Long targetColumnId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        var blockDAO = new BlockDAO(connection);

        var card = cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));

        var targetColumn = boardColumnDAO.findById(targetColumnId)
                .orElseThrow(() -> new EntityNotFoundException("BoardColumn", targetColumnId));

        // Verificar se o card está bloqueado
        if (isCardBlocked(cardId, blockDAO)) {
            throw new CardBlockedException(cardId);
        }

        // Verificar se o card já está finalizado e tentando mover para uma coluna que não é final
        if (isCardFinished(card) && !targetColumn.getKind().equals(FINAL)) {
            throw new CardFinishedException(cardId);
        }

        card.setBoardColumn(targetColumn);
        return cardDAO.save(card);
    }

    public void deleteCard(Long cardId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        var blockDAO = new BlockDAO(connection);

        var card = cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));

        // Verificar se o card está bloqueado
        if (isCardBlocked(cardId, blockDAO)) {
            throw new CardBlockedException(cardId);
        }

        cardDAO.deleteById(cardId);
    }

    public CardEntity blockCard(Long cardId, String reason) throws SQLException {
        var cardDAO = new CardDAO(connection);
        var blockDAO = new BlockDAO(connection);

        var card = cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));

        // Verificar se o card já está bloqueado
        if (isCardBlocked(cardId, blockDAO)) {
            throw new CardBlockedException("O card com ID " + cardId + " já está bloqueado.");
        }

        // Criar um novo bloqueio
        var block = new BlockEntity();
        block.setBlockedAt(OffsetDateTime.now());
        block.setBlockReason(reason);
        block = blockDAO.save(block);

        // Associar o bloqueio ao card
        blockDAO.linkCardToBlock(cardId, block.getId());

        return card;
    }

    public CardEntity unblockCard(Long cardId, String reason) throws SQLException {
        var cardDAO = new CardDAO(connection);
        var blockDAO = new BlockDAO(connection);

        var card = cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));

        // Buscar o bloqueio ativo
        var activeBlock = blockDAO.findActiveBlockByCardId(cardId)
                .orElseThrow(() -> new RuntimeException("O card com ID " + cardId + " não está bloqueado."));

        // Desbloquear
        activeBlock.setUnblockedAt(OffsetDateTime.now());
        activeBlock.setUnblockReason(reason);
        blockDAO.save(activeBlock);

        return card;
    }

    public CardEntity cancelCard(Long cardId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        var blockDAO = new BlockDAO(connection);

        var card = cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));

        // Verificar se o card está bloqueado
        if (isCardBlocked(cardId, blockDAO)) {
            throw new CardBlockedException(cardId);
        }

        // Buscar a coluna de cancelamento do board
        var boardId = card.getBoardColumn().getBoard().getId();
        var cancelColumn = boardColumnDAO.findByBoardIdAndKind(boardId, CANCEL)
                .orElseThrow(() -> new EntityNotFoundException("Coluna de cancelamento não encontrada para o board."));

        card.setBoardColumn(cancelColumn);
        return cardDAO.save(card);
    }

    public CardEntity findById(Long cardId) throws SQLException {
        var cardDAO = new CardDAO(connection);
        return cardDAO.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card", cardId));
    }

    private boolean isCardBlocked(Long cardId, BlockDAO blockDAO) throws SQLException {
        return blockDAO.findActiveBlockByCardId(cardId).isPresent();
    }

    private boolean isCardFinished(CardEntity card) {
        return card.getBoardColumn().getKind().equals(FINAL) || 
               card.getBoardColumn().getKind().equals(CANCEL);
    }

}
