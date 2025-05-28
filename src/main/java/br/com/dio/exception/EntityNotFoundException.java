package br.com.dio.exception;

/**
 * Exceção lançada quando uma entidade não é encontrada no banco de dados.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityNotFoundException(Class<?> entityClass, Long id) {
        super(entityClass.getSimpleName() + " com ID " + id + " não foi encontrado.");
    }

    public EntityNotFoundException(String entityName, Long id) {
        super(entityName + " com ID " + id + " não foi encontrado.");
    }

}
