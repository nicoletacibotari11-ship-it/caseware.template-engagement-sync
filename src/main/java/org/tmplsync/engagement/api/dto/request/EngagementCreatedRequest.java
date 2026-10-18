package org.tmplsync.engagement.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EngagementCreatedRequest(
        @NotNull(message = "firmId must not be null") UUID firmId,
        @NotNull(message = "templateId must not be null") UUID templateId,
        int appliedVersion
) {
}
