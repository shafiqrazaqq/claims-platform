package com.claims.claim;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.claims.claim.Summary.CurrencyTotal;
import com.claims.claim.Summary.OfficerCount;
import com.claims.claim.Summary.StatusCount;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByIdAndClaimant(Long id, String claimant);

    Page<Claim> findByClaimant(String claimant, Pageable page);

    Page<Claim> findByClaimantAndStatus(String claimant, ClaimStatus status, Pageable page);

    Page<Claim> findByStatus(ClaimStatus status, Pageable page);

    @Query("""
            select new com.claims.claim.Summary$StatusCount(c.status, count(c))
            from Claim c group by c.status order by c.status
            """)
    List<StatusCount> countByStatus();

    @Query("""
            select new com.claims.claim.Summary$OfficerCount(c.assignedOfficer, c.status, count(c))
            from Claim c where c.assignedOfficer is not null
            group by c.assignedOfficer, c.status order by c.assignedOfficer, c.status
            """)
    List<OfficerCount> countByOfficer();

    // Approved claims count the approved amount. Other open claims count the reserve.
    @Query("""
            select new com.claims.claim.Summary$CurrencyTotal(c.currency,
                sum(case when c.status = com.claims.claim.ClaimStatus.APPROVED
                    then c.approvedAmount else c.reserveAmount end))
            from Claim c
            where c.status not in (com.claims.claim.ClaimStatus.REJECTED, com.claims.claim.ClaimStatus.SETTLED)
            group by c.currency order by c.currency
            """)
    List<CurrencyTotal> outstandingByCurrency();
}
