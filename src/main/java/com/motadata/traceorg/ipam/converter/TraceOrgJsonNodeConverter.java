package com.motadata.traceorg.ipam.converter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * IPAM-160 IPAM Roadmap : The solution must be flexible to allow the creation of custom fields for objects in IPAM. This must be configurable via the Web GUI.
 * Store custom columns as json.
 */
@Converter(autoApply = true)
public class TraceOrgJsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgJsonNodeConverter.class, "json converter");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode attribute) {
        try
        {
            return (attribute == null || attribute.isNull()) ? null : objectMapper.writeValueAsString(attribute);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
            throw new RuntimeException("JSON writing error: " + exception.getMessage(), exception);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String dbData) {
        try
        {
            return (dbData == null || dbData.trim().isEmpty()) ? MissingNode.getInstance() : objectMapper.readTree(dbData);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
            throw new RuntimeException("JSON reading error: " + exception.getMessage(), exception);
        }
    }
}