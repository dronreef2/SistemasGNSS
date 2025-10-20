package com.geosat.gateway.controller;

import com.geosat.gateway.sis.transform.CoordinateTransformationService;
import com.geosat.gateway.sis.transform.CoordinateTransformationService.UTMCoordinate;
import com.geosat.gateway.sis.units.UnitConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Apache SIS coordinate transformations and unit conversions.
 * Demonstrates the integration of SIS for GNSS geospatial operations.
 */
@RestController
@RequestMapping("/api/v1/transform")
@Tag(name = "SIS Transformations", description = "Coordinate transformations and unit conversions using Apache SIS")
public class SisTransformController {

    @Autowired
    private CoordinateTransformationService transformService;

    @Autowired
    private UnitConversionService unitService;

    /**
     * Converts geodetic coordinates (latitude, longitude) to UTM projection.
     * 
     * @param latitude Latitude in decimal degrees (-90 to 90)
     * @param longitude Longitude in decimal degrees (-180 to 180)
     * @return UTM coordinates with zone, easting, northing, and hemisphere
     */
    @GetMapping("/geodetic-to-utm")
    @Operation(
        summary = "Convert geodetic coordinates to UTM",
        description = "Transforms latitude/longitude coordinates to UTM projection with automatic zone detection"
    )
    public ResponseEntity<Map<String, Object>> geodeticToUtm(
            @Parameter(description = "Latitude in decimal degrees", example = "-15.7939")
            @RequestParam double latitude,
            @Parameter(description = "Longitude in decimal degrees", example = "-47.8828")
            @RequestParam double longitude) {
        
        try {
            UTMCoordinate utm = transformService.geodeticToUTM(latitude, longitude);
            
            Map<String, Object> response = new HashMap<>();
            response.put("input", Map.of(
                "latitude", latitude,
                "longitude", longitude,
                "units", "degrees"
            ));
            response.put("output", Map.of(
                "zone", utm.zone(),
                "hemisphere", utm.isNorth() ? "N" : "S",
                "easting", utm.easting(),
                "northing", utm.northing(),
                "units", "meters"
            ));
            response.put("epsgCode", (utm.isNorth() ? 32600 : 32700) + utm.zone());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Converts a length value between different units.
     * 
     * @param value Numeric value to convert
     * @param fromUnit Source unit (m, km, meter, kilometer)
     * @param toUnit Target unit (m, km, meter, kilometer)
     * @return Converted value with both input and output details
     */
    @GetMapping("/convert-length")
    @Operation(
        summary = "Convert length between units",
        description = "Converts lengths using JSR-385 unit API (meters, kilometers)"
    )
    public ResponseEntity<Map<String, Object>> convertLength(
            @Parameter(description = "Value to convert", example = "5500")
            @RequestParam double value,
            @Parameter(description = "Source unit", example = "m")
            @RequestParam String fromUnit,
            @Parameter(description = "Target unit", example = "km")
            @RequestParam String toUnit) {
        
        try {
            double converted = unitService.convertLength(value, fromUnit, toUnit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("input", Map.of(
                "value", value,
                "unit", fromUnit,
                "formatted", unitService.formatLength(value, fromUnit)
            ));
            response.put("output", Map.of(
                "value", converted,
                "unit", toUnit,
                "formatted", unitService.formatLength(converted, toUnit)
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Converts angles between degrees and radians.
     * 
     * @param value Numeric value to convert
     * @param fromUnit Source unit (deg, rad, degrees, radians)
     * @param toUnit Target unit (deg, rad, degrees, radians)
     * @return Converted angle value
     */
    @GetMapping("/convert-angle")
    @Operation(
        summary = "Convert angle between degrees and radians",
        description = "Converts angular measurements for GNSS calculations"
    )
    public ResponseEntity<Map<String, Object>> convertAngle(
            @Parameter(description = "Angle value", example = "45")
            @RequestParam double value,
            @Parameter(description = "Source unit (deg/rad)", example = "deg")
            @RequestParam String fromUnit,
            @Parameter(description = "Target unit (deg/rad)", example = "rad")
            @RequestParam String toUnit) {
        
        try {
            double converted;
            
            // Convert based on units
            boolean fromDegrees = fromUnit.toLowerCase().startsWith("deg");
            boolean toRadians = toUnit.toLowerCase().startsWith("rad");
            
            if (fromDegrees && toRadians) {
                converted = unitService.degreesToRadians(value);
            } else if (!fromDegrees && !toRadians) {
                converted = unitService.radiansToDegrees(value);
            } else {
                // Same unit or unsupported combination
                converted = value;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("input", Map.of("value", value, "unit", fromUnit));
            response.put("output", Map.of("value", converted, "unit", toUnit));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets information about supported coordinate transformations.
     */
    @GetMapping("/info")
    @Operation(
        summary = "Get transformation service information",
        description = "Returns information about available transformations and supported operations"
    )
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("service", "Apache SIS Transformation Service");
        info.put("version", "1.4");
        
        info.put("coordinateTransformations", Map.of(
            "geodetic-to-utm", Map.of(
                "description", "Converts WGS84 lat/lon to UTM projection",
                "input", "latitude, longitude (degrees)",
                "output", "zone, easting, northing (meters), hemisphere"
            )
        ));
        
        info.put("unitConversions", Map.of(
            "length", Map.of(
                "supportedUnits", new String[]{"m", "km", "meter", "meters", "kilometer", "kilometres"},
                "standard", "JSR-385"
            ),
            "angle", Map.of(
                "supportedUnits", new String[]{"deg", "rad", "degrees", "radians"},
                "standard", "JSR-385"
            )
        ));
        
        info.put("examples", Map.of(
            "utm", "/api/v1/transform/geodetic-to-utm?latitude=-15.7939&longitude=-47.8828",
            "length", "/api/v1/transform/convert-length?value=5500&fromUnit=m&toUnit=km",
            "angle", "/api/v1/transform/convert-angle?value=45&fromUnit=deg&toUnit=rad"
        ));
        
        return ResponseEntity.ok(info);
    }
}
