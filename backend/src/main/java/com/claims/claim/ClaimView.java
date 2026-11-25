package com.claims.claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** The reserve is internal to claims staff, so claimants get null. */
public record ClaimView(Long id, String reference, String claimant, String market, ClaimType type,
        String policyNumber, LocalDate incidentDate, String description, String currency,
        BigDecimal estimatedAmount, BigDecimal reserveAmount, BigDecimal approvedAmount, ClaimStatus status,
        String assignedOfficer, String decisionReason, Instant createdAt, Instant updatedAt) {

    static ClaimView of(Claim c, boolean officer) {
        return new ClaimView(c.getId(), c.getReference(), c.getClaimant(), c.getMarket(), c.getType(),
                c.getPolicyNumber(), c.getIncidentDate(), c.getDescription(), c.getCurrency(),
                c.getEstimatedAmount(), officer ? c.getReserveAmount() : null, c.getApprovedAmount(),
                c.getStatus(), c.getAssignedOfficer(), c.getDecisionReason(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
