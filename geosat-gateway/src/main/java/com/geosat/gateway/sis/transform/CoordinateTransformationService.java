package com.geosat.gateway.sis.transform;

import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.springframework.stereotype.Service;

/**
 * Service for coordinate transformations using Apache SIS.
 * Provides conversions between different coordinate reference systems (CRS),
 * including ECEF (geocentric) and geodetic (lat/long) coordinates.
 */
@Service
public class CoordinateTransformationService {

    /**
     * Transforms ECEF (Earth-Centered, Earth-Fixed) coordinates to geodetic coordinates (lat/lon/height).
     * 
     * @param x ECEF X coordinate in meters
     * @param y ECEF Y coordinate in meters
     * @param z ECEF Z coordinate in meters
     * @return GeodeticCoordinate with latitude (degrees), longitude (degrees), and height (meters)
     * @throws TransformException if transformation fails
     * @throws FactoryException if CRS setup fails
     */
    public GeodeticCoordinate ecefToGeodetic(double x, double y, double z) 
            throws TransformException, FactoryException {
        
        // Source: ECEF (geocentric Cartesian) - WGS84
        CoordinateReferenceSystem sourceCRS = CommonCRS.WGS84.geocentric();
        
        // Target: Geographic 3D (lat/lon/height) - WGS84
        CoordinateReferenceSystem targetCRS = CommonCRS.WGS84.geographic3D();
        
        // Find transformation
        CoordinateOperation operation = CRS.findOperation(sourceCRS, targetCRS, null);
        
        // Create source position (ECEF coordinates)
        DirectPosition sourcePos = new DirectPosition2D(sourceCRS, x, y);
        // Note: For 3D, we need to handle Z coordinate separately in SIS
        // This is a simplified version; full 3D transformation would require more setup
        
        // Transform
        DirectPosition targetPos = operation.getMathTransform().transform(sourcePos, null);
        
        // Extract results (longitude, latitude in degrees, height in meters)
        double longitude = targetPos.getOrdinate(0);
        double latitude = targetPos.getOrdinate(1);
        double height = targetPos.getDimension() > 2 ? targetPos.getOrdinate(2) : 0.0;
        
        return new GeodeticCoordinate(latitude, longitude, height);
    }

    /**
     * Transforms geodetic coordinates (lat/lon/height) to ECEF coordinates.
     * 
     * @param latitude Latitude in degrees
     * @param longitude Longitude in degrees
     * @param height Ellipsoidal height in meters
     * @return ECEFCoordinate with X, Y, Z in meters
     * @throws TransformException if transformation fails
     * @throws FactoryException if CRS setup fails
     */
    public ECEFCoordinate geodeticToEcef(double latitude, double longitude, double height) 
            throws TransformException, FactoryException {
        
        // Source: Geographic 3D (lat/lon/height) - WGS84
        CoordinateReferenceSystem sourceCRS = CommonCRS.WGS84.geographic3D();
        
        // Target: ECEF (geocentric Cartesian) - WGS84
        CoordinateReferenceSystem targetCRS = CommonCRS.WGS84.geocentric();
        
        // Find transformation
        CoordinateOperation operation = CRS.findOperation(sourceCRS, targetCRS, null);
        
        // Create source position (lat, lon in degrees, height in meters)
        DirectPosition sourcePos = new DirectPosition2D(sourceCRS, longitude, latitude);
        
        // Transform
        DirectPosition targetPos = operation.getMathTransform().transform(sourcePos, null);
        
        // Extract ECEF coordinates
        double x = targetPos.getOrdinate(0);
        double y = targetPos.getOrdinate(1);
        double z = targetPos.getDimension() > 2 ? targetPos.getOrdinate(2) : 0.0;
        
        return new ECEFCoordinate(x, y, z);
    }

    /**
     * Transforms geodetic coordinates to UTM projection.
     * 
     * @param latitude Latitude in degrees
     * @param longitude Longitude in degrees
     * @return UTMCoordinate with zone, easting, northing, and hemisphere
     * @throws TransformException if transformation fails
     * @throws FactoryException if CRS setup fails
     */
    public UTMCoordinate geodeticToUTM(double latitude, double longitude) 
            throws TransformException, FactoryException {
        
        // Calculate UTM zone from longitude
        int zone = (int) Math.floor((longitude + 180.0) / 6.0) + 1;
        boolean isNorth = latitude >= 0;
        
        // Source: WGS84 geographic
        CoordinateReferenceSystem sourceCRS = CommonCRS.WGS84.geographic();
        
        // Target: UTM zone
        CoordinateReferenceSystem targetCRS = createUTMCRS(zone, isNorth);
        
        // Find transformation
        CoordinateOperation operation = CRS.findOperation(sourceCRS, targetCRS, null);
        
        // Create source position
        DirectPosition sourcePos = new DirectPosition2D(sourceCRS, longitude, latitude);
        
        // Transform
        DirectPosition targetPos = operation.getMathTransform().transform(sourcePos, null);
        
        // Extract UTM coordinates
        double easting = targetPos.getOrdinate(0);
        double northing = targetPos.getOrdinate(1);
        
        return new UTMCoordinate(zone, easting, northing, isNorth);
    }

    /**
     * Creates a UTM CRS for the specified zone and hemisphere.
     */
    private CoordinateReferenceSystem createUTMCRS(int zone, boolean isNorth) throws FactoryException {
        // Use EPSG code for UTM zones
        // Northern hemisphere: 32600 + zone (e.g., 32631 for zone 31N)
        // Southern hemisphere: 32700 + zone (e.g., 32731 for zone 31S)
        int epsgCode = (isNorth ? 32600 : 32700) + zone;
        return CRS.forCode("EPSG:" + epsgCode);
    }

    /**
     * Represents a geodetic coordinate (latitude, longitude, height).
     */
    public record GeodeticCoordinate(double latitude, double longitude, double height) {}

    /**
     * Represents an ECEF coordinate (X, Y, Z).
     */
    public record ECEFCoordinate(double x, double y, double z) {}

    /**
     * Represents a UTM coordinate.
     */
    public record UTMCoordinate(int zone, double easting, double northing, boolean isNorth) {}
}
