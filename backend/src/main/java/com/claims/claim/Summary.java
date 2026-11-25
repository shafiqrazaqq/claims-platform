package com.claims.claim;

import java.math.BigDecimal;
import java.util.List;

public record Summary(List<StatusCount> byStatus, List<OfficerCount> byOfficer, List<CurrencyTotal> outstanding) {

    public record StatusCount(ClaimStatus status, long count) {
    }

    public record OfficerCount(String officer, ClaimStatus status, long count) {
    }

    public record CurrencyTotal(String currency, BigDecimal amount) {
    }
}
