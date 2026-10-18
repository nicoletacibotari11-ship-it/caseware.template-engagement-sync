package org.tmplsync.engagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tmplsync.engagement.model.enums.EngagementStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "engagement_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EngagementVersion {

    @Id
    @Column(name = "engagement_id", updatable = false, nullable = false)
    private UUID engagementId;

    @Column(name = "firm_id", nullable = false)
    private UUID firmId;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "applied_version", nullable = false)
    private int appliedVersion;

    @Column(name = "latest_known_version", nullable = false)
    private int latestKnownVersion;

    @Column(name = "status", nullable = false)
    private EngagementStatus status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
