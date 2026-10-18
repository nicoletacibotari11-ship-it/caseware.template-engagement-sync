package org.tmplsync.engagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EngagementServiceTest {

    @Mock
    private EngagementVersionRepository engagementVersionRepository;

    @Mock
    private TemplateWatermarkRepository templateWatermarkRepository;

    private EngagementService service() {
        return new EngagementService(engagementVersionRepository, templateWatermarkRepository);
    }

    @Test
    void createsEngagementAsCurrentWhenNoWatermarkExists() {
        UUID engagementId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        when(engagementVersionRepository.findById(engagementId)).thenReturn(Optional.empty());
        when(templateWatermarkRepository.findById(templateId)).thenReturn(Optional.empty());

        EngagementResponse result = service()
                .recordEngagementCreated(engagementId, new EngagementCreatedRequest(firmId, templateId, 1));

        ArgumentCaptor<EngagementVersion> captor = ArgumentCaptor.forClass(EngagementVersion.class);
        verify(engagementVersionRepository).saveAndFlush(captor.capture());
        EngagementVersion saved = captor.getValue();
        assertEquals(EngagementStatus.CURRENT, saved.getStatus());
        assertEquals(1, saved.getAppliedVersion());
        assertEquals(1, saved.getLatestKnownVersion());
        assertEquals(EngagementResponse.from(saved), result);
    }

    @Test
    void createsEngagementAsPendingWhenWatermarkIsNewer() {
        UUID engagementId = UUID.randomUUID();
        UUID firmId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        when(engagementVersionRepository.findById(engagementId)).thenReturn(Optional.empty());
        when(templateWatermarkRepository.findById(templateId))
                .thenReturn(Optional.of(new TemplateWatermark(templateId, 3, Instant.now())));

        service().recordEngagementCreated(engagementId, new EngagementCreatedRequest(firmId, templateId, 1));

        ArgumentCaptor<EngagementVersion> captor = ArgumentCaptor.forClass(EngagementVersion.class);
        verify(engagementVersionRepository).saveAndFlush(captor.capture());
        EngagementVersion saved = captor.getValue();
        assertEquals(EngagementStatus.PENDING, saved.getStatus());
        assertEquals(1, saved.getAppliedVersion());
        assertEquals(3, saved.getLatestKnownVersion());
    }

    @Test
    void doesNotOverwriteExistingEngagementOnRetry() {
        UUID engagementId = UUID.randomUUID();
        EngagementVersion existing = new EngagementVersion(
                engagementId, UUID.randomUUID(), UUID.randomUUID(), 1, 1, EngagementStatus.CURRENT, Instant.now());
        when(engagementVersionRepository.findById(engagementId)).thenReturn(Optional.of(existing));

        EngagementResponse result = service().recordEngagementCreated(
                engagementId, new EngagementCreatedRequest(UUID.randomUUID(), UUID.randomUUID(), 1));

        verify(engagementVersionRepository, never()).saveAndFlush(any());
        assertEquals(EngagementResponse.from(existing), result);
    }

    @Test
    void applyingADecisionUpdatesAppliedVersionAndSetsCurrent() {
        UUID engagementId = UUID.randomUUID();
        EngagementVersion existing = new EngagementVersion(
                engagementId, UUID.randomUUID(), UUID.randomUUID(), 1, 3, EngagementStatus.PENDING, Instant.now());
        when(engagementVersionRepository.findById(engagementId)).thenReturn(Optional.of(existing));

        EngagementResponse result = service()
                .recordEngagementDecided(engagementId, new EngagementDecidedRequest(DecisionType.APPLY, 3));

        assertEquals(EngagementStatus.CURRENT, existing.getStatus());
        assertEquals(3, existing.getAppliedVersion());
        assertEquals(EngagementResponse.from(existing), result);
        verify(engagementVersionRepository).saveAndFlush(existing);
    }

    @Test
    void decliningADecisionSetsDeclinedWithoutChangingAppliedVersion() {
        UUID engagementId = UUID.randomUUID();
        EngagementVersion existing = new EngagementVersion(
                engagementId, UUID.randomUUID(), UUID.randomUUID(), 1, 3, EngagementStatus.PENDING, Instant.now());
        when(engagementVersionRepository.findById(engagementId)).thenReturn(Optional.of(existing));

        service().recordEngagementDecided(engagementId, new EngagementDecidedRequest(DecisionType.DECLINE, null));

        assertEquals(EngagementStatus.DECLINED, existing.getStatus());
        assertEquals(1, existing.getAppliedVersion());
    }

    @Test
    void decidingUnknownEngagementThrows() {
        UUID engagementId = UUID.randomUUID();
        when(engagementVersionRepository.findById(engagementId)).thenReturn(Optional.empty());

        assertThrows(EngagementNotFoundException.class, () -> service()
                .recordEngagementDecided(engagementId, new EngagementDecidedRequest(DecisionType.APPLY, 2)));
    }

    @Test
    void findsPendingEngagementsForFirm() {
        UUID firmId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        EngagementVersion pending = new EngagementVersion(
                UUID.randomUUID(), firmId, templateId, 1, 2, EngagementStatus.PENDING, Instant.now());
        Pageable pageable = PageRequest.of(0, 20);
        when(engagementVersionRepository.findByFirmIdAndStatus(firmId, EngagementStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(pending), pageable, 1));

        var result = service().findPendingByFirm(firmId, pageable);

        assertEquals(1, result.getTotalElements());
        PendingEngagementResponse first = result.getContent().get(0);
        assertEquals(pending.getEngagementId(), first.engagementId());
        assertEquals(templateId, first.templateId());
    }

    @Test
    void marksPendingForTemplateDelegatesToRepository() {
        UUID templateId = UUID.randomUUID();
        when(engagementVersionRepository.markPendingForTemplate(eq(templateId), eq(4), any())).thenReturn(2);

        int updated = service().markPendingForTemplate(templateId, 4);

        assertEquals(2, updated);
    }
}
