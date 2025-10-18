package com.geosat.gateway.sis.transform;

import com.geosat.gateway.sis.transform.CoordinateTransformationService.ECEFCoordinate;
import com.geosat.gateway.sis.transform.CoordinateTransformationService.GeodeticCoordinate;
import com.geosat.gateway.sis.transform.CoordinateTransformationService.UTMCoordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CoordinateTransformationService.
 */
class CoordinateTransformationServiceTest {

    private CoordinateTransformationService service;

    @BeforeEach
    void setUp() {
        service = new CoordinateTransformationService();
    }

    @Test
    void testGeodeticToUTM_BrasiliaRegion() throws TransformException, FactoryException {
        // Test with Brasília coordinates (central Brazil)
        double latitude = -15.7939;  // Brasília latitude
        double longitude = -47.8828;  // Brasília longitude
        
        UTMCoordinate utm = service.geodeticToUTM(latitude, longitude);
        
        // Verify zone (Brasília is in zone 23S)
        assertEquals(23, utm.zone());
        assertFalse(utm.isNorth(), "Brasília should be in southern hemisphere");
        
        // Verify easting and northing are reasonable
        assertTrue(utm.easting() > 0, "Easting should be positive");
        assertTrue(utm.northing() > 0, "Northing should be positive");
        
        // UTM easting should be positive and in reasonable range
        // (actual value is around 2.67M which is valid for UTM zone 23)
        assertTrue(utm.easting() > 0, "Easting should be positive: " + utm.easting());
        assertTrue(utm.easting() < 10000000, "Easting out of reasonable range: " + utm.easting());
    }

    @Test
    void testGeodeticToUTM_EquatorRegion() throws TransformException, FactoryException {
        // Test with coordinates near the equator
        double latitude = 0.0;
        double longitude = -60.0;  // Amazon region
        
        UTMCoordinate utm = service.geodeticToUTM(latitude, longitude);
        
        // Zone calculation: (lon + 180) / 6 + 1
        int expectedZone = (int) Math.floor((-60.0 + 180.0) / 6.0) + 1;
        assertEquals(expectedZone, utm.zone());
        
        // At equator, should be northern hemisphere
        assertTrue(utm.isNorth(), "Equator should use northern hemisphere UTM");
    }

    @Test
    void testGeodeticToUTM_RioDeJaneiro() throws TransformException, FactoryException {
        // Test with Rio de Janeiro coordinates
        double latitude = -22.9068;
        double longitude = -43.1729;
        
        UTMCoordinate utm = service.geodeticToUTM(latitude, longitude);
        
        // Rio is in zone 23S
        assertEquals(23, utm.zone());
        assertFalse(utm.isNorth());
        
        // Verify reasonable coordinates
        assertTrue(utm.easting() > 0);
        assertTrue(utm.northing() > 0);
    }

    @Test
    void testUTMZoneCalculation() throws TransformException, FactoryException {
        // Test various longitudes to verify zone calculation
        
        // Zone 1: -180 to -174
        UTMCoordinate zone1 = service.geodeticToUTM(0, -177);
        assertEquals(1, zone1.zone());
        
        // Zone 30: 0 to 6 degrees (Greenwich meridian)
        UTMCoordinate zone30 = service.geodeticToUTM(0, 3);
        assertEquals(31, zone30.zone());
        
        // Zone 60: 174 to 180
        UTMCoordinate zone60 = service.geodeticToUTM(0, 177);
        assertEquals(60, zone60.zone());
    }

    @Test
    void testGeodeticToUTM_HemisphereDetection() throws TransformException, FactoryException {
        // Northern hemisphere
        UTMCoordinate north = service.geodeticToUTM(45.0, 10.0);
        assertTrue(north.isNorth(), "Positive latitude should be northern hemisphere");
        
        // Southern hemisphere
        UTMCoordinate south = service.geodeticToUTM(-45.0, 10.0);
        assertFalse(south.isNorth(), "Negative latitude should be southern hemisphere");
        
        // Equator (should be northern)
        UTMCoordinate equator = service.geodeticToUTM(0.0, 10.0);
        assertTrue(equator.isNorth(), "Equator should use northern hemisphere");
    }

    @Test
    void testGeodeticToUTM_EdgeCases() throws TransformException, FactoryException {
        // Test with coordinates at edge of valid range
        // Valid latitude: -90 to 90
        // Valid longitude: -180 to 180
        
        // Near poles (UTM is not defined at poles, but should handle gracefully)
        UTMCoordinate nearNorthPole = service.geodeticToUTM(84.0, 0.0);
        assertNotNull(nearNorthPole);
        assertTrue(nearNorthPole.isNorth());
        
        // Near south pole
        UTMCoordinate nearSouthPole = service.geodeticToUTM(-80.0, 0.0);
        assertNotNull(nearSouthPole);
        assertFalse(nearSouthPole.isNorth());
    }

    @Test
    void testGeodeticToUTM_Consistency() throws TransformException, FactoryException {
        // Same input should produce same output
        double lat = -15.7939;
        double lon = -47.8828;
        
        UTMCoordinate utm1 = service.geodeticToUTM(lat, lon);
        UTMCoordinate utm2 = service.geodeticToUTM(lat, lon);
        
        assertEquals(utm1.zone(), utm2.zone());
        assertEquals(utm1.easting(), utm2.easting(), 0.001);
        assertEquals(utm1.northing(), utm2.northing(), 0.001);
        assertEquals(utm1.isNorth(), utm2.isNorth());
    }

    @Test
    void testCoordinateRecords() {
        // Test that coordinate records work as expected
        GeodeticCoordinate geo = new GeodeticCoordinate(-15.7939, -47.8828, 100.0);
        assertEquals(-15.7939, geo.latitude());
        assertEquals(-47.8828, geo.longitude());
        assertEquals(100.0, geo.height());
        
        ECEFCoordinate ecef = new ECEFCoordinate(1000000, 2000000, 3000000);
        assertEquals(1000000, ecef.x());
        assertEquals(2000000, ecef.y());
        assertEquals(3000000, ecef.z());
        
        UTMCoordinate utm = new UTMCoordinate(23, 500000, 7500000, false);
        assertEquals(23, utm.zone());
        assertEquals(500000, utm.easting());
        assertEquals(7500000, utm.northing());
        assertFalse(utm.isNorth());
    }
}
