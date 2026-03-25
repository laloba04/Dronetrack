package com.skyfence.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that checks the state of the WebSocket STOMP broker.
 * Provides the number of active sessions if available.
 */
@Component("websocket")
public class WebSocketHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private SimpUserRegistry simpUserRegistry;

    @Override
    public Health health() {
        try {
            if (simpUserRegistry != null) {
                // If there's an active registry, we consider it UP and show active counts
                int activeUsers = simpUserRegistry.getUserCount();
                return Health.up()
                        .withDetail("service", "WebSocket STOMP Broker")
                        .withDetail("status", "Reachable")
                        .withDetail("active_users", activeUsers)
                        .build();
            }
            
            // Fallback if registry is not injected but component exists
            return Health.up()
                    .withDetail("service", "WebSocket STOMP Broker")
                    .withDetail("status", "Running")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "WebSocket STOMP Broker")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
