package com.example.bff.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SessionInfo(
    String enterpriseId,
    String hsidUuid,
    Instant sessionStartTime,
    Instant sessionEndTime,
    String persona,
    Map<String, List<DelegatePermission>> managedMembers
) {
}
