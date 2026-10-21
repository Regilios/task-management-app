package dev.stockflow.core.exceprion;

public class DialogException extends RuntimeException {
    public DialogException(String message) {
        super(message);
    }

    public DialogException(String message, Throwable cause) {
        super(message, cause);
    }
}
