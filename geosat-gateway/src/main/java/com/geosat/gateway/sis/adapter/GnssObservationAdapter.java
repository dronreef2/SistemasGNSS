package com.geosat.gateway.sis.adapter;

import com.geosat.gateway.model.PosicaoSampleDTO;
import com.geosat.gateway.model.SnrSampleDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter to convert GNSS observations to a lightweight structured format.
 * Provides a bridge between internal DTOs and standardized representations
 * that can be extended to full SIS Feature model in the future.
 */
@Component
public class GnssObservationAdapter {

    /**
     * Converts a PosicaoSampleDTO to a structured map representation.
     * This is a lightweight alternative to full SIS Feature objects,
     * providing the foundation for future Feature integration.
     * 
     * @param sample Position sample
     * @param stationId Station identifier
     * @return Map representing the position observation
     */
    public Map<String, Object> toPositionProperties(PosicaoSampleDTO sample, String stationId) {
        Map<String, Object> properties = new HashMap<>();
        
        // Parse epoch to Instant
        Instant time = Instant.parse(sample.epoch());
        
        // Set properties following ISO 19115 conventions
        properties.put("time", time);
        properties.put("latitude", sample.lat());
        properties.put("longitude", sample.lon());
        properties.put("height", sample.h() != null ? sample.h() : 0.0);
        properties.put("stationId", stationId);
        properties.put("type", "GnssPosition");
        
        return properties;
    }

    /**
     * Converts an SnrSampleDTO to a structured map representation.
     * 
     * @param sample SNR sample
     * @return Map representing the SNR observation
     */
    public Map<String, Object> toSnrProperties(SnrSampleDTO sample) {
        Map<String, Object> properties = new HashMap<>();
        
        // Parse epoch to Instant
        Instant time = Instant.parse(sample.epoch());
        
        // Set properties
        properties.put("time", time);
        properties.put("snr", sample.snr());
        properties.put("satelliteId", sample.sv());
        properties.put("type", "GnssSnr");
        properties.put("unit", "dB-Hz");
        
        return properties;
    }

    /**
     * Validates that a position observation has required fields.
     * 
     * @param sample Position sample
     * @return true if valid, false otherwise
     */
    public boolean isValidPosition(PosicaoSampleDTO sample) {
        if (sample == null) return false;
        if (sample.lat() == null || sample.lon() == null) return false;
        if (sample.epoch() == null || sample.epoch().isEmpty()) return false;
        
        // Validate coordinate ranges
        if (sample.lat() < -90.0 || sample.lat() > 90.0) return false;
        if (sample.lon() < -180.0 || sample.lon() > 180.0) return false;
        
        return true;
    }

    /**
     * Validates that an SNR observation has required fields.
     * 
     * @param sample SNR sample
     * @return true if valid, false otherwise
     */
    public boolean isValidSnr(SnrSampleDTO sample) {
        if (sample == null) return false;
        if (sample.snr() == null) return false;
        if (sample.epoch() == null || sample.epoch().isEmpty()) return false;
        
        // Validate SNR range (typical GNSS range: 0-60 dB-Hz)
        if (sample.snr() < 0.0 || sample.snr() > 100.0) return false;
        
        return true;
    }
}
