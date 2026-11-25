package com.claims.claim;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ClaimRequests {

    private ClaimRequests() {
    }

    public record NewClaim(
            @NotNull @Pattern(regexp = "[A-Z]{2}") String market,
            @NotNull ClaimType type,
            @NotBlank @Size(max = 50) String policyNumber,
            @NotNull @PastOrPresent LocalDate incidentDate,
            @NotBlank @Size(max = 2000) String description,
            @NotNull Currency currency,
            @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal estimatedAmount) {
    }

    public record Message(@NotBlank @Size(max = 2000) String message) {
    }

    public record Amount(
            @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount) {
    }
}
