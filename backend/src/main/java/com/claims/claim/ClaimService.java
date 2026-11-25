package com.claims.claim;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.claims.claim.ClaimDetail.Activity;
import com.claims.claim.ClaimRequests.NewClaim;

@Service
@Transactional
public class ClaimService {

    private final ClaimRepository claims;
    private final ClaimActivityRepository activities;
    private final ApplicationEventPublisher events;

    public ClaimService(ClaimRepository claims, ClaimActivityRepository activities,
            ApplicationEventPublisher events) {
        this.claims = claims;
        this.activities = activities;
        this.events = events;
    }

    public ClaimDetail submit(String claimant, NewClaim request) {
        Claim claim = claims.save(new Claim(claimant, request.market(), request.type(), request.policyNumber(),
                request.incidentDate(), request.description(), request.currency().getCurrencyCode(),
                request.estimatedAmount()));
        addActivity(claim, ActivityType.SUBMITTED, claimant, null);
        return detail(claim, false);
    }

    @Transactional(readOnly = true)
    public Page<ClaimView> list(String user, boolean officer, ClaimStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Claim> found;
        if (officer) {
            found = status == null ? claims.findAll(pageable) : claims.findByStatus(status, pageable);
        } else {
            found = status == null ? claims.findByClaimant(user, pageable)
                    : claims.findByClaimantAndStatus(user, status, pageable);
        }
        return found.map(claim -> ClaimView.of(claim, officer));
    }

    @Transactional(readOnly = true)
    public ClaimDetail get(Long id, String user, boolean officer) {
        return detail(officer ? find(id) : findOwn(id, user), officer);
    }

    public ClaimDetail answerInfo(Long id, String claimant, String message) {
        Claim claim = findOwn(id, claimant);
        claim.answerInfo();
        addActivity(claim, ActivityType.INFO_PROVIDED, claimant, message);
        return detail(claim, false);
    }

    public ClaimDetail pickUp(Long id, String officer) {
        return officerAction(id, officer, ActivityType.PICKED_UP, null, claim -> claim.pickUp(officer));
    }

    public ClaimDetail requestInfo(Long id, String officer, String message) {
        return officerAction(id, officer, ActivityType.INFO_REQUESTED, message, claim -> claim.requestInfo(officer));
    }

    public ClaimDetail changeReserve(Long id, String officer, BigDecimal amount) {
        return officerAction(id, officer, ActivityType.RESERVE_CHANGED, amount.toPlainString(),
                claim -> claim.changeReserve(officer, amount));
    }

    public ClaimDetail approve(Long id, String officer, BigDecimal amount) {
        return officerAction(id, officer, ActivityType.APPROVED, amount.toPlainString(),
                claim -> claim.approve(officer, amount));
    }

    public ClaimDetail reject(Long id, String officer, String reason) {
        return officerAction(id, officer, ActivityType.REJECTED, reason, claim -> claim.reject(officer, reason));
    }

    public ClaimDetail settle(Long id, String officer) {
        return officerAction(id, officer, ActivityType.SETTLED, null, claim -> claim.settle(officer));
    }

    @Transactional(readOnly = true)
    public Summary summary() {
        return new Summary(claims.countByStatus(), claims.countByOfficer(), claims.outstandingByCurrency());
    }

    private ClaimDetail officerAction(Long id, String officer, ActivityType type, String message,
            Consumer<Claim> action) {
        Claim claim = find(id);
        action.accept(claim);
        addActivity(claim, type, officer, message);
        return detail(claim, true);
    }

    private void addActivity(Claim claim, ActivityType type, String actor, String message) {
        activities.save(new ClaimActivity(claim.getId(), type, actor, message));
        events.publishEvent(new ClaimChanged(claim.getId(), type));
    }

    private ClaimDetail detail(Claim claim, boolean officer) {
        return new ClaimDetail(ClaimView.of(claim, officer),
                activities.findByClaimIdOrderById(claim.getId()).stream()
                        .filter(a -> officer || a.getType() != ActivityType.RESERVE_CHANGED)
                        .map(a -> new Activity(a.getType(), a.getActor(), a.getMessage(), a.getCreatedAt()))
                        .toList());
    }

    private Claim find(Long id) {
        return claims.findById(id).orElseThrow(() -> new NoSuchElementException("Claim not found"));
    }

    private Claim findOwn(Long id, String claimant) {
        return claims.findByIdAndClaimant(id, claimant)
                .orElseThrow(() -> new NoSuchElementException("Claim not found"));
    }
}
