package com.equipe404.rotaviva.transfers;

import java.util.Map;

public record ApiErrorResponse(String code, String message, Map<String, String> details, String storageMode) {
}
