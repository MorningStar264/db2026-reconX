package com.dbtraining.reconx.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Requires Docker - run manually when Docker is available")
class TradeLifecycleIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reconx")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;
    
    @Autowired
    private ObjectMapper om;

    private static String token;
    private static Long createdId;
    private static String reconJobId;
    private static Long breakId;

    private final RestTemplate http = new RestTemplate();

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(token);
        return h;
    }

    @Test
    @Order(1)
    @Disabled("Requires authentication endpoint to be implemented")
    void loginAsAdmin() {
        String body = """
                {"username":"admin@db.com","password":"admin123"}
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(body, headers);
        
        ResponseEntity<JsonNode> resp = http.postForEntity(
                "http://localhost:" + port + "/api/auth/login", req, JsonNode.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        token = resp.getBody().get("token").asText();
        assertThat(token).isNotNull();
    }

    @Test
    @Order(2)
    @Disabled("Requires authentication first")
    void createTrade() {
        String body = """
                {"tradeRef":"INT-20260315-0001","instrumentId":1,"counterpartyId":1,
                 "assetClass":"EQUITY","side":"BUY",
                 "quantity":100.0,"price":245.50,"tradeDate":"2026-03-15"}
                """;
        ResponseEntity<JsonNode> resp = http.exchange(
                "http://localhost:" + port + "/api/v1/trades",
                HttpMethod.POST, new HttpEntity<>(body, authHeaders()), JsonNode.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        createdId = resp.getBody().get("id").asLong();
    }

    @Test
    @Order(3)
    @Disabled("Requires trade to be created first")
    void getTradeBack() {
        ResponseEntity<JsonNode> resp = http.exchange(
                "http://localhost:" + port + "/api/v1/trades?status=PENDING",
                HttpMethod.GET, new HttpEntity<>(authHeaders()), JsonNode.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("totalElements").asLong()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(4)
    @Disabled("Requires trade to be created first")
    void patchStatus() {
        String body = """
                {"status":"MATCHED"}
                """;
        ResponseEntity<JsonNode> resp = http.exchange(
                "http://localhost:" + port + "/api/v1/trades/" + createdId + "/status",
                HttpMethod.PATCH, new HttpEntity<>(body, authHeaders()), JsonNode.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("status").asText()).isEqualTo("MATCHED");
    }

    @Test
    @Order(5)
    @Disabled("Requires reconciliation endpoint to be implemented")
    void triggerRecon() {
        String body = """
                {"from":"2026-03-01","to":"2026-03-31"}
                """;
        ResponseEntity<JsonNode> resp = http.exchange(
                "http://localhost:" + port + "/api/v1/recon/run",
                HttpMethod.POST, new HttpEntity<>(body, authHeaders()), JsonNode.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        reconJobId = resp.getBody().get("jobId").asText();
    }

    @Test
    @Order(6)
    @Disabled("Requires reconciliation to be triggered first")
    void resolveBreak() {
        breakId = 1L;
        String body = """
                {"note":"Confirmed via counterparty email on 2026-03-16."}
                """;
        ResponseEntity<JsonNode> resp = http.exchange(
                "http://localhost:" + port + "/api/v1/recon/results/" + breakId + "/resolve",
                HttpMethod.PUT, new HttpEntity<>(body, authHeaders()), JsonNode.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("status").asText()).isEqualTo("RESOLVED");
    }
}