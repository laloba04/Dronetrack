package com.dronetrack.controller;

import com.dronetrack.model.RestrictedZone;
import com.dronetrack.repository.RestrictedZoneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ZoneController.class)
@ActiveProfiles("test")
class ZoneControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RestrictedZoneRepository zoneRepository;

    @Test
    void getAll_shouldReturn200WithZones() throws Exception {
        RestrictedZone zone = new RestrictedZone("Aeropuerto Madrid-Barajas", "AIRPORT", 40.4983, -3.5676, 5.0);
        when(zoneRepository.findAll()).thenReturn(List.of(zone));

        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aeropuerto Madrid-Barajas"))
                .andExpect(jsonPath("$[0].type").value("AIRPORT"));
    }

    @Test
    void getAll_whenEmpty_shouldReturnEmptyList() throws Exception {
        when(zoneRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void create_shouldReturn200WithSavedZone() throws Exception {
        RestrictedZone zone = new RestrictedZone("Base Naval de Rota", "MILITARY", 36.6412, -6.3496, 5.0);
        when(zoneRepository.save(any(RestrictedZone.class))).thenReturn(zone);

        mockMvc.perform(post("/api/zones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Base Naval de Rota"))
                .andExpect(jsonPath("$.type").value("MILITARY"));

        verify(zoneRepository, times(1)).save(any(RestrictedZone.class));
    }

    @Test
    void delete_shouldReturn200AndCallRepository() throws Exception {
        doNothing().when(zoneRepository).deleteById(1L);

        mockMvc.perform(delete("/api/zones/1"))
                .andExpect(status().isOk());

        verify(zoneRepository, times(1)).deleteById(1L);
    }
}
