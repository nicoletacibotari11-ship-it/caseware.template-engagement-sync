package org.tmplsync.engagement.api.dto.response;

import java.util.UUID;

public record PendingEngagementResponse(
        UUID engagementId,
        UUID templateId,
        int appliedVersion,
        int latestKnownVersion
) {
}
