package com.equipe404.rotaviva.transfers;

public final class TransferConflictException extends RuntimeException {
    public TransferConflictException(String message) {
        super(message);
    }

    public TransferConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
