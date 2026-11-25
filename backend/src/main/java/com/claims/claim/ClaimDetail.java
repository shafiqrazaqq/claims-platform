package com.claims.claim;

import java.time.Instant;
import java.util.List;

public record ClaimDetail(ClaimView claim, List<Activity> timeline) {

    public record Activity(ActivityType type, String actor, String message, Instant createdAt) {
    }
}
