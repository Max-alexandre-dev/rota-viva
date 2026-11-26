package com.equipe404.rotaviva.transfers;

public final class TransferNotFoundException extends RuntimeException {
    public TransferNotFoundException(String message) {
        super(message);
    }
}
