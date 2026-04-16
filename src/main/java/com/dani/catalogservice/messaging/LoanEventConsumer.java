package com.dani.catalogservice.messaging;

import com.dani.catalogservice.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoanEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LoanEventConsumer.class);

    private final CatalogService catalogService;

    public LoanEventConsumer(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LOAN_COPY_RESERVATION_REQUESTED)
    public void handleCopyReservationRequested(CopyReservationRequestedEnvelope message) {
        UUID loanId  = message.data().loanId();
        UUID titleId = message.data().titleId();
        log.info("Received copy_reservation_requested: loanId={}, titleId={}", loanId, titleId);
        catalogService.reserveCopy(loanId, titleId);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LOAN_COPY_RELEASE_REQUESTED)
    public void handleCopyReleaseRequested(CopyReleaseRequestedEnvelope message) {
        UUID loanId = message.data().loanId();
        UUID copyId = message.data().copyId();
        log.info("Received copy_release_requested: loanId={}, copyId={}", loanId, copyId);
        catalogService.releaseCopy(loanId, copyId);
    }

    // --- Incoming message shapes (matching loan-service MessageEnvelope<T> structure) ---

    record CopyReservationRequestedEnvelope(String event, CopyReservationRequestedData data, EventMetadata metadata) {}

    record CopyReservationRequestedData(UUID loanId, UUID titleId) {}

    record CopyReleaseRequestedEnvelope(String event, CopyReleaseRequestedData data, EventMetadata metadata) {}

    record CopyReleaseRequestedData(UUID loanId, UUID copyId) {}
}