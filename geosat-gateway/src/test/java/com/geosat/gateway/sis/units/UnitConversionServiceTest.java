package com.geosat.gateway.sis.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UnitConversionService.
 */
class UnitConversionServiceTest {

    private UnitConversionService service;

    @BeforeEach
    void setUp() {
        service = new UnitConversionService();
    }

    @Test
    void testDegreesToRadians() {
        // Test standard angles
        assertEquals(0.0, service.degreesToRadians(0.0), 0.000001);
        assertEquals(Math.PI / 2, service.degreesToRadians(90.0), 0.000001);
        assertEquals(Math.PI, service.degreesToRadians(180.0), 0.000001);
        assertEquals(2 * Math.PI, service.degreesToRadians(360.0), 0.000001);
        
        // Test negative angles
        assertEquals(-Math.PI / 2, service.degreesToRadians(-90.0), 0.000001);
        assertEquals(-Math.PI, service.degreesToRadians(-180.0), 0.000001);
    }

    @Test
    void testRadiansToDegrees() {
        // Test standard angles
        assertEquals(0.0, service.radiansToDegrees(0.0), 0.000001);
        assertEquals(90.0, service.radiansToDegrees(Math.PI / 2), 0.000001);
        assertEquals(180.0, service.radiansToDegrees(Math.PI), 0.000001);
        assertEquals(360.0, service.radiansToDegrees(2 * Math.PI), 0.000001);
        
        // Test negative angles
        assertEquals(-90.0, service.radiansToDegrees(-Math.PI / 2), 0.000001);
        assertEquals(-180.0, service.radiansToDegrees(-Math.PI), 0.000001);
    }

    @Test
    void testAngleConversionRoundTrip() {
        // Test that converting back and forth preserves value
        double originalDegrees = 45.5;
        double radians = service.degreesToRadians(originalDegrees);
        double backToDegrees = service.radiansToDegrees(radians);
        
        assertEquals(originalDegrees, backToDegrees, 0.000001);
    }

    @Test
    void testMetersToKilometers() {
        assertEquals(0.0, service.metersToKilometers(0.0), 0.000001);
        assertEquals(1.0, service.metersToKilometers(1000.0), 0.000001);
        assertEquals(5.5, service.metersToKilometers(5500.0), 0.000001);
        assertEquals(0.001, service.metersToKilometers(1.0), 0.000001);
    }

    @Test
    void testKilometersToMeters() {
        assertEquals(0.0, service.kilometersToMeters(0.0), 0.000001);
        assertEquals(1000.0, service.kilometersToMeters(1.0), 0.000001);
        assertEquals(5500.0, service.kilometersToMeters(5.5), 0.000001);
        assertEquals(1.0, service.kilometersToMeters(0.001), 0.000001);
    }

    @Test
    void testLengthConversionRoundTrip() {
        // Test that converting back and forth preserves value
        double originalMeters = 12345.67;
        double kilometers = service.metersToKilometers(originalMeters);
        double backToMeters = service.kilometersToMeters(kilometers);
        
        assertEquals(originalMeters, backToMeters, 0.001);
    }

    @Test
    void testConvertLength_MetersToKilometers() {
        double result = service.convertLength(1000.0, "m", "km");
        assertEquals(1.0, result, 0.000001);
    }

    @Test
    void testConvertLength_KilometersToMeters() {
        double result = service.convertLength(5.5, "km", "m");
        assertEquals(5500.0, result, 0.000001);
    }

    @Test
    void testConvertLength_SameUnit() {
        // Converting to same unit should return same value
        double result = service.convertLength(100.0, "m", "m");
        assertEquals(100.0, result, 0.000001);
    }

    @Test
    void testConvertLength_CaseInsensitive() {
        // Test that unit parsing is case-insensitive
        double result1 = service.convertLength(1000.0, "M", "KM");
        assertEquals(1.0, result1, 0.000001);
        
        double result2 = service.convertLength(1.0, "Kilometer", "Meter");
        assertEquals(1000.0, result2, 0.000001);
    }

    @Test
    void testFormatLength() {
        assertEquals("1234.56 m", service.formatLength(1234.56, "m"));
        assertEquals("5.50 km", service.formatLength(5.5, "km"));
        assertEquals("0.00 m", service.formatLength(0.0, "m"));
        assertEquals("123.45 km", service.formatLength(123.45, "km"));
    }

    @Test
    void testFormatLength_Rounding() {
        // Test that formatting rounds to 2 decimal places
        assertEquals("1234.57 m", service.formatLength(1234.5678, "m"));
        assertEquals("1234.56 m", service.formatLength(1234.5634, "m"));
    }

    @Test
    void testConversionPrecision() {
        // Test that conversions maintain high precision
        double meters = 12345.6789;
        double km = service.metersToKilometers(meters);
        double backToMeters = service.kilometersToMeters(km);
        
        // Should be accurate to at least 6 decimal places
        assertEquals(meters, backToMeters, 0.000001);
    }

    @Test
    void testGnssTypicalDistances() {
        // Test conversions with typical GNSS distances
        
        // Satellite altitude (~20,200 km)
        double satelliteAltKm = 20200.0;
        double satelliteAltM = service.kilometersToMeters(satelliteAltKm);
        assertEquals(20200000.0, satelliteAltM, 1.0);
        
        // Baseline length (100 m)
        double baselineM = 100.0;
        double baselineKm = service.metersToKilometers(baselineM);
        assertEquals(0.1, baselineKm, 0.000001);
        
        // Earth radius (~6371 km)
        double earthRadiusKm = 6371.0;
        double earthRadiusM = service.kilometersToMeters(earthRadiusKm);
        assertEquals(6371000.0, earthRadiusM, 1.0);
    }
}
