package com.claims.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.security.access.AccessDeniedException;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String claimant;
    private String market;

    @Enumerated(EnumType.STRING)
    private ClaimType type;

    private String policyNumber;
    private LocalDate incidentDate;
    private String description;
    private String currency;
    private BigDecimal estimatedAmount;
    private BigDecimal reserveAmount;
    private BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    private String assignedOfficer;
    private String decisionReason;
    private Instant createdAt;
    private Instant updatedAt;

    // Two people saving the same claim at once: the second save fails.
    @Version
    private long version;

    protected Claim() {
        // needed by JPA
    }

    public Claim(String claimant, String market, ClaimType type, String policyNumber, LocalDate incidentDate,
            String description, String currency, BigDecimal estimatedAmount) {
        this.claimant = claimant;
        this.market = market;
        this.type = type;
        this.policyNumber = policyNumber;
        this.incidentDate = incidentDate;
        this.description = description;
        this.currency = currency;
        this.estimatedAmount = estimatedAmount;
        this.reserveAmount = estimatedAmount;
        this.status = ClaimStatus.SUBMITTED;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void pickUp(String officer) {
        requireStatus(ClaimStatus.SUBMITTED, "pick up");
        assignedOfficer = officer;
        moveTo(ClaimStatus.UNDER_REVIEW);
    }

    public void requestInfo(String officer) {
        requireAssigned(officer);
        requireStatus(ClaimStatus.UNDER_REVIEW, "request information for");
        moveTo(ClaimStatus.INFO_REQUESTED);
    }

    public void answerInfo() {
        requireStatus(ClaimStatus.INFO_REQUESTED, "answer");
        moveTo(ClaimStatus.UNDER_REVIEW);
    }

    public void changeReserve(String officer, BigDecimal amount) {
        requireAssigned(officer);
        requireStatus(ClaimStatus.UNDER_REVIEW, "change the reserve of");
        reserveAmount = amount;
        updatedAt = Instant.now();
    }

    public void approve(String officer, BigDecimal amount) {
        requireAssigned(officer);
        requireStatus(ClaimStatus.UNDER_REVIEW, "approve");
        approvedAmount = amount;
        moveTo(ClaimStatus.APPROVED);
    }

    public void reject(String officer, String reason) {
        requireAssigned(officer);
        requireStatus(ClaimStatus.UNDER_REVIEW, "reject");
        decisionReason = reason;
        moveTo(ClaimStatus.REJECTED);
    }

    public void settle(String officer) {
        requireAssigned(officer);
        requireStatus(ClaimStatus.APPROVED, "settle");
        moveTo(ClaimStatus.SETTLED);
    }

    private void requireAssigned(String officer) {
        if (!officer.equals(assignedOfficer)) {
            throw new AccessDeniedException("Only the assigned officer can do this");
        }
    }

    private void requireStatus(ClaimStatus required, String action) {
        if (status != required) {
            throw new InvalidTransitionException("Cannot " + action + " a claim with status " + status);
        }
    }

    private void moveTo(ClaimStatus newStatus) {
        status = newStatus;
        updatedAt = Instant.now();
    }

    public String getReference() {
        return "CLM-%06d".formatted(id);
    }

    public Long getId() {
        return id;
    }

    public String getClaimant() {
        return claimant;
    }

    public String getMarket() {
        return market;
    }

    public ClaimType getType() {
        return type;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public BigDecimal getReserveAmount() {
        return reserveAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public String getAssignedOfficer() {
        return assignedOfficer;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
