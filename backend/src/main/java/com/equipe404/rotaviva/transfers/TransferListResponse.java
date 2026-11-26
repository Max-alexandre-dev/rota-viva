package com.equipe404.rotaviva.transfers;

import java.util.List;

public record TransferListResponse(String storageMode, List<TransferResponse> transfers) {
}
