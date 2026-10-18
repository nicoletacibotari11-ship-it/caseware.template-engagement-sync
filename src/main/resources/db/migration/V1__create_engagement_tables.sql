CREATE TABLE template_watermarks (
    template_id    UUID PRIMARY KEY,
    latest_version INT NOT NULL CHECK (latest_version > 0),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE engagement_versions (
    engagement_id        UUID PRIMARY KEY,
    firm_id              UUID NOT NULL,
    template_id          UUID NOT NULL,
    applied_version       INT NOT NULL,
    latest_known_version  INT NOT NULL,
    status               VARCHAR(16) NOT NULL CHECK (status IN ('current','pending','declined')),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (applied_version <= latest_known_version)
);

CREATE INDEX idx_engagement_versions_firm_status ON engagement_versions (firm_id, status);
CREATE INDEX idx_engagement_versions_template ON engagement_versions (template_id);
