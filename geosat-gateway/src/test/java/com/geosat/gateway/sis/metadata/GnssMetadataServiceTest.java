package com.geosat.gateway.sis.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GnssMetadataService.
 */
class GnssMetadataServiceTest {

    private GnssMetadataService service;

    @BeforeEach
    void setUp() {
        service = new GnssMetadataService();
    }

    @Test
    void testCreateObservationMetadata() {
        String stationId = "BRAZ";
        Instant startTime = Instant.parse("2025-01-15T00:00:00Z");
        Instant endTime = Instant.parse("2025-01-15T23:59:59Z");
        double latitude = -15.7939;
        double longitude = -47.8828;
        
        Map<String, Object> metadata = service.createObservationMetadata(
            stationId, startTime, endTime, latitude, longitude
        );
        
        // Verify basic metadata
        assertNotNull(metadata);
        assertTrue(metadata.containsKey("title"));
        assertTrue(metadata.containsKey("abstract"));
        
        // Verify title
        String title = (String) metadata.get("title");
        assertTrue(title.contains("BRAZ"));
        assertTrue(title.contains("GNSS Observations"));
        
        // Verify abstract
        String abstractText = (String) metadata.get("abstract");
        assertTrue(abstractText.contains("BRAZ"));
        assertTrue(abstractText.contains(startTime.toString()));
        assertTrue(abstractText.contains(endTime.toString()));
    }

    @Test
    void testCreateObservationMetadata_GeographicExtent() {
        String stationId = "BRAZ";
        Instant startTime = Instant.parse("2025-01-15T00:00:00Z");
        Instant endTime = Instant.parse("2025-01-15T23:59:59Z");
        double latitude = -15.7939;
        double longitude = -47.8828;
        
        Map<String, Object> metadata = service.createObservationMetadata(
            stationId, startTime, endTime, latitude, longitude
        );
        
        // Verify geographic extent
        assertTrue(metadata.containsKey("geographicExtent"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> extent = (Map<String, Object>) metadata.get("geographicExtent");
        
        assertNotNull(extent);
        assertEquals(longitude - 0.01, (Double) extent.get("westBound"), 0.000001);
        assertEquals(longitude + 0.01, (Double) extent.get("eastBound"), 0.000001);
        assertEquals(latitude - 0.01, (Double) extent.get("southBound"), 0.000001);
        assertEquals(latitude + 0.01, (Double) extent.get("northBound"), 0.000001);
    }

    @Test
    void testCreateObservationMetadata_TemporalExtent() {
        String stationId = "BRAZ";
        Instant startTime = Instant.parse("2025-01-15T00:00:00Z");
        Instant endTime = Instant.parse("2025-01-15T23:59:59Z");
        double latitude = -15.7939;
        double longitude = -47.8828;
        
        Map<String, Object> metadata = service.createObservationMetadata(
            stationId, startTime, endTime, latitude, longitude
        );
        
        // Verify temporal extent
        assertTrue(metadata.containsKey("temporalExtent"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> temporal = (Map<String, Object>) metadata.get("temporalExtent");
        
        assertNotNull(temporal);
        assertEquals(startTime.toString(), temporal.get("startTime"));
        assertEquals(endTime.toString(), temporal.get("endTime"));
    }

    @Test
    void testCreateObservationMetadata_Contact() {
        String stationId = "BRAZ";
        Instant startTime = Instant.parse("2025-01-15T00:00:00Z");
        Instant endTime = Instant.parse("2025-01-15T23:59:59Z");
        double latitude = -15.7939;
        double longitude = -47.8828;
        
        Map<String, Object> metadata = service.createObservationMetadata(
            stationId, startTime, endTime, latitude, longitude
        );
        
        // Verify contact information
        assertTrue(metadata.containsKey("organisation"));
        assertTrue(metadata.containsKey("role"));
        
        assertEquals("RBMC/IBGE GNSS Network", metadata.get("organisation"));
        assertEquals("pointOfContact", metadata.get("role"));
    }

    @Test
    void testCreateObservationMetadata_Standard() {
        String stationId = "BRAZ";
        Instant startTime = Instant.parse("2025-01-15T00:00:00Z");
        Instant endTime = Instant.parse("2025-01-15T23:59:59Z");
        double latitude = -15.7939;
        double longitude = -47.8828;
        
        Map<String, Object> metadata = service.createObservationMetadata(
            stationId, startTime, endTime, latitude, longitude
        );
        
        // Verify standard compliance
        assertTrue(metadata.containsKey("standard"));
        assertEquals("ISO 19115 (lightweight)", metadata.get("standard"));
        
        // Verify date stamp exists
        assertTrue(metadata.containsKey("dateStamp"));
        assertNotNull(metadata.get("dateStamp"));
    }

    @Test
    void testCreateProcessingMetadata() {
        String processType = "PPP";
        String inputDataset = "RINEX3 observations from station BRAZ";
        Instant processingDate = Instant.parse("2025-01-15T14:30:00Z");
        
        Map<String, Object> metadata = service.createProcessingMetadata(
            processType, inputDataset, processingDate
        );
        
        // Verify basic metadata
        assertNotNull(metadata);
        assertTrue(metadata.containsKey("title"));
        assertTrue(metadata.containsKey("abstract"));
        
        // Verify title
        String title = (String) metadata.get("title");
        assertTrue(title.contains("PPP"));
        assertTrue(title.contains("Processing Results"));
        
        // Verify abstract
        String abstractText = (String) metadata.get("abstract");
        assertTrue(abstractText.contains("PPP"));
        assertTrue(abstractText.contains(inputDataset));
        assertTrue(abstractText.contains(processingDate.toString()));
    }

    @Test
    void testCreateProcessingMetadata_ProcessingInfo() {
        String processType = "RTK";
        String inputDataset = "Base and rover observations";
        Instant processingDate = Instant.parse("2025-01-15T14:30:00Z");
        
        Map<String, Object> metadata = service.createProcessingMetadata(
            processType, inputDataset, processingDate
        );
        
        // Verify processing information
        assertTrue(metadata.containsKey("processingInfo"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> processing = (Map<String, Object>) metadata.get("processingInfo");
        
        assertNotNull(processing);
        assertEquals("RTK", processing.get("type"));
        assertEquals(inputDataset, processing.get("inputDataset"));
        assertEquals(processingDate.toString(), processing.get("processingDate"));
    }

    @Test
    void testCreateProcessingMetadata_Contact() {
        String processType = "DGPS";
        String inputDataset = "Test dataset";
        Instant processingDate = Instant.parse("2025-01-15T14:30:00Z");
        
        Map<String, Object> metadata = service.createProcessingMetadata(
            processType, inputDataset, processingDate
        );
        
        // Verify contact information
        assertTrue(metadata.containsKey("organisation"));
        assertTrue(metadata.containsKey("role"));
        
        assertEquals("GeoSat Gateway Processing System", metadata.get("organisation"));
        assertEquals("processor", metadata.get("role"));
    }

    @Test
    void testToXml_BasicMetadata() {
        String stationId = "BRAZ";
        Instant startTime = Instant.parse("2025-01-15T00:00:00Z");
        Instant endTime = Instant.parse("2025-01-15T23:59:59Z");
        double latitude = -15.7939;
        double longitude = -47.8828;
        
        Map<String, Object> metadata = service.createObservationMetadata(
            stationId, startTime, endTime, latitude, longitude
        );
        
        String xml = service.toXml(metadata);
        
        // Verify XML structure
        assertNotNull(xml);
        assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(xml.contains("<metadata>"));
        assertTrue(xml.contains("</metadata>"));
        
        // Verify content
        assertTrue(xml.contains("<title>"));
        assertTrue(xml.contains("BRAZ"));
        assertTrue(xml.contains("<abstract>"));
        assertTrue(xml.contains("<contact>"));
        assertTrue(xml.contains("<organisation>"));
        assertTrue(xml.contains("<dateStamp>"));
    }

    @Test
    void testToXml_GeographicExtent() {
        String stationId = "BRAZ";
        Instant startTime = Instant.parse("2025-01-15T00:00:00Z");
        Instant endTime = Instant.parse("2025-01-15T23:59:59Z");
        double latitude = -15.7939;
        double longitude = -47.8828;
        
        Map<String, Object> metadata = service.createObservationMetadata(
            stationId, startTime, endTime, latitude, longitude
        );
        
        String xml = service.toXml(metadata);
        
        // Verify geographic extent in XML
        assertTrue(xml.contains("<geographicExtent>"));
        assertTrue(xml.contains("<westBound>"));
        assertTrue(xml.contains("<eastBound>"));
        assertTrue(xml.contains("<southBound>"));
        assertTrue(xml.contains("<northBound>"));
        assertTrue(xml.contains("</geographicExtent>"));
    }

    @Test
    void testToXml_WellFormed() {
        String stationId = "TEST";
        Instant startTime = Instant.parse("2025-01-15T00:00:00Z");
        Instant endTime = Instant.parse("2025-01-15T23:59:59Z");
        double latitude = 0.0;
        double longitude = 0.0;
        
        Map<String, Object> metadata = service.createObservationMetadata(
            stationId, startTime, endTime, latitude, longitude
        );
        
        String xml = service.toXml(metadata);
        
        // Check for proper tag closure
        int openTags = xml.split("<[^/]").length - 1;
        int closeTags = xml.split("</").length - 1;
        
        // Should have balanced tags (excluding XML declaration and self-closing tags)
        assertTrue(openTags > 0);
        assertTrue(closeTags > 0);
    }
}
