package org.tmplsync.engagement.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record EngagementDecidedRequest(
        @NotNull(message = "decision must not be null") DecisionType decision,
        Integer decidedVersion
) {
}
