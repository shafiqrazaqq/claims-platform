package com.claims.claim;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimActivityRepository extends JpaRepository<ClaimActivity, Long> {

    List<ClaimActivity> findByClaimIdOrderById(Long claimId);
}
