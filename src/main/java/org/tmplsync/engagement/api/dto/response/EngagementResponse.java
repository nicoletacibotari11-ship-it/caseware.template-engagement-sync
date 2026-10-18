package org.tmplsync.engagement.api.dto.response;

import org.tmplsync.engagement.model.enums.EngagementStatus;
import org.tmplsync.engagement.model.EngagementVersion;

import java.util.UUID;

public record EngagementResponse(
        UUID engagementId,
        UUID firmId,
        UUID templateId,
        int appliedVersion,
        int latestKnownVersion,
        EngagementStatus status
) {
    public static EngagementResponse from(EngagementVersion engagementVersion) {
        return new EngagementResponse(
                engagementVersion.getEngagementId(),
                engagementVersion.getFirmId(),
                engagementVersion.getTemplateId(),
                engagementVersion.getAppliedVersion(),
                engagementVersion.getLatestKnownVersion(),
                engagementVersion.getStatus()
        );
    }
}
