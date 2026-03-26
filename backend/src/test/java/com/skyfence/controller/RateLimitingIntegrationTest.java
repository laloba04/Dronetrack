package com.skyfence.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


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

        // In slow CI environments, the 'greedy' refill (1 token/sec) might grant extra tokens during execution.
        // We perform additional requests until we finally hit the 429 status code.
        boolean rateLimited = false;
        for (int i = 0; i < 60; i++) { // Up to 60 more to account for up to 60s of execution time
            int status = mockMvc.perform(get("/api/aircraft/live")
                    .header("X-Forwarded-For", ip1))
                    .andReturn().getResponse().getStatus();
            if (status == 429) {
                rateLimited = true;
                break;
            }
        }
        
        if (!rateLimited) {
            throw new AssertionError("Rate limiting did not trigger even after exceeding capacity (60 + 60 requests).");
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

        // Hit the limit with extra requests to account for refill in slow environments
        boolean rateLimited = false;
        for (int i = 0; i < 10; i++) { 
            int status = mockMvc.perform(post("/api/zones")
                    .contentType("application/json")
                    .content("{\"name\":\"Test Zone\",\"geometry\":\"invalid\"}")
                    .header("X-Forwarded-For", ip2))
                    .andReturn().getResponse().getStatus();
            if (status == 429) {
                rateLimited = true;
                break;
            }
        }

        if (!rateLimited) {
            throw new AssertionError("Rate limiting did not trigger for write operations after 20 attempts.");
        }
    }
}
