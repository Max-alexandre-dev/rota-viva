package com.equipe404.rotaviva.transfers;

import com.equipe404.rotaviva.domain.StockBalance;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Fixed synthetic centers, resource, and stock used by the in-memory demo. */
@Component
public final class DemoInventory {
    public static final String STORAGE_MODE = "DEMO_IN_MEMORY";

    public static final UUID ORIGIN_CENTER_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    public static final UUID DESTINATION_CENTER_ID = UUID.fromString("10000000-0000-4000-8000-000000000002");
    public static final UUID RESOURCE_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    public static final UUID LIMITED_CENTER_ID = UUID.fromString("10000000-0000-4000-8000-000000000003");

    private final Set<UUID> centerIds = Set.of(ORIGIN_CENTER_ID, DESTINATION_CENTER_ID, LIMITED_CENTER_ID);
    private final Set<UUID> resourceIds = Set.of(RESOURCE_ID);
    private final Map<BalanceKey, StockBalance> balances = Map.of(
            new BalanceKey(ORIGIN_CENTER_ID, RESOURCE_ID), new StockBalance(ORIGIN_CENTER_ID, RESOURCE_ID, 100_000, 0, 100),
            new BalanceKey(DESTINATION_CENTER_ID, RESOURCE_ID), new StockBalance(DESTINATION_CENTER_ID, RESOURCE_ID, 5_000, 0, 100),
            new BalanceKey(LIMITED_CENTER_ID, RESOURCE_ID), new StockBalance(LIMITED_CENTER_ID, RESOURCE_ID, 10, 0, 0)
    );

    public boolean containsCenter(UUID centerId) {
        return centerIds.contains(centerId);
    }

    public boolean containsResource(UUID resourceId) {
        return resourceIds.contains(resourceId);
    }

    public StockBalance findBalance(UUID centerId, UUID resourceId) {
        return balances.get(new BalanceKey(centerId, resourceId));
    }

    private record BalanceKey(UUID centerId, UUID resourceId) {
    }
}
