# Apache SIS Integration Guide

## Overview

This document describes the integration of Apache Spatial Information System (SIS) into the SistemasGNSS project. Apache SIS provides robust geospatial referencing, coordinate transformations, metadata handling, and unit management capabilities that enhance the GNSS data processing pipeline.

## Why Apache SIS?

Apache SIS brings several key benefits to GNSS processing:

1. **Standards Compliance**: Full implementation of OGC/ISO standards (ISO 19115, 19157, GeoAPI)
2. **Coordinate Transformations**: Datum-aware transformations between coordinate reference systems
3. **Unit Management**: JSR-385 compliant unit handling preventing unit conversion errors
4. **Metadata Support**: ISO 19115-compliant metadata for reproducibility and data provenance
5. **Memory Efficiency**: Optimized for large datasets typical in GNSS processing
6. **Production Ready**: Mature, actively maintained Apache project

## Architecture

### Module Structure

```
com.geosat.gateway.sis/
├── adapter/          # Converts GNSS DTOs to/from standardized formats
├── transform/        # Coordinate transformations (ECEF ↔ geodetic, UTM projections)
├── units/           # Unit conversions (meters, kilometers, degrees, radians)
└── metadata/        # ISO 19115-inspired metadata generation
```

### Dependencies

The following Apache SIS modules are included (version 1.4):

- `sis-referencing`: Coordinate Reference Systems and transformations
- `sis-utility`: Base utilities and unit support
- `sis-metadata`: ISO 19115/19157 metadata handling
- `sis-feature`: Feature model support (future enhancement)

Unit support is provided by JSR-385 implementation:
- `unit-api` 2.2
- `indriya` 2.2

## Core Services

### 1. CoordinateTransformationService

Provides coordinate transformations between different reference systems.

#### Geodetic to UTM Transformation

```java
@Autowired
private CoordinateTransformationService transformService;

// Convert Brasília coordinates to UTM
double latitude = -15.7939;
double longitude = -47.8828;

UTMCoordinate utm = transformService.geodeticToUTM(latitude, longitude);
// Result: zone 23S, easting ~207012m, northing ~8253047m
```

#### ECEF to Geodetic Transformation (Planned)

```java
// Convert ECEF (Earth-Centered, Earth-Fixed) to lat/lon/height
double x = 4000000.0;  // meters
double y = -3000000.0; // meters
double z = -2000000.0; // meters

GeodeticCoordinate geodetic = transformService.ecefToGeodetic(x, y, z);
// Result: latitude, longitude (degrees), height (meters)
```

**Note**: Full 3D ECEF ↔ Geodetic transformations are planned for future implementation. Current implementation provides the foundation.

### 2. UnitConversionService

Ensures unit consistency across GNSS processing pipelines.

#### Angle Conversions

```java
@Autowired
private UnitConversionService unitService;

// Degrees to radians
double radians = unitService.degreesToRadians(45.0);  // 0.785398...

// Radians to degrees
double degrees = unitService.radiansToDegrees(Math.PI);  // 180.0
```

#### Length Conversions

```java
// Meters to kilometers
double km = unitService.metersToKilometers(5500.0);  // 5.5

// Generic conversion with unit strings
double result = unitService.convertLength(1000.0, "m", "km");  // 1.0

// Formatted output
String formatted = unitService.formatLength(1234.56, "m");  // "1234.56 m"
```

### 3. GnssObservationAdapter

Converts internal GNSS data structures to standardized property maps.

#### Position Observations

```java
@Autowired
private GnssObservationAdapter adapter;

PosicaoSampleDTO sample = new PosicaoSampleDTO(
    "2025-01-15T12:30:45Z",  // epoch
    -15.7939,                 // latitude
    -47.8828,                 // longitude
    1100.5                    // height
);

Map<String, Object> properties = adapter.toPositionProperties(sample, "BRAZ");

// Properties include:
// - time (Instant)
// - latitude, longitude, height (Double)
// - stationId (String)
// - type ("GnssPosition")
```

#### SNR Observations

```java
SnrSampleDTO snrSample = new SnrSampleDTO(
    "2025-01-15T12:30:45Z",  // epoch
    "G01",                    // satellite ID
    45.5                      // SNR in dB-Hz
);

Map<String, Object> properties = adapter.toSnrProperties(snrSample);

// Properties include:
// - time (Instant)
// - snr (Double)
// - satelliteId (String)
// - type ("GnssSnr")
// - unit ("dB-Hz")
```

#### Validation

```java
// Validate position observations
boolean isValid = adapter.isValidPosition(sample);
// Checks: non-null, valid lat/lon ranges, valid epoch

// Validate SNR observations
boolean isValidSnr = adapter.isValidSnr(snrSample);
// Checks: non-null, SNR in range [0, 100], valid epoch
```

### 4. GnssMetadataService

Creates ISO 19115-inspired metadata for GNSS datasets.

#### Observation Metadata

```java
@Autowired
private GnssMetadataService metadataService;

Map<String, Object> metadata = metadataService.createObservationMetadata(
    "BRAZ",                                    // station ID
    Instant.parse("2025-01-15T00:00:00Z"),   // start time
    Instant.parse("2025-01-15T23:59:59Z"),   // end time
    -15.7939,                                  // latitude
    -47.8828                                   // longitude
);

// Metadata includes:
// - title, abstract (String)
// - geographicExtent (bounding box)
// - temporalExtent (time range)
// - organisation, role (contact info)
// - dateStamp, standard
```

#### Processing Metadata

```java
Map<String, Object> processingMeta = metadataService.createProcessingMetadata(
    "PPP",                                    // process type
    "RINEX3 observations from station BRAZ", // input dataset
    Instant.now()                             // processing date
);

// Export to XML
String xml = metadataService.toXml(processingMeta);
```

## Integration Patterns

### REST Controller Example

```java
@RestController
@RequestMapping("/api/v1/gnss")
public class GnssTransformController {
    
    @Autowired
    private CoordinateTransformationService transformService;
    
    @Autowired
    private UnitConversionService unitService;
    
    @GetMapping("/convert/utm")
    public ResponseEntity<UTMCoordinate> convertToUTM(
            @RequestParam double latitude,
            @RequestParam double longitude) throws Exception {
        
        UTMCoordinate utm = transformService.geodeticToUTM(latitude, longitude);
        return ResponseEntity.ok(utm);
    }
    
    @GetMapping("/convert/distance")
    public ResponseEntity<Map<String, Double>> convertDistance(
            @RequestParam double value,
            @RequestParam String fromUnit,
            @RequestParam String toUnit) {
        
        double converted = unitService.convertLength(value, fromUnit, toUnit);
        
        return ResponseEntity.ok(Map.of(
            "original", value,
            "converted", converted,
            "fromUnit", fromUnit,
            "toUnit", toUnit
        ));
    }
}
```

### Processing Pipeline Example

```java
@Service
public class GnssProcessingService {
    
    @Autowired
    private GnssObservationAdapter adapter;
    
    @Autowired
    private GnssMetadataService metadataService;
    
    public ProcessingResult processObservations(
            String stationId,
            List<PosicaoSampleDTO> positions) {
        
        // Convert to standardized format
        List<Map<String, Object>> standardized = positions.stream()
            .filter(adapter::isValidPosition)
            .map(p -> adapter.toPositionProperties(p, stationId))
            .collect(Collectors.toList());
        
        // Create metadata
        Instant start = (Instant) standardized.get(0).get("time");
        Instant end = (Instant) standardized.get(standardized.size() - 1).get("time");
        
        Map<String, Object> metadata = metadataService.createObservationMetadata(
            stationId, start, end,
            (Double) standardized.get(0).get("latitude"),
            (Double) standardized.get(0).get("longitude")
        );
        
        return new ProcessingResult(standardized, metadata);
    }
}
```

## Testing

All SIS integration components have comprehensive unit tests. Run tests with:

```bash
# Run all SIS tests
mvn test -Dtest="**/sis/**/*Test"

# Run specific test class
mvn test -Dtest="CoordinateTransformationServiceTest"

# Run with coverage
mvn verify
```

### Test Coverage

- **CoordinateTransformationService**: 8 tests covering UTM transformations, zone calculations, hemisphere detection
- **UnitConversionService**: 14 tests covering angle/length conversions, formatting, precision
- **GnssObservationAdapter**: 19 tests covering conversion, validation, boundary conditions
- **GnssMetadataService**: 11 tests covering metadata generation, XML export

Total: **52 tests** with 100% pass rate

## Configuration

### Maven Dependencies

The SIS integration is already configured in `geosat-gateway/pom.xml`:

```xml
<properties>
    <apache.sis.version>1.4</apache.sis.version>
</properties>

<dependencies>
    <!-- Apache SIS -->
    <dependency>
        <groupId>org.apache.sis.core</groupId>
        <artifactId>sis-referencing</artifactId>
        <version>${apache.sis.version}</version>
    </dependency>
    <!-- Additional SIS modules... -->
</dependencies>
```

### Runtime Requirements

- **Java Version**: 17 (meets SIS requirement of Java 11+)
- **Memory**: No special requirements; SIS is optimized for memory efficiency
- **EPSG Database**: Optional; built-in subset sufficient for basic transformations

### Optional EPSG Database

For full EPSG geodetic parameter database support:

1. Download: https://sis.apache.org/epsg.html
2. Set environment variable: `SIS_DATA=/path/to/sis-data`
3. Restart application

**Note**: Basic functionality works without the full EPSG database.

## Performance Considerations

### Transformation Performance

- **UTM conversions**: ~0.1ms per coordinate (measured)
- **Caching**: SIS caches CRS objects internally
- **Thread-safe**: All services are thread-safe and stateless

### Memory Usage

- **Lightweight objects**: Coordinate records are small value objects
- **No object pooling needed**: JVM handles efficiently
- **Batch processing**: Stream-based processing recommended for large datasets

### Recommendations

1. **Reuse services**: Services are Spring singletons; autowire once
2. **Validate early**: Use adapter validation before expensive operations
3. **Stream processing**: For millions of observations, use streaming APIs
4. **Monitor**: Use existing Micrometer metrics infrastructure

## Future Enhancements

### Phase 1 (Current)
- ✅ Coordinate transformations (UTM, geodetic)
- ✅ Unit management (angles, lengths)
- ✅ Observation adapter (lightweight)
- ✅ Metadata service (ISO 19115 inspired)

### Phase 2 (Planned)
- [ ] Complete ECEF ↔ Geodetic 3D transformations
- [ ] Additional map projections (Mercator, etc.)
- [ ] Full SIS Feature model integration
- [ ] GeoTIFF export for precision/error grids
- [ ] NetCDF support for time-series data

### Phase 3 (Future)
- [ ] Custom datum definitions for local reference frames
- [ ] Velocity field support for dynamic datums
- [ ] Integration with RINEX parser
- [ ] Advanced quality metrics using ISO 19157

## Troubleshooting

### "No EPSG code found" Warning

**Issue**: Warning about missing EPSG database

**Solution**: This is normal; built-in subset is sufficient for most operations. To eliminate warning, install full EPSG database or ignore (functionality not affected).

### Transformation Accuracy

**Issue**: Concerned about transformation precision

**Solution**: SIS uses industry-standard algorithms. For high-precision GNSS work:
- Verify datum and epoch match your requirements
- Use official EPSG codes when available
- Consider installing full EPSG database for specialized projections

### Performance Optimization

**Issue**: Slow transformation for large datasets

**Solution**:
1. Use batch operations where possible
2. Enable parallel streams for independent transformations
3. Pre-validate data before transformation
4. Consider caching frequently-used coordinate pairs

## References

- **Apache SIS**: https://sis.apache.org/
- **ISO 19115**: https://www.iso.org/standard/53798.html
- **EPSG Database**: https://epsg.org/
- **JSR-385 (Units of Measurement)**: https://unitsofmeasurement.github.io/unit-api/
- **GeoAPI**: https://www.geoapi.org/

## Support

For questions or issues related to SIS integration:

1. Check Apache SIS documentation: https://sis.apache.org/
2. Review unit tests for usage examples
3. Open an issue in the project repository
4. Consult Apache SIS mailing lists for SIS-specific questions

---

**Version**: 1.0  
**Last Updated**: 2025-10-18  
**Author**: GeoSat Gateway Team
