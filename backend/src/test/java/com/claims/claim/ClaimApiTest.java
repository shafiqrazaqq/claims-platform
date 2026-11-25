package com.claims.claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimApiTest {

    private static final RequestPostProcessor ALICE = user("alice").roles("CLAIMANT");
    private static final RequestPostProcessor BOB = user("bob").roles("CLAIMANT");
    private static final RequestPostProcessor OLIVIA = user("olivia").roles("OFFICER");
    private static final RequestPostProcessor OMAR = user("omar").roles("OFFICER");

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ClaimRepository claims;
    @Autowired
    private ClaimActivityRepository activities;
    @Value("${app.demo-password}")
    private String demoPassword;

    @BeforeEach
    void clean() {
        activities.deleteAll();
        claims.deleteAll();
    }

    @Test
    void claimGoesFromSubmissionToSettlement() throws Exception {
        long id = submit("MYR", "1000.00");

        send(post(url(id, "pick-up")), OLIVIA, null).andExpect(status().isOk());
        send(post(url(id, "info-request")), OLIVIA, "{\"message\":\"Send the repair quote\"}")
                .andExpect(jsonPath("$.claim.status").value("INFO_REQUESTED"));
        send(post(url(id, "info-response")), ALICE, "{\"message\":\"Quote is 950\"}")
                .andExpect(jsonPath("$.claim.status").value("UNDER_REVIEW"));
        send(post(url(id, "approve")), OLIVIA, "{\"amount\":950.00}").andExpect(status().isOk());
        send(post(url(id, "settle")), OLIVIA, null).andExpect(status().isOk());

        send(get("/api/claims/" + id), ALICE, null)
                .andExpect(jsonPath("$.claim.status").value("SETTLED"))
                .andExpect(jsonPath("$.claim.approvedAmount").value(950.00))
                .andExpect(jsonPath("$.timeline.length()").value(6))
                .andExpect(jsonPath("$.timeline[2].message").value("Send the repair quote"));
    }

    @Test
    void realLoginWorksAndMissingLoginGets401Json() throws Exception {
        mvc.perform(get("/api/me").with(httpBasic("olivia", demoPassword)))
                .andExpect(jsonPath("$.role").value("OFFICER"));
        mvc.perform(get("/api/me").with(httpBasic("olivia", "wrong"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/claims"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Login required"));
    }

    @Test
    void rolesAreEnforced() throws Exception {
        long id = submit("MYR", "1000.00");

        send(post("/api/claims"), OLIVIA, newClaim("MYR", "10.00", "2026-11-01")).andExpect(status().isForbidden());
        for (String action : new String[] { "pick-up", "info-request", "approve", "reject", "settle" }) {
            send(post(url(id, action)), ALICE, "{\"message\":\"x\",\"amount\":1}").andExpect(status().isForbidden());
        }
        send(put(url(id, "reserve")), ALICE, "{\"amount\":1}").andExpect(status().isForbidden());
        send(get("/api/summary"), ALICE, null).andExpect(status().isForbidden());
    }

    @Test
    void onlyTheAssignedOfficerCanDecide() throws Exception {
        long id = submit("MYR", "1000.00");
        send(post(url(id, "pick-up")), OLIVIA, null);

        send(post(url(id, "approve")), OMAR, "{\"amount\":1000.00}").andExpect(status().isForbidden());
        send(get("/api/claims/" + id), OLIVIA, null).andExpect(jsonPath("$.claim.status").value("UNDER_REVIEW"));
    }

    @Test
    void anotherClaimantsClaimLooksTheSameAsAMissingClaim() throws Exception {
        long id = submit("MYR", "1000.00");

        send(get("/api/claims/" + id), BOB, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Claim not found"));
        send(get("/api/claims/999999"), BOB, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Claim not found"));
        send(post(url(id, "info-response")), BOB, "{\"message\":\"x\"}").andExpect(status().isNotFound());
        send(get("/api/claims"), BOB, null).andExpect(jsonPath("$.content.length()").value(0));
        send(get("/api/claims?status=SUBMITTED"), BOB, null).andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void claimantDoesNotSeeTheReserve() throws Exception {
        long id = submit("MYR", "1000.00");
        send(post(url(id, "pick-up")), OLIVIA, null);
        send(put(url(id, "reserve")), OLIVIA, "{\"amount\":1500.00}")
                .andExpect(jsonPath("$.claim.reserveAmount").value(1500.00))
                .andExpect(jsonPath("$.timeline.length()").value(3));

        send(get("/api/claims/" + id), ALICE, null)
                .andExpect(jsonPath("$.claim.reserveAmount").doesNotExist())
                .andExpect(jsonPath("$.timeline.length()").value(2));
        send(get("/api/claims"), ALICE, null).andExpect(jsonPath("$.content[0].reserveAmount").doesNotExist());
    }

    @Test
    void badInputGets400() throws Exception {
        String future = LocalDate.now().plusDays(2).toString();
        for (String body : new String[] {
                newClaim("MYR", "0", "2026-11-01"),
                newClaim("MYR", "10.555", "2026-11-01"),
                newClaim("XXQ", "10.00", "2026-11-01"),
                newClaim("MYR", "10.00", future),
                "{}", "not json" }) {
            send(post("/api/claims"), ALICE, body).andExpect(status().isBadRequest());
        }
        long id = submit("MYR", "1000.00");
        send(post(url(id, "pick-up")), OLIVIA, null);
        send(post(url(id, "reject")), OLIVIA, "{\"message\":\"  \"}").andExpect(status().isBadRequest());
        send(get("/api/claims?page=-1"), OLIVIA, null).andExpect(status().isBadRequest());
        send(get("/api/claims?size=101"), OLIVIA, null).andExpect(status().isBadRequest());
        send(get("/api/claims?status=NOPE"), OLIVIA, null).andExpect(status().isBadRequest());
        send(get("/api/claims/abc"), OLIVIA, null).andExpect(status().isBadRequest());
    }

    @Test
    void secondPickUpGets409() throws Exception {
        long id = submit("MYR", "1000.00");
        send(post(url(id, "pick-up")), OLIVIA, null).andExpect(status().isOk());
        send(post(url(id, "pick-up")), OMAR, null).andExpect(status().isConflict());
        send(get("/api/claims/" + id), OMAR, null).andExpect(jsonPath("$.claim.assignedOfficer").value("olivia"));
    }

    @Test
    void staleUpdateDoesNotOverwriteANewerOne() throws Exception {
        long id = submit("MYR", "1000.00");
        Claim stale = claims.findById(id).orElseThrow();
        send(post(url(id, "pick-up")), OLIVIA, null).andExpect(status().isOk());

        stale.pickUp("omar");
        assertThatThrownBy(() -> claims.save(stale)).isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(claims.findById(id).orElseThrow().getAssignedOfficer()).isEqualTo("olivia");
    }

    @Test
    void summaryCountsLiabilityPerCurrency() throws Exception {
        submit("MYR", "1000.00");                              // open: reserve 1000

        long approved = submit("MYR", "500.00");               // approved: 400
        send(post(url(approved, "pick-up")), OLIVIA, null);
        send(post(url(approved, "approve")), OLIVIA, "{\"amount\":400.00}");

        long rejected = submit("MYR", "9999.00");              // rejected: 0
        send(post(url(rejected, "pick-up")), OLIVIA, null);
        send(post(url(rejected, "reject")), OLIVIA, "{\"message\":\"Not covered\"}");

        long settled = submit("SGD", "700.00");                // settled: 0
        send(post(url(settled, "pick-up")), OMAR, null);
        send(post(url(settled, "approve")), OMAR, "{\"amount\":700.00}");
        send(post(url(settled, "settle")), OMAR, null);

        submit("SGD", "300.00");                               // open: reserve 300

        send(get("/api/summary"), OLIVIA, null)
                .andExpect(jsonPath("$.outstanding.length()").value(2))
                .andExpect(jsonPath("$.outstanding[0].currency").value("MYR"))
                .andExpect(jsonPath("$.outstanding[0].amount").value(1400.00))
                .andExpect(jsonPath("$.outstanding[1].currency").value("SGD"))
                .andExpect(jsonPath("$.outstanding[1].amount").value(300.00))
                .andExpect(jsonPath("$.byStatus[?(@.status == 'SUBMITTED')].count").value(2))
                .andExpect(jsonPath("$.byOfficer[?(@.officer == 'omar')].count").value(1));
    }

    private long submit(String currency, String amount) throws Exception {
        String json = send(post("/api/claims"), ALICE, newClaim(currency, amount, "2026-11-01"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Integer>read(json, "$.claim.id");
    }

    private static String newClaim(String currency, String amount, String date) {
        return """
                {"market":"MY","type":"MOTOR","policyNumber":"POL-1","incidentDate":"%s",
                 "description":"Rear bumper damage","currency":"%s","estimatedAmount":%s}
                """.formatted(date, currency, amount);
    }

    private static String url(long id, String action) {
        return "/api/claims/" + id + "/" + action;
    }

    private ResultActions send(MockHttpServletRequestBuilder request, RequestPostProcessor as, String body)
            throws Exception {
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mvc.perform(request.with(as));
    }
}
