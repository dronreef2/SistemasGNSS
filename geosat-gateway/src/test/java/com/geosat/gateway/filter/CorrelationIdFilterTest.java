package com.geosat.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@AutoConfigureMockMvc
class CorrelationIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveGerarCorrelationIdSeNaoExistir() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(header().string("X-Correlation-ID", notNullValue()));
    }

    @Test
    void devePreservarCorrelationIdExistente() throws Exception {
        String customId = "test-correlation-id-123";
        mockMvc.perform(get("/actuator/health")
                        .header("X-Correlation-ID", customId))
                .andExpect(header().string("X-Correlation-ID", customId));
    }
}
