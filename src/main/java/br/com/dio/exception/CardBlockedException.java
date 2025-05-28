package br.com.dio.exception;

/**
 * Exceção lançada quando se tenta realizar uma operação em um card que está bloqueado.
 */
public class CardBlockedException extends RuntimeException {

    public CardBlockedException(String message) {
        super(message);
    }

    public CardBlockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardBlockedException(Long cardId) {
        super("O card com ID " + cardId + " está bloqueado e não pode ser movido ou finalizado.");
    }

}
