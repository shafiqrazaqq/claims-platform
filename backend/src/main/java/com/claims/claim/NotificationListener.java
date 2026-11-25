package com.claims.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    // shortcut: a crash after commit loses the notification, use an outbox and Kafka when another service needs it
    @TransactionalEventListener
    public void onClaimChanged(ClaimChanged event) {
        log.info("Notify: claim {} {}", event.claimId(), event.type());
    }
}
