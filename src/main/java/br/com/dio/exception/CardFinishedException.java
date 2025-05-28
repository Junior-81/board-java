package br.com.dio.exception;

/**
 * Exceção lançada quando se tenta realizar uma operação em um card que já foi finalizado.
 */
public class CardFinishedException extends RuntimeException {

    public CardFinishedException(String message) {
        super(message);
    }

    public CardFinishedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardFinishedException(Long cardId) {
        super("O card com ID " + cardId + " já foi finalizado e não pode ser modificado.");
    }

}
