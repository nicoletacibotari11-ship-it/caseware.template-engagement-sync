package org.tmplsync.engagement.exception;

import java.util.UUID;

public class EngagementNotFoundException extends RuntimeException {

    public EngagementNotFoundException(UUID engagementId) {
        super("Engagement not found: " + engagementId);
    }
}
