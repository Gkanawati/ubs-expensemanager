package com.ubs.expensemanager.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class InternalHealthPingScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(InternalHealthPingScheduler.class);

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${BACKEND_ENDPOINT}")
    private String backendEndpoint;

    @Value("${ACTUATOR_REQUIRED_USER}")
    private String user;

    @Value("${ACTUATOR_REQUIRED_PASSWORD}")
    private String password;

    /**
     * Pings the internal /actuator/health endpoint every 14 minute.
     */
    @Scheduled(cron = "0 0/14 * * * *")
    public void pingInternalHealthEndpoint() {
        String now = LocalDateTime.now().format(formatter);

        try {
            // 🔐 Basic Auth
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(user, password);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    backendEndpoint,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info(
                        "Internal health ping OK [{}] - status={}, body={}",
                        now,
                        response.getStatusCode(),
                        response.getBody()
                );
            } else {
                logger.warn(
                        "Internal health ping WARNING [{}] - status={}",
                        now,
                        response.getStatusCode()
                );
            }

        } catch (Exception ex) {
            logger.error(
                    "Internal health ping FAILED [{}] - reason={}",
                    now,
                    ex.getMessage(),
                    ex
            );
        }
    }
}


