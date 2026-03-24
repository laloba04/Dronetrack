package com.dronetrack.service;

import com.dronetrack.model.Aircraft;
import com.dronetrack.repository.AircraftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AircraftServiceTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private AircraftService aircraftService;

    @Test
    void getAllAircraft_shouldReturnAllAircraft() {
        Aircraft a1 = new Aircraft("abc1", "IBE001", "Spain", 40.4, -3.7, 8000.0, 250.0, false);
        Aircraft a2 = new Aircraft("abc2", "VLG002", "Spain", 41.3, 2.1, 9000.0, 270.0, false);
        when(aircraftRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Aircraft> result = aircraftService.getAllAircraft();

        assertEquals(2, result.size());
        verify(aircraftRepository, times(1)).findAll();
    }

    @Test
    void getAllAircraft_whenEmpty_shouldReturnEmptyList() {
        when(aircraftRepository.findAll()).thenReturn(List.of());

        assertTrue(aircraftService.getAllAircraft().isEmpty());
    }

    @Test
    void getAircraftInFlight_shouldReturnOnlyAirborne() {
        Aircraft flying = new Aircraft("abc1", "IBE001", "Spain", 40.4, -3.7, 8000.0, 250.0, false);
        when(aircraftRepository.findByOnGroundFalse()).thenReturn(List.of(flying));

        List<Aircraft> result = aircraftService.getAircraftInFlight();

        assertEquals(1, result.size());
        assertFalse(result.get(0).getOnGround());
        verify(aircraftRepository, times(1)).findByOnGroundFalse();
    }

    @Test
    void getAircraftInFlight_whenAllOnGround_shouldReturnEmpty() {
        when(aircraftRepository.findByOnGroundFalse()).thenReturn(List.of());

        assertTrue(aircraftService.getAircraftInFlight().isEmpty());
    }
}
