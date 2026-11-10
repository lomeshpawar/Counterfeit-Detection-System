package com.counterfeit.config;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

class WebConfigTest {

    @Test
    void restTemplateStopsWaitingWhenAiServiceResponseExceedsReadTimeout() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(500);
                byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } catch (IOException clientMayHaveTimedOut) {
                // The client is expected to close the connection after its read timeout.
            } finally {
                exchange.close();
            }
        });
        server.start();

        try {
            RestTemplate restTemplate = new WebConfig().restTemplate(
                    Duration.ofSeconds(1),
                    Duration.ofMillis(100)
            );

            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/slow";
            assertThrows(ResourceAccessException.class,
                    () -> restTemplate.getForObject(url, String.class));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsNonPositiveTimeoutValues() {
        WebConfig config = new WebConfig();

        assertThrows(IllegalArgumentException.class,
                () -> config.restTemplate(Duration.ZERO, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class,
                () -> config.restTemplate(Duration.ofSeconds(1), Duration.ofMillis(-1)));
    }
}
