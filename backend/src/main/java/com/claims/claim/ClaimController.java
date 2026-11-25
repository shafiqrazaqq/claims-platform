package com.claims.claim;

import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.claims.claim.ClaimRequests.Amount;
import com.claims.claim.ClaimRequests.Message;
import com.claims.claim.ClaimRequests.NewClaim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// Role checks per path are in SecurityConfig.
@RestController
@RequestMapping("/api")
public class ClaimController {

    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public Me me(Authentication user) {
        return new Me(user.getName(), isOfficer(user) ? "OFFICER" : "CLAIMANT");
    }

    @PostMapping("/claims")
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimDetail submit(Authentication user, @Valid @RequestBody NewClaim request) {
        return service.submit(user.getName(), request);
    }

    @GetMapping("/claims")
    public PagedModel<ClaimView> list(Authentication user, @RequestParam(required = false) ClaimStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new PagedModel<>(service.list(user.getName(), isOfficer(user), status, page, size));
    }

    @GetMapping("/claims/{id}")
    public ClaimDetail get(Authentication user, @PathVariable Long id) {
        return service.get(id, user.getName(), isOfficer(user));
    }

    @PostMapping("/claims/{id}/info-response")
    public ClaimDetail answerInfo(Authentication user, @PathVariable Long id, @Valid @RequestBody Message body) {
        return service.answerInfo(id, user.getName(), body.message());
    }

    @PostMapping("/claims/{id}/pick-up")
    public ClaimDetail pickUp(Authentication user, @PathVariable Long id) {
        return service.pickUp(id, user.getName());
    }

    @PostMapping("/claims/{id}/info-request")
    public ClaimDetail requestInfo(Authentication user, @PathVariable Long id, @Valid @RequestBody Message body) {
        return service.requestInfo(id, user.getName(), body.message());
    }

    @PutMapping("/claims/{id}/reserve")
    public ClaimDetail changeReserve(Authentication user, @PathVariable Long id, @Valid @RequestBody Amount body) {
        return service.changeReserve(id, user.getName(), body.amount());
    }

    @PostMapping("/claims/{id}/approve")
    public ClaimDetail approve(Authentication user, @PathVariable Long id, @Valid @RequestBody Amount body) {
        return service.approve(id, user.getName(), body.amount());
    }

    @PostMapping("/claims/{id}/reject")
    public ClaimDetail reject(Authentication user, @PathVariable Long id, @Valid @RequestBody Message body) {
        return service.reject(id, user.getName(), body.message());
    }

    @PostMapping("/claims/{id}/settle")
    public ClaimDetail settle(Authentication user, @PathVariable Long id) {
        return service.settle(id, user.getName());
    }

    @GetMapping("/summary")
    public Summary summary() {
        return service.summary();
    }

    private static boolean isOfficer(Authentication user) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER"));
    }

    public record Me(String username, String role) {
    }
}
