package org.tmplsync.engagement.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tmplsync.engagement.api.dto.request.EngagementCreatedRequest;
import org.tmplsync.engagement.api.dto.request.EngagementDecidedRequest;
import org.tmplsync.engagement.api.dto.response.EngagementResponse;
import org.tmplsync.engagement.api.dto.response.PendingEngagementResponse;
import org.tmplsync.engagement.service.EngagementService;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/engagements")
@RequiredArgsConstructor
public class EngagementController {

    private final EngagementService engagementService;

    @PostMapping("/{engagementId}")
    public ResponseEntity<EngagementResponse> engagementCreated(@PathVariable UUID engagementId,
                                                                 @Valid @RequestBody EngagementCreatedRequest request) {
        EngagementResponse response = engagementService.recordEngagementCreated(engagementId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{engagementId}/decision")
    public ResponseEntity<EngagementResponse> engagementDecided(@PathVariable UUID engagementId,
                                                                 @Valid @RequestBody EngagementDecidedRequest request) {
        EngagementResponse response = engagementService.recordEngagementDecided(engagementId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<PendingEngagementResponse>> pendingEngagements(
            @RequestParam("firmId") UUID firmId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(engagementService.findPendingByFirm(firmId, pageable));
    }
}
