package com.geosat.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for SisTransformController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SisTransformControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGeodeticToUtm_Brasilia() throws Exception {
        mockMvc.perform(get("/api/v1/transform/geodetic-to-utm")
                .param("latitude", "-15.7939")
                .param("longitude", "-47.8828"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.input.latitude").value(-15.7939))
            .andExpect(jsonPath("$.input.longitude").value(-47.8828))
            .andExpect(jsonPath("$.output.zone").value(23))
            .andExpect(jsonPath("$.output.hemisphere").value("S"))
            .andExpect(jsonPath("$.output.easting").exists())
            .andExpect(jsonPath("$.output.northing").exists())
            .andExpect(jsonPath("$.epsgCode").value(32723));  // UTM Zone 23S
    }

    @Test
    void testGeodeticToUtm_NorthernHemisphere() throws Exception {
        mockMvc.perform(get("/api/v1/transform/geodetic-to-utm")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.output.zone").value(18))
            .andExpect(jsonPath("$.output.hemisphere").value("N"))
            .andExpect(jsonPath("$.epsgCode").value(32618));  // UTM Zone 18N
    }

    @Test
    void testConvertLength_MetersToKilometers() throws Exception {
        mockMvc.perform(get("/api/v1/transform/convert-length")
                .param("value", "5500")
                .param("fromUnit", "m")
                .param("toUnit", "km"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.input.value").value(5500.0))
            .andExpect(jsonPath("$.input.unit").value("m"))
            .andExpect(jsonPath("$.output.value").value(closeTo(5.5, 0.001)))
            .andExpect(jsonPath("$.output.unit").value("km"))
            .andExpect(jsonPath("$.output.formatted").value("5.50 km"));
    }

    @Test
    void testConvertLength_KilometersToMeters() throws Exception {
        mockMvc.perform(get("/api/v1/transform/convert-length")
                .param("value", "2.5")
                .param("fromUnit", "km")
                .param("toUnit", "m"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.input.value").value(2.5))
            .andExpect(jsonPath("$.output.value").value(closeTo(2500.0, 0.001)))
            .andExpect(jsonPath("$.output.unit").value("m"));
    }

    @Test
    void testConvertAngle_DegreesToRadians() throws Exception {
        mockMvc.perform(get("/api/v1/transform/convert-angle")
                .param("value", "45")
                .param("fromUnit", "deg")
                .param("toUnit", "rad"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.input.value").value(45.0))
            .andExpect(jsonPath("$.input.unit").value("deg"))
            .andExpect(jsonPath("$.output.value").value(closeTo(0.785398, 0.000001)))
            .andExpect(jsonPath("$.output.unit").value("rad"));
    }

    @Test
    void testConvertAngle_RadiansToDegrees() throws Exception {
        mockMvc.perform(get("/api/v1/transform/convert-angle")
                .param("value", String.valueOf(Math.PI))
                .param("fromUnit", "rad")
                .param("toUnit", "deg"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.input.value").value(Math.PI))
            .andExpect(jsonPath("$.output.value").value(closeTo(180.0, 0.0001)))
            .andExpect(jsonPath("$.output.unit").value("deg"));
    }

    @Test
    void testGetInfo() throws Exception {
        mockMvc.perform(get("/api/v1/transform/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("Apache SIS Transformation Service"))
            .andExpect(jsonPath("$.version").value("1.4"))
            .andExpect(jsonPath("$.coordinateTransformations").exists())
            .andExpect(jsonPath("$.unitConversions").exists())
            .andExpect(jsonPath("$.examples").exists())
            .andExpect(jsonPath("$.examples.utm").isNotEmpty())
            .andExpect(jsonPath("$.examples.length").isNotEmpty())
            .andExpect(jsonPath("$.examples.angle").isNotEmpty());
    }

    @Test
    void testGeodeticToUtm_MissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/transform/geodetic-to-utm")
                .param("latitude", "-15.7939"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testConvertLength_MissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/transform/convert-length")
                .param("value", "100"))
            .andExpect(status().isBadRequest());
    }
}
