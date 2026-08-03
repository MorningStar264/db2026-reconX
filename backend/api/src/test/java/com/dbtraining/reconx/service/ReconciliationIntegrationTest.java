package com.dbtraining.reconx.service;

import com.dbtraining.reconx.domain.Trade;
import com.dbtraining.reconx.domain.TradeStatus;
import com.dbtraining.reconx.repository.TradeRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Disabled("Requires Docker - run manually when Docker is available")
class ReconciliationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reconx")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TradeRepository internalTradeRepo;

    @Autowired
    private TradeRepository externalTradeRepo;

    @Autowired
    private ReconciliationEngine reconciliationService;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void containerIsRunning() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void insertedTradesAreReconciledAndPersisted() {
        // given — two matching trades
        Trade internal = new Trade();
        internal.setTradeRef("TRD-INT-1");
        internal.setQuantity(new BigDecimal("100"));
        internal.setPrice(new BigDecimal("245.50"));
        internal.setTradeDate(LocalDate.now());
        internal.setStatus(TradeStatus.PENDING);

        Trade external = new Trade();
        external.setTradeRef("TRD-INT-1");
        external.setQuantity(new BigDecimal("100"));
        external.setPrice(new BigDecimal("245.50"));
        external.setTradeDate(LocalDate.now());
        external.setStatus(TradeStatus.PENDING);

        internalTradeRepo.save(internal);
        externalTradeRepo.save(external);

        assertThat(internalTradeRepo.findAll()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(externalTradeRepo.findAll()).hasSizeGreaterThanOrEqualTo(1);
    }
}