package com.claims.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class ClaimTest {

    private static final BigDecimal ESTIMATE = new BigDecimal("1000.00");

    private final Claim claim = new Claim("alice", "MY", ClaimType.MOTOR, "POL-1",
            LocalDate.of(2026, 11, 1), "Rear bumper damage", "MYR", ESTIMATE);

    @Test
    void newClaimIsSubmittedAndReserveStartsAsEstimate() {
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(claim.getReserveAmount()).isEqualByComparingTo(ESTIMATE);
    }

    @Test
    void claimCanMoveFromSubmittedToSettled() {
        claim.pickUp("olivia");
        claim.requestInfo("olivia");
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.INFO_REQUESTED);
        claim.answerInfo();
        claim.approve("olivia", new BigDecimal("900.00"));
        claim.settle("olivia");

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.SETTLED);
        assertThat(claim.getApprovedAmount()).isEqualByComparingTo("900.00");
        assertThat(claim.getAssignedOfficer()).isEqualTo("olivia");
    }

    @Test
    void assignedOfficerCanRejectWithReason() {
        claim.pickUp("olivia");
        claim.reject("olivia", "Policy expired");

        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(claim.getDecisionReason()).isEqualTo("Policy expired");
    }

    @Test
    void reserveCanChangeOnlyDuringReview() {
        claim.pickUp("olivia");
        claim.changeReserve("olivia", new BigDecimal("1500.00"));
        assertThat(claim.getReserveAmount()).isEqualByComparingTo("1500.00");

        claim.reject("olivia", "Duplicate");
        assertThatThrownBy(() -> claim.changeReserve("olivia", BigDecimal.TEN))
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void skippingOrRepeatingAStepIsNotAllowed() {
        assertThatThrownBy(() -> claim.answerInfo()).isInstanceOf(InvalidTransitionException.class);

        claim.pickUp("olivia");
        assertThatThrownBy(() -> claim.pickUp("omar")).isInstanceOf(InvalidTransitionException.class);
        assertThatThrownBy(() -> claim.settle("olivia")).isInstanceOf(InvalidTransitionException.class);

        claim.approve("olivia", ESTIMATE);
        claim.settle("olivia");
        assertThatThrownBy(() -> claim.settle("olivia")).isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void onlyTheAssignedOfficerCanDecide() {
        claim.pickUp("olivia");

        assertThatThrownBy(() -> claim.approve("omar", ESTIMATE)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> claim.reject("omar", "No")).isInstanceOf(AccessDeniedException.class);
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
    }
}
