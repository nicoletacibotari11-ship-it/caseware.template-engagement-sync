package org.tmplsync.engagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tmplsync.engagement.model.TemplateWatermark;

import java.util.UUID;

@Repository
public interface TemplateWatermarkRepository extends JpaRepository<TemplateWatermark, UUID> {
}
