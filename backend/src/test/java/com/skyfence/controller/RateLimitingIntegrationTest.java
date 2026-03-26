package com.skyfence.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RateLimitingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testReadRateLimit() throws Exception {
        String ip1 = "192.168.1.100";
        // Send 60 requests (valid)
        for (int i = 0; i < 60; i++) {
            final int reqIndex = i;
            mockMvc.perform(get("/api/aircraft/live")
                    .header("X-Forwarded-For", ip1))
                    .andExpect(result -> {
                        int code = result.getResponse().getStatus();
                        if (code == 429) {
                            throw new AssertionError("Request " + reqIndex + " was rate limited prematurely!");
                        }
                    });
        }

        // Due to greedy refill (1 token/sec), the test execution time might have granted 1-2 extra tokens.
        // We will make a few more requests and assert that at least one of them gets rate limited (429).
        boolean rateLimited = false;
        for (int i = 0; i < 5; i++) {
            int status = mockMvc.perform(get("/api/aircraft/live")
                    .header("X-Forwarded-For", ip1))
                    .andReturn().getResponse().getStatus();
            if (status == 429) {
                rateLimited = true;
                break;
            }
        }
        
        if (!rateLimited) {
            throw new AssertionError("Rate limiting did not trigger even after exceeding capacity.");
        }
    }

    @Test
    public void testWriteRateLimit() throws Exception {
        String ip2 = "192.168.1.200";
        // Send 10 requests (valid)
        for (int i = 0; i < 10; i++) {
            final int reqIndex = i;
            mockMvc.perform(post("/api/zones")
                    .contentType("application/json")
                    .content("{\"name\":\"Test Zone\",\"geometry\":\"invalid\"}")
                    .header("X-Forwarded-For", ip2))
                    .andExpect(result -> {
                        if (result.getResponse().getStatus() == 429) {
                            throw new AssertionError("Request " + reqIndex + " was rate limited prematurely!");
                        }
                    });
        }

        // 11th request should be rate limited
        mockMvc.perform(post("/api/zones")
                .contentType("application/json")
                .content("{\"name\":\"Test Zone\",\"geometry\":\"invalid\"}")
                .header("X-Forwarded-For", ip2))
                .andExpect(status().isTooManyRequests());
    }
}
