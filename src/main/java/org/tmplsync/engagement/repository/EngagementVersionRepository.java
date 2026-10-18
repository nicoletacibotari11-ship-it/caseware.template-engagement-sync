package org.tmplsync.engagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tmplsync.engagement.model.enums.EngagementStatus;
import org.tmplsync.engagement.model.EngagementVersion;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface EngagementVersionRepository extends JpaRepository<EngagementVersion, UUID> {

    Page<EngagementVersion> findByFirmIdAndStatus(UUID firmId, EngagementStatus status, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE EngagementVersion e
        SET e.status = org.tmplsync.engagement.model.enums.EngagementStatus.PENDING,
            e.latestKnownVersion = :newVersion,
            e.updatedAt = :now
        WHERE e.templateId = :templateId AND e.appliedVersion < :newVersion
        """)
    int markPendingForTemplate(@Param("templateId") UUID templateId,
                                @Param("newVersion") int newVersion,
                                @Param("now") Instant now);
}
