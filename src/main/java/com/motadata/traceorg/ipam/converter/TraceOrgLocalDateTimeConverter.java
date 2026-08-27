package com.motadata.traceorg.ipam.converter;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;


/**
 * IPAM-159 IPAM Roadmap : Streamline IP address request creation and management with the IP Request tool.
 * Convert jpa auditing entity date and time.
 */
@Converter(autoApply = true)
public class TraceOrgLocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime localDateTime) {
        return (localDateTime == null ? null : Timestamp.valueOf(localDateTime));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp timestamp) {
        return (timestamp == null ? null : timestamp.toLocalDateTime());
    }
}
