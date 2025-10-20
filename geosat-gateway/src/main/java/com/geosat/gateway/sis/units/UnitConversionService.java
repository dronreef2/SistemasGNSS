package com.geosat.gateway.sis.units;

import org.springframework.stereotype.Service;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.MetricPrefix;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

/**
 * Service for unit conversions using JSR-385 (via Apache SIS).
 * Ensures consistency in unit handling across GNSS processing pipelines.
 */
@Service
public class UnitConversionService {

    /**
     * Converts degrees to radians.
     * 
     * @param degrees Angle in degrees
     * @return Angle in radians
     */
    public double degreesToRadians(double degrees) {
        // Direct mathematical conversion (more reliable than unit library)
        return Math.toRadians(degrees);
    }

    /**
     * Converts radians to degrees.
     * 
     * @param radians Angle in radians
     * @return Angle in degrees
     */
    public double radiansToDegrees(double radians) {
        // Direct mathematical conversion (more reliable than unit library)
        return Math.toDegrees(radians);
    }

    /**
     * Converts meters to kilometers.
     * 
     * @param meters Length in meters
     * @return Length in kilometers
     */
    public double metersToKilometers(double meters) {
        Quantity<Length> lengthInMeters = Quantities.getQuantity(meters, Units.METRE);
        Quantity<Length> lengthInKilometers = lengthInMeters.to(MetricPrefix.KILO(Units.METRE));
        return lengthInKilometers.getValue().doubleValue();
    }

    /**
     * Converts kilometers to meters.
     * 
     * @param kilometers Length in kilometers
     * @return Length in meters
     */
    public double kilometersToMeters(double kilometers) {
        Quantity<Length> lengthInKilometers = Quantities.getQuantity(kilometers, MetricPrefix.KILO(Units.METRE));
        Quantity<Length> lengthInMeters = lengthInKilometers.to(Units.METRE);
        return lengthInMeters.getValue().doubleValue();
    }

    /**
     * Validates and converts a length value with explicit unit.
     * 
     * @param value Numeric value
     * @param sourceUnit Source unit (e.g., "m", "km")
     * @param targetUnit Target unit (e.g., "m", "km")
     * @return Converted value
     */
    public double convertLength(double value, String sourceUnit, String targetUnit) {
        Unit<Length> source = parseUnit(sourceUnit, Units.METRE);
        Unit<Length> target = parseUnit(targetUnit, Units.METRE);
        
        Quantity<Length> quantity = Quantities.getQuantity(value, source);
        Quantity<Length> converted = quantity.to(target);
        
        return converted.getValue().doubleValue();
    }

    /**
     * Parses a unit string to a Unit object.
     * Supports common GNSS units: m, km, deg, rad
     */
    private Unit<Length> parseUnit(String unitStr, Unit<Length> defaultUnit) {
        return switch (unitStr.toLowerCase()) {
            case "m", "meter", "meters", "metre", "metres" -> Units.METRE;
            case "km", "kilometer", "kilometers", "kilometre", "kilometres" -> MetricPrefix.KILO(Units.METRE);
            default -> defaultUnit;
        };
    }

    /**
     * Creates a formatted string with unit for a length value.
     * 
     * @param value Numeric value
     * @param unit Unit abbreviation
     * @return Formatted string (e.g., "1234.56 m")
     */
    public String formatLength(double value, String unit) {
        return String.format("%.2f %s", value, unit);
    }
}
