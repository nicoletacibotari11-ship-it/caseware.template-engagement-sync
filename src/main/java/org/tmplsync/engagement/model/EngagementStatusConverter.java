package org.tmplsync.engagement.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.tmplsync.engagement.model.enums.EngagementStatus;

import java.util.Locale;

@Converter(autoApply = true)
public class EngagementStatusConverter implements AttributeConverter<EngagementStatus, String> {

    @Override
    public String convertToDatabaseColumn(EngagementStatus attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public EngagementStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EngagementStatus.valueOf(dbData.toUpperCase(Locale.ROOT));
    }
}
