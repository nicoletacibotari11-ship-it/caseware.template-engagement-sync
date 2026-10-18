package org.tmplsync.engagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "template_watermarks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateWatermark {

    @Id
    @Column(name = "template_id",  updatable = false, nullable = false)
    private UUID templateId;

    @Column(name = "latest_version", nullable = false)
    private int latestVersion;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
