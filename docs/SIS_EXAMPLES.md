# Apache SIS Integration - Code Examples

## Quick Start Examples

### 1. Coordinate Transformation Example

Convert Brazilian station coordinates to UTM projection:

```java
import com.geosat.gateway.sis.transform.CoordinateTransformationService;
import com.geosat.gateway.sis.transform.CoordinateTransformationService.UTMCoordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoordinateExample {
    
    @Autowired
    private CoordinateTransformationService transformService;
    
    public void convertBrazilianStations() throws Exception {
        // Brasília coordinates
        UTMCoordinate brasilia = transformService.geodeticToUTM(-15.7939, -47.8828);
        System.out.printf("Brasília - Zone: %d%s, E: %.2f, N: %.2f%n",
            brasilia.zone(),
            brasilia.isNorth() ? "N" : "S",
            brasilia.easting(),
            brasilia.northing()
        );
        // Output: Brasília - Zone: 23S, E: 207012.27, N: 8253047.03
        
        // Rio de Janeiro coordinates
        UTMCoordinate rio = transformService.geodeticToUTM(-22.9068, -43.1729);
        System.out.printf("Rio - Zone: %d%s, E: %.2f, N: %.2f%n",
            rio.zone(),
            rio.isNorth() ? "N" : "S",
            rio.easting(),
            rio.northing()
        );
        // Output: Rio - Zone: 23S, E: 683919.82, N: 7467445.23
        
        // São Paulo coordinates
        UTMCoordinate saopaulo = transformService.geodeticToUTM(-23.5505, -46.6333);
        System.out.printf("São Paulo - Zone: %d%s, E: %.2f, N: %.2f%n",
            saopaulo.zone(),
            saopaulo.isNorth() ? "N" : "S",
            saopaulo.easting(),
            saopaulo.northing()
        );
        // Output: São Paulo - Zone: 23S, E: 333851.47, N: 7395932.69
    }
}
```

### 2. Unit Conversion Example

Handle GNSS units consistently:

```java
import com.geosat.gateway.sis.units.UnitConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnitExample {
    
    @Autowired
    private UnitConversionService unitService;
    
    public void gnssDistanceCalculations() {
        // Satellite altitude in kilometers
        double satelliteAltKm = 20200.0;
        double satelliteAltM = unitService.kilometersToMeters(satelliteAltKm);
        System.out.println("Satellite altitude: " + 
            unitService.formatLength(satelliteAltM, "m"));
        // Output: Satellite altitude: 20200000.00 m
        
        // Baseline distance
        double baselineM = 1234.56;
        double baselineKm = unitService.metersToKilometers(baselineM);
        System.out.printf("Baseline: %.2f km%n", baselineKm);
        // Output: Baseline: 1.23 km
        
        // Angular conversions for orbital calculations
        double elevationAngleDeg = 15.0;
        double elevationAngleRad = unitService.degreesToRadians(elevationAngleDeg);
        System.out.printf("Elevation: %.4f rad (%.1f°)%n",
            elevationAngleRad, elevationAngleDeg);
        // Output: Elevation: 0.2618 rad (15.0°)
        
        // Generic conversion
        double distanceKm = unitService.convertLength(5500.0, "m", "km");
        System.out.printf("Distance: %.1f km%n", distanceKm);
        // Output: Distance: 5.5 km
    }
}
```

### 3. Observation Adapter Example

Validate and convert GNSS observations:

```java
import com.geosat.gateway.model.PosicaoSampleDTO;
import com.geosat.gateway.model.SnrSampleDTO;
import com.geosat.gateway.sis.adapter.GnssObservationAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class ObservationExample {
    
    @Autowired
    private GnssObservationAdapter adapter;
    
    public void processStationData() {
        String stationId = "BRAZ";
        
        // Position observation
        PosicaoSampleDTO position = new PosicaoSampleDTO(
            "2025-01-15T12:30:45Z",
            -15.7939,
            -47.8828,
            1100.5
        );
        
        // Validate before processing
        if (adapter.isValidPosition(position)) {
            Map<String, Object> props = adapter.toPositionProperties(position, stationId);
            
            System.out.println("Position observation:");
            System.out.println("  Time: " + props.get("time"));
            System.out.println("  Latitude: " + props.get("latitude") + "°");
            System.out.println("  Longitude: " + props.get("longitude") + "°");
            System.out.println("  Height: " + props.get("height") + " m");
            System.out.println("  Station: " + props.get("stationId"));
        }
        
        // SNR observation
        SnrSampleDTO snr = new SnrSampleDTO(
            "2025-01-15T12:30:45Z",
            "G01",
            45.5
        );
        
        if (adapter.isValidSnr(snr)) {
            Map<String, Object> props = adapter.toSnrProperties(snr);
            
            System.out.println("\nSNR observation:");
            System.out.println("  Time: " + props.get("time"));
            System.out.println("  Satellite: " + props.get("satelliteId"));
            System.out.println("  SNR: " + props.get("snr") + " " + props.get("unit"));
        }
    }
    
    public void filterValidObservations(List<PosicaoSampleDTO> observations) {
        List<PosicaoSampleDTO> valid = observations.stream()
            .filter(adapter::isValidPosition)
            .toList();
        
        System.out.printf("Filtered %d valid positions from %d total%n",
            valid.size(), observations.size());
    }
}
```

### 4. Metadata Generation Example

Create ISO 19115-compliant metadata:

```java
import com.geosat.gateway.sis.metadata.GnssMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class MetadataExample {
    
    @Autowired
    private GnssMetadataService metadataService;
    
    public void createDatasetMetadata() {
        // Create observation metadata
        Map<String, Object> metadata = metadataService.createObservationMetadata(
            "BRAZ",
            Instant.parse("2025-01-15T00:00:00Z"),
            Instant.parse("2025-01-15T23:59:59Z"),
            -15.7939,
            -47.8828
        );
        
        // Print metadata
        System.out.println("Dataset Metadata:");
        System.out.println("  Title: " + metadata.get("title"));
        System.out.println("  Abstract: " + metadata.get("abstract"));
        System.out.println("  Organisation: " + metadata.get("organisation"));
        System.out.println("  Date: " + metadata.get("dateStamp"));
        
        // Geographic extent
        @SuppressWarnings("unchecked")
        Map<String, Object> extent = (Map<String, Object>) metadata.get("geographicExtent");
        System.out.println("  Extent:");
        System.out.println("    West: " + extent.get("westBound"));
        System.out.println("    East: " + extent.get("eastBound"));
        System.out.println("    South: " + extent.get("southBound"));
        System.out.println("    North: " + extent.get("northBound"));
        
        // Export to XML
        String xml = metadataService.toXml(metadata);
        System.out.println("\nXML Export:");
        System.out.println(xml);
    }
    
    public void createProcessingMetadata() {
        Map<String, Object> metadata = metadataService.createProcessingMetadata(
            "PPP",
            "RINEX3 observations from station BRAZ, DOY 015/2025",
            Instant.now()
        );
        
        System.out.println("\nProcessing Metadata:");
        System.out.println("  Title: " + metadata.get("title"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> processing = (Map<String, Object>) metadata.get("processingInfo");
        System.out.println("  Process Type: " + processing.get("type"));
        System.out.println("  Input Dataset: " + processing.get("inputDataset"));
        System.out.println("  Processing Date: " + processing.get("processingDate"));
    }
}
```

### 5. Complete Pipeline Example

Integrate all components in a processing pipeline:

```java
import com.geosat.gateway.model.PosicaoSampleDTO;
import com.geosat.gateway.sis.adapter.GnssObservationAdapter;
import com.geosat.gateway.sis.metadata.GnssMetadataService;
import com.geosat.gateway.sis.transform.CoordinateTransformationService;
import com.geosat.gateway.sis.units.UnitConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GnssProcessingPipeline {
    
    @Autowired
    private GnssObservationAdapter adapter;
    
    @Autowired
    private CoordinateTransformationService transformService;
    
    @Autowired
    private UnitConversionService unitService;
    
    @Autowired
    private GnssMetadataService metadataService;
    
    public ProcessingResult process(String stationId, List<PosicaoSampleDTO> observations) {
        System.out.println("Starting GNSS processing pipeline...");
        
        // Step 1: Validate and filter observations
        List<PosicaoSampleDTO> validObservations = observations.stream()
            .filter(adapter::isValidPosition)
            .collect(Collectors.toList());
        
        System.out.printf("Validated %d of %d observations%n",
            validObservations.size(), observations.size());
        
        // Step 2: Convert to standardized format
        List<Map<String, Object>> standardized = validObservations.stream()
            .map(obs -> adapter.toPositionProperties(obs, stationId))
            .collect(Collectors.toList());
        
        // Step 3: Add UTM coordinates to each observation
        for (int i = 0; i < validObservations.size(); i++) {
            PosicaoSampleDTO obs = validObservations.get(i);
            Map<String, Object> props = standardized.get(i);
            
            try {
                var utm = transformService.geodeticToUTM(obs.lat(), obs.lon());
                props.put("utm_zone", utm.zone());
                props.put("utm_easting", utm.easting());
                props.put("utm_northing", utm.northing());
                props.put("utm_hemisphere", utm.isNorth() ? "N" : "S");
            } catch (Exception e) {
                System.err.println("Warning: UTM conversion failed for observation " + i);
            }
        }
        
        // Step 4: Calculate statistics
        double minHeight = validObservations.stream()
            .mapToDouble(obs -> obs.h() != null ? obs.h() : 0.0)
            .min().orElse(0.0);
        
        double maxHeight = validObservations.stream()
            .mapToDouble(obs -> obs.h() != null ? obs.h() : 0.0)
            .max().orElse(0.0);
        
        double heightRange = maxHeight - minHeight;
        
        System.out.printf("Height range: %.2f m (%.3f km)%n",
            heightRange,
            unitService.metersToKilometers(heightRange));
        
        // Step 5: Create metadata
        Instant startTime = (Instant) standardized.get(0).get("time");
        Instant endTime = (Instant) standardized.get(standardized.size() - 1).get("time");
        
        double avgLat = validObservations.stream()
            .mapToDouble(PosicaoSampleDTO::lat)
            .average().orElse(0.0);
        
        double avgLon = validObservations.stream()
            .mapToDouble(PosicaoSampleDTO::lon)
            .average().orElse(0.0);
        
        Map<String, Object> metadata = metadataService.createObservationMetadata(
            stationId, startTime, endTime, avgLat, avgLon
        );
        
        // Step 6: Create result
        ProcessingResult result = new ProcessingResult();
        result.setStationId(stationId);
        result.setObservationCount(standardized.size());
        result.setObservations(standardized);
        result.setMetadata(metadata);
        result.setStatistics(Map.of(
            "minHeight", minHeight,
            "maxHeight", maxHeight,
            "heightRange", heightRange,
            "avgLatitude", avgLat,
            "avgLongitude", avgLon
        ));
        
        System.out.println("Processing complete!");
        return result;
    }
    
    public static class ProcessingResult {
        private String stationId;
        private int observationCount;
        private List<Map<String, Object>> observations;
        private Map<String, Object> metadata;
        private Map<String, Double> statistics;
        
        // Getters and setters
        public String getStationId() { return stationId; }
        public void setStationId(String stationId) { this.stationId = stationId; }
        
        public int getObservationCount() { return observationCount; }
        public void setObservationCount(int count) { this.observationCount = count; }
        
        public List<Map<String, Object>> getObservations() { return observations; }
        public void setObservations(List<Map<String, Object>> obs) { this.observations = obs; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> meta) { this.metadata = meta; }
        
        public Map<String, Double> getStatistics() { return statistics; }
        public void setStatistics(Map<String, Double> stats) { this.statistics = stats; }
    }
}
```

## Testing Your Integration

### Unit Test Example

```java
import com.geosat.gateway.sis.transform.CoordinateTransformationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyTransformationTest {
    
    @Autowired
    private CoordinateTransformationService transformService;
    
    @Test
    void testBrazilianStationConversion() throws Exception {
        // Known coordinates for Brasília RBMC station
        double lat = -15.7939;
        double lon = -47.8828;
        
        var utm = transformService.geodeticToUTM(lat, lon);
        
        // Verify zone
        assertEquals(23, utm.zone());
        assertFalse(utm.isNorth());
        
        // Verify coordinates are in reasonable range
        assertTrue(utm.easting() > 0);
        assertTrue(utm.northing() > 0);
    }
}
```

## Command-Line Usage

If you create a simple CLI runner:

```bash
# Convert coordinates
curl "http://localhost:8080/api/v1/gnss/convert/utm?latitude=-15.7939&longitude=-47.8828"

# Convert units
curl "http://localhost:8080/api/v1/gnss/convert/distance?value=5500&fromUnit=m&toUnit=km"
```

## Next Steps

1. **Explore the API**: Review the full [SIS Integration Guide](SIS_INTEGRATION.md)
2. **Run Tests**: Execute `mvn test -Dtest="**/sis/**/*Test"` to see all examples
3. **Check Documentation**: Read Apache SIS docs at https://sis.apache.org/
4. **Extend**: Add your own transformations and processing logic

---

**Note**: All examples assume Spring Boot context with autowired services. Adjust for your specific application structure.
