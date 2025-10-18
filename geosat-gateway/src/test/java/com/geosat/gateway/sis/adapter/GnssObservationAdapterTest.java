package com.geosat.gateway.sis.adapter;

import com.geosat.gateway.model.PosicaoSampleDTO;
import com.geosat.gateway.model.SnrSampleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GnssObservationAdapter.
 */
class GnssObservationAdapterTest {

    private GnssObservationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GnssObservationAdapter();
    }

    @Test
    void testToPositionProperties() {
        // Create a sample position
        String epoch = "2025-01-15T12:30:45Z";
        PosicaoSampleDTO sample = new PosicaoSampleDTO(epoch, -15.7939, -47.8828, 1100.5);
        String stationId = "BRAZ";
        
        Map<String, Object> properties = adapter.toPositionProperties(sample, stationId);
        
        // Verify all expected properties are present
        assertNotNull(properties);
        assertEquals(6, properties.size());
        
        // Verify time property
        assertTrue(properties.get("time") instanceof Instant);
        assertEquals(Instant.parse(epoch), properties.get("time"));
        
        // Verify spatial properties
        assertEquals(-15.7939, properties.get("latitude"));
        assertEquals(-47.8828, properties.get("longitude"));
        assertEquals(1100.5, properties.get("height"));
        
        // Verify metadata properties
        assertEquals("BRAZ", properties.get("stationId"));
        assertEquals("GnssPosition", properties.get("type"));
    }

    @Test
    void testToPositionProperties_NullHeight() {
        // Test handling of null height
        String epoch = "2025-01-15T12:30:45Z";
        PosicaoSampleDTO sample = new PosicaoSampleDTO(epoch, -15.7939, -47.8828, null);
        String stationId = "BRAZ";
        
        Map<String, Object> properties = adapter.toPositionProperties(sample, stationId);
        
        // Height should default to 0.0
        assertEquals(0.0, properties.get("height"));
    }

    @Test
    void testToSnrProperties() {
        // Create a sample SNR observation
        String epoch = "2025-01-15T12:30:45Z";
        String satelliteId = "G01";
        SnrSampleDTO sample = new SnrSampleDTO(epoch, satelliteId, 45.5);
        
        Map<String, Object> properties = adapter.toSnrProperties(sample);
        
        // Verify all expected properties are present
        assertNotNull(properties);
        assertEquals(5, properties.size());
        
        // Verify time property
        assertTrue(properties.get("time") instanceof Instant);
        assertEquals(Instant.parse(epoch), properties.get("time"));
        
        // Verify SNR value
        assertEquals(45.5, properties.get("snr"));
        
        // Verify metadata properties
        assertEquals("G01", properties.get("satelliteId"));
        assertEquals("GnssSnr", properties.get("type"));
        assertEquals("dB-Hz", properties.get("unit"));
    }

    @Test
    void testIsValidPosition_Valid() {
        PosicaoSampleDTO validSample = new PosicaoSampleDTO(
            "2025-01-15T12:30:45Z", 
            -15.7939, 
            -47.8828, 
            1100.5
        );
        
        assertTrue(adapter.isValidPosition(validSample));
    }

    @Test
    void testIsValidPosition_NullSample() {
        assertFalse(adapter.isValidPosition(null));
    }

    @Test
    void testIsValidPosition_NullLatitude() {
        PosicaoSampleDTO sample = new PosicaoSampleDTO(
            "2025-01-15T12:30:45Z", 
            null, 
            -47.8828, 
            1100.5
        );
        
        assertFalse(adapter.isValidPosition(sample));
    }

    @Test
    void testIsValidPosition_NullLongitude() {
        PosicaoSampleDTO sample = new PosicaoSampleDTO(
            "2025-01-15T12:30:45Z", 
            -15.7939, 
            null, 
            1100.5
        );
        
        assertFalse(adapter.isValidPosition(sample));
    }

    @Test
    void testIsValidPosition_NullEpoch() {
        PosicaoSampleDTO sample = new PosicaoSampleDTO(
            null, 
            -15.7939, 
            -47.8828, 
            1100.5
        );
        
        assertFalse(adapter.isValidPosition(sample));
    }

    @Test
    void testIsValidPosition_EmptyEpoch() {
        PosicaoSampleDTO sample = new PosicaoSampleDTO(
            "", 
            -15.7939, 
            -47.8828, 
            1100.5
        );
        
        assertFalse(adapter.isValidPosition(sample));
    }

    @Test
    void testIsValidPosition_InvalidLatitude() {
        // Latitude out of range [-90, 90]
        PosicaoSampleDTO tooHigh = new PosicaoSampleDTO(
            "2025-01-15T12:30:45Z", 
            95.0, 
            -47.8828, 
            1100.5
        );
        assertFalse(adapter.isValidPosition(tooHigh));
        
        PosicaoSampleDTO tooLow = new PosicaoSampleDTO(
            "2025-01-15T12:30:45Z", 
            -95.0, 
            -47.8828, 
            1100.5
        );
        assertFalse(adapter.isValidPosition(tooLow));
    }

    @Test
    void testIsValidPosition_InvalidLongitude() {
        // Longitude out of range [-180, 180]
        PosicaoSampleDTO tooHigh = new PosicaoSampleDTO(
            "2025-01-15T12:30:45Z", 
            -15.7939, 
            185.0, 
            1100.5
        );
        assertFalse(adapter.isValidPosition(tooHigh));
        
        PosicaoSampleDTO tooLow = new PosicaoSampleDTO(
            "2025-01-15T12:30:45Z", 
            -15.7939, 
            -185.0, 
            1100.5
        );
        assertFalse(adapter.isValidPosition(tooLow));
    }

    @Test
    void testIsValidPosition_BoundaryValues() {
        // Test latitude boundaries
        assertTrue(adapter.isValidPosition(
            new PosicaoSampleDTO("2025-01-15T12:30:45Z", 90.0, 0.0, 0.0)
        ));
        assertTrue(adapter.isValidPosition(
            new PosicaoSampleDTO("2025-01-15T12:30:45Z", -90.0, 0.0, 0.0)
        ));
        
        // Test longitude boundaries
        assertTrue(adapter.isValidPosition(
            new PosicaoSampleDTO("2025-01-15T12:30:45Z", 0.0, 180.0, 0.0)
        ));
        assertTrue(adapter.isValidPosition(
            new PosicaoSampleDTO("2025-01-15T12:30:45Z", 0.0, -180.0, 0.0)
        ));
    }

    @Test
    void testIsValidSnr_Valid() {
        SnrSampleDTO validSample = new SnrSampleDTO("2025-01-15T12:30:45Z", "G01", 45.5);
        
        assertTrue(adapter.isValidSnr(validSample));
    }

    @Test
    void testIsValidSnr_NullSample() {
        assertFalse(adapter.isValidSnr(null));
    }

    @Test
    void testIsValidSnr_NullSnrValue() {
        SnrSampleDTO sample = new SnrSampleDTO("2025-01-15T12:30:45Z", "G01", null);
        
        assertFalse(adapter.isValidSnr(sample));
    }

    @Test
    void testIsValidSnr_NullEpoch() {
        SnrSampleDTO sample = new SnrSampleDTO(null, "G01", 45.5);
        
        assertFalse(adapter.isValidSnr(sample));
    }

    @Test
    void testIsValidSnr_EmptyEpoch() {
        SnrSampleDTO sample = new SnrSampleDTO("", "G01", 45.5);
        
        assertFalse(adapter.isValidSnr(sample));
    }

    @Test
    void testIsValidSnr_InvalidRange() {
        // SNR should be in range [0, 100]
        SnrSampleDTO negative = new SnrSampleDTO("2025-01-15T12:30:45Z", "G01", -5.0);
        assertFalse(adapter.isValidSnr(negative));
        
        SnrSampleDTO tooHigh = new SnrSampleDTO("2025-01-15T12:30:45Z", "G01", 105.0);
        assertFalse(adapter.isValidSnr(tooHigh));
    }

    @Test
    void testIsValidSnr_BoundaryValues() {
        // Test SNR boundaries
        assertTrue(adapter.isValidSnr(
            new SnrSampleDTO("2025-01-15T12:30:45Z", "G01", 0.0)
        ));
        assertTrue(adapter.isValidSnr(
            new SnrSampleDTO("2025-01-15T12:30:45Z", "G01", 100.0)
        ));
        
        // Typical GNSS SNR values
        assertTrue(adapter.isValidSnr(
            new SnrSampleDTO("2025-01-15T12:30:45Z", "G01", 25.0)
        ));
        assertTrue(adapter.isValidSnr(
            new SnrSampleDTO("2025-01-15T12:30:45Z", "G01", 55.0)
        ));
    }
}
