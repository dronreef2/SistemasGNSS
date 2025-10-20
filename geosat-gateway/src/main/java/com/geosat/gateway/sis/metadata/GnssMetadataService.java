package com.geosat.gateway.sis.metadata;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for creating ISO 19115-inspired metadata for GNSS datasets.
 * Provides standardized metadata generation for GNSS observations and processing results.
 * This is a lightweight implementation that follows ISO 19115 principles.
 */
@Service
public class GnssMetadataService {

    /**
     * Creates metadata for a GNSS observation dataset.
     * 
     * @param stationId Station identifier
     * @param startTime Start of observation period
     * @param endTime End of observation period
     * @param latitude Station latitude
     * @param longitude Station longitude
     * @return Metadata map following ISO 19115 structure
     */
    public Map<String, Object> createObservationMetadata(
            String stationId,
            Instant startTime,
            Instant endTime,
            double latitude,
            double longitude) {
        
        Map<String, Object> metadata = new HashMap<>();
        
        // Identification
        metadata.put("title", String.format("GNSS Observations - Station %s", stationId));
        metadata.put("abstract", String.format(
            "GNSS observation data from station %s for period %s to %s. " +
            "Data includes position solutions, signal quality measurements, and derived metrics.",
            stationId, startTime, endTime));
        
        // Geographic extent
        Map<String, Object> extent = new HashMap<>();
        extent.put("westBound", longitude - 0.01);
        extent.put("eastBound", longitude + 0.01);
        extent.put("southBound", latitude - 0.01);
        extent.put("northBound", latitude + 0.01);
        metadata.put("geographicExtent", extent);
        
        // Temporal extent
        Map<String, Object> temporal = new HashMap<>();
        temporal.put("startTime", startTime.toString());
        temporal.put("endTime", endTime.toString());
        metadata.put("temporalExtent", temporal);
        
        // Contact
        metadata.put("organisation", "RBMC/IBGE GNSS Network");
        metadata.put("role", "pointOfContact");
        
        // Date stamp
        metadata.put("dateStamp", Instant.now().toString());
        metadata.put("standard", "ISO 19115 (lightweight)");
        
        return metadata;
    }

    /**
     * Creates metadata for a GNSS processing result.
     * 
     * @param processType Type of processing (e.g., "PPP", "RTK", "DGPS")
     * @param inputDataset Description of input data
     * @param processingDate Date of processing
     * @return Metadata map following ISO 19115 structure
     */
    public Map<String, Object> createProcessingMetadata(
            String processType,
            String inputDataset,
            Instant processingDate) {
        
        Map<String, Object> metadata = new HashMap<>();
        
        // Identification
        metadata.put("title", String.format("GNSS %s Processing Results", processType));
        metadata.put("abstract", String.format(
            "Results from %s processing of GNSS observations. " +
            "Input dataset: %s. Processing date: %s.",
            processType, inputDataset, processingDate));
        
        // Processing information
        Map<String, Object> processing = new HashMap<>();
        processing.put("type", processType);
        processing.put("inputDataset", inputDataset);
        processing.put("processingDate", processingDate.toString());
        metadata.put("processingInfo", processing);
        
        // Contact
        metadata.put("organisation", "GeoSat Gateway Processing System");
        metadata.put("role", "processor");
        
        // Date stamp
        metadata.put("dateStamp", processingDate.toString());
        metadata.put("standard", "ISO 19115 (lightweight)");
        
        return metadata;
    }

    /**
     * Converts metadata to XML string (ISO 19115 format).
     * 
     * @param metadata Metadata map
     * @return XML representation
     */
    public String toXml(Map<String, Object> metadata) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<metadata>\n");
        
        if (metadata.containsKey("title")) {
            xml.append("  <title>").append(metadata.get("title")).append("</title>\n");
        }
        
        if (metadata.containsKey("abstract")) {
            xml.append("  <abstract>").append(metadata.get("abstract")).append("</abstract>\n");
        }
        
        if (metadata.containsKey("organisation")) {
            xml.append("  <contact>\n");
            xml.append("    <organisation>").append(metadata.get("organisation")).append("</organisation>\n");
            xml.append("    <role>").append(metadata.get("role")).append("</role>\n");
            xml.append("  </contact>\n");
        }
        
        if (metadata.containsKey("geographicExtent")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> extent = (Map<String, Object>) metadata.get("geographicExtent");
            xml.append("  <geographicExtent>\n");
            xml.append("    <westBound>").append(extent.get("westBound")).append("</westBound>\n");
            xml.append("    <eastBound>").append(extent.get("eastBound")).append("</eastBound>\n");
            xml.append("    <southBound>").append(extent.get("southBound")).append("</southBound>\n");
            xml.append("    <northBound>").append(extent.get("northBound")).append("</northBound>\n");
            xml.append("  </geographicExtent>\n");
        }
        
        if (metadata.containsKey("dateStamp")) {
            xml.append("  <dateStamp>").append(metadata.get("dateStamp")).append("</dateStamp>\n");
        }
        
        xml.append("</metadata>");
        
        return xml.toString();
    }
}
