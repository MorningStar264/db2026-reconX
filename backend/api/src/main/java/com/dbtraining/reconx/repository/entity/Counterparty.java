package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * TICKET-ADV050 — JPA entity Counterparty (one of the 8 entities of ADV006).
 */
@Entity
@Table(name = "counterparties")
public class Counterparty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "lei_code", nullable = false, unique = true, length = 20)
    private String leiCode;

    @Column(nullable = false, length = 10)
    private String region;

    @Column(name = "created_at")
    private Instant createdAt;

    // Additional fields for compatibility
    @Column(length = 50)
    private String code;

    @Column(length = 50)
    private String type;

    public Counterparty() {}

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLeiCode() { return leiCode; }
    public String getRegion() { return region; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCode() { return code; }
    public String getType() { return type; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLeiCode(String leiCode) { this.leiCode = leiCode; }
    public void setRegion(String region) { this.region = region; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setCode(String code) { this.code = code; }
    public void setType(String type) { this.type = type; }
}