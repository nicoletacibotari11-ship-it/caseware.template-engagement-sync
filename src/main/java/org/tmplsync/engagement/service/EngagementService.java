package org.tmplsync.engagement.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tmplsync.engagement.api.dto.request.DecisionType;
import org.tmplsync.engagement.api.dto.request.EngagementCreatedRequest;
import org.tmplsync.engagement.api.dto.request.EngagementDecidedRequest;
import org.tmplsync.engagement.api.dto.response.EngagementResponse;
import org.tmplsync.engagement.api.dto.response.PendingEngagementResponse;
import org.tmplsync.engagement.exception.EngagementNotFoundException;
import org.tmplsync.engagement.model.enums.EngagementStatus;
import org.tmplsync.engagement.model.EngagementVersion;
import org.tmplsync.engagement.model.TemplateWatermark;
import org.tmplsync.engagement.repository.EngagementVersionRepository;
import org.tmplsync.engagement.repository.TemplateWatermarkRepository;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class EngagementService {

    private EngagementVersionRepository engagementVersionRepository;
    private TemplateWatermarkRepository templateWatermarkRepository;

    @Transactional
    public EngagementResponse recordEngagementCreated(UUID engagementId, EngagementCreatedRequest request) {
        EngagementVersion existing = engagementVersionRepository.findById(engagementId).orElse(null);
        if (existing != null) {
            log.info("Engagement {} already recorded, skipping duplicate create (at-least-once retry)", engagementId);
            return EngagementResponse.from(existing);
        }

        int latestKnownVersion = templateWatermarkRepository.findById(request.templateId())
                .map(TemplateWatermark::getLatestVersion)
                .orElse(request.appliedVersion());

        EngagementStatus status = request.appliedVersion() >= latestKnownVersion
                ? EngagementStatus.CURRENT
                : EngagementStatus.PENDING;

        EngagementVersion engagementVersion = new EngagementVersion(
                engagementId,
                request.firmId(),
                request.templateId(),
                request.appliedVersion(),
                latestKnownVersion,
                status,
                Instant.now()
        );
        engagementVersionRepository.saveAndFlush(engagementVersion);
        log.info("Recorded engagement {} for template {} with status {}", engagementId, request.templateId(), status);
        return EngagementResponse.from(engagementVersion);
    }

    @Transactional
    public EngagementResponse recordEngagementDecided(UUID engagementId, EngagementDecidedRequest request) {
        EngagementVersion engagementVersion = engagementVersionRepository.findById(engagementId)
                .orElseThrow(() -> {
                    log.warn("Decision {} received for unknown engagement {}", request.decision(), engagementId);
                    return new EngagementNotFoundException(engagementId);
                });

        if (request.decision() == DecisionType.APPLY) {
            int decidedVersion = request.decidedVersion() != null
                    ? request.decidedVersion()
                    : engagementVersion.getLatestKnownVersion();
            engagementVersion.setAppliedVersion(decidedVersion);
            engagementVersion.setStatus(EngagementStatus.CURRENT);
        } else {
            engagementVersion.setStatus(EngagementStatus.DECLINED);
        }
        engagementVersion.setUpdatedAt(Instant.now());
        engagementVersionRepository.saveAndFlush(engagementVersion);
        log.info("Engagement {} decision {} applied, status now {}",
                engagementId, request.decision(), engagementVersion.getStatus());
        return EngagementResponse.from(engagementVersion);
    }

    @Transactional(readOnly = true)
    public Page<PendingEngagementResponse> findPendingByFirm(UUID firmId, Pageable pageable) {
        return engagementVersionRepository.findByFirmIdAndStatus(firmId, EngagementStatus.PENDING, pageable)
                .map(e -> new PendingEngagementResponse(
                        e.getEngagementId(),
                        e.getTemplateId(),
                        e.getAppliedVersion(),
                        e.getLatestKnownVersion()));
    }

    @Transactional
    public int markPendingForTemplate(UUID templateId, int newVersion) {
        int updated = engagementVersionRepository.markPendingForTemplate(templateId, newVersion, Instant.now());
        log.info("Marked {} engagement(s) pending for template {} at version {}", updated, templateId, newVersion);
        return updated;
    }
}
