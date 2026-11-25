package com.claims.claim;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "claim_activities")
public class ClaimActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long claimId;

    @Enumerated(EnumType.STRING)
    private ActivityType type;

    private String actor;
    private String message;
    private Instant createdAt;

    protected ClaimActivity() {
        // needed by JPA
    }

    public ClaimActivity(Long claimId, ActivityType type, String actor, String message) {
        this.claimId = claimId;
        this.type = type;
        this.actor = actor;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public ActivityType getType() {
        return type;
    }

    public String getActor() {
        return actor;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
